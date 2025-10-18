#!/usr/bin/env bash
# ============================================================
# RustDesk hbb_common 本地化脚本（适用于 libs/hbb_common 结构）
# 作者: ziwen
# 功能: 将 hbb_common 子模块改为本地源码，不再依赖远程仓库
# ============================================================

set -e
set -u

# 颜色输出
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}>>> [1/6] 检查项目结构...${NC}"
if [ ! -f "Cargo.toml" ]; then
  echo "错误: 请在 RustDesk 项目根目录执行此脚本（包含 Cargo.toml）。"
  exit 1
fi

# 确保 libs 目录存在
mkdir -p libs

# 子模块路径
SUBMODULE_PATH="libs/hbb_common"

# 检查是否存在 .gitmodules 中的配置
if grep -q "hbb_common" .gitmodules 2>/dev/null; then
  echo -e "${YELLOW}>>> [2/6] 移除子模块配置与 Git 记录...${NC}"
  git submodule deinit -f "$SUBMODULE_PATH" || true
  rm -rf ".git/modules/$SUBMODULE_PATH" || true

  echo -e "${YELLOW}>>> [3/6] 删除旧的子模块目录与配置...${NC}"
  rm -rf "$SUBMODULE_PATH"
  # 删除 gitmodules 中相关行
  sed -i '/hbb_common/d' .gitmodules 2>/dev/null || true
else
  echo "未检测到 hbb_common 子模块配置，跳过删除步骤。"
fi

# 复制本地版本
echo -e "${YELLOW}>>> [4/6] 复制本地 hbb_common 源码...${NC}"
read -p "请输入本地 hbb_common 路径 (例如 ../hbb_common_local): " LOCAL_PATH

if [ ! -d "$LOCAL_PATH" ]; then
  echo "错误: 路径 '$LOCAL_PATH' 不存在或不是目录。"
  exit 1
fi

cp -r "$LOCAL_PATH" "$SUBMODULE_PATH"
echo -e "${GREEN}本地模块已复制到 $SUBMODULE_PATH${NC}"

# 修改 Cargo.toml
echo -e "${YELLOW}>>> [5/6] 更新 Cargo.toml 依赖为本地路径...${NC}"

# 删除旧的 git 依赖声明
sed -i '/hbb_common/d' Cargo.toml 2>/dev/null || true

cat <<EOF >> Cargo.toml

[dependencies.hbb_common]
path = "libs/hbb_common"
EOF

echo -e "${GREEN}Cargo.toml 已更新为本地路径依赖。${NC}"

# 忽略本地目录（防止上传）
if ! grep -q "^/libs/hbb_common" .gitignore 2>/dev/null; then
  echo "/libs/hbb_common" >> .gitignore
  echo -e "${GREEN}已将 libs/hbb_common 添加至 .gitignore${NC}"
fi

echo -e "${YELLOW}>>> [6/6] 验证配置...${NC}"
if cargo check >/dev/null 2>&1; then
  echo -e "${GREEN}\n✅ hbb_common 已成功本地化并可正常编译！${NC}"
else
  echo -e "${YELLOW}\n⚠️  本地化完成，但请手动执行 'cargo check' 检查依赖。${NC}"
fi
