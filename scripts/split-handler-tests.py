#!/usr/bin/env python3
"""Split monolithic *HandlersTest classes into per-handler test files."""

from __future__ import annotations

import re
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
TEST_ROOT = ROOT / "magical-vibes-application/src/test/java/com/github/laxika/magicalvibes"
NORMALFX_TEST = TEST_ROOT / "service/effect/normalfx"
STATICFX_TEST = TEST_ROOT / "service/effect/staticfx"

MONOLITH_PATHS = [
    "service/battlefield/CounterHandlersTest.java",
    "service/battlefield/BounceHandlersTest.java",
    "service/battlefield/CopyHandlersTest.java",
    "service/battlefield/ExileHandlersTest.java",
    "service/battlefield/DestructionHandlersTest.java",
    "service/combat/DamageHandlersTest.java",
    "service/graveyard/GraveyardReturnHandlersTest.java",
    "service/library/LibraryRevealHandlersTest.java",
    "service/library/LibrarySearchHandlersTest.java",
    "service/library/MillHandlersTest.java",
    "service/library/LibraryShuffleHandlersTest.java",
    "service/turn/TurnHandlersTest.java",
    "service/target/TargetRedirectionHandlersTest.java",
    "service/effect/PermanentControlHandlersTest.java",
    "service/effect/StaticEffectHandlersTest.java",
    "service/effect/KeywordGrantHandlersTest.java",
    "service/effect/PlayerInteractionHandlersTest.java",
]

BASE_CLASS = {
    "DamageHandlersTest": "AbstractDamageHandlerTest",
    "PlayerInteractionHandlersTest": "AbstractPlayerInteractionHandlerTest",
    "StaticEffectHandlersTest": "AbstractStaticEffectHandlerTest",
}

DISPLAY_NAME_TO_HANDLER = {
    "resolveBite": "FirstTargetDealsPowerDamageToSecondTargetEffectHandler",
    "resolveSearchLibraryForCardToHand": "SearchLibraryForCardsToHandEffectHandler",
    "resolveMayEffect": "MayEffectHandler",
    "resolveMayPayManaEffect": "MayPayManaEffectHandler",
    "resolveDiscardUnlessExileFromGraveyard": "DiscardUnlessExileCardFromGraveyardEffectHandler",
    "resolvePutAwakeningCounters": "PutAwakeningCountersOnTargetLandsEffectHandler",
    "resolveAwardAnyColorManaWithCopy": "AwardAnyColorManaWithInstantSorceryCopyEffectHandler",
    "resolveChooseCardFromTargetHandToDiscardHandler": "ChooseCardFromTargetHandToDiscardEffectHandler",
    "resolveChooseCardFromTargetHandToExileHandler": "ChooseCardFromTargetHandToExileEffectHandler",
    "resolveLookAtTopXCards with remainingToBottomRandom": "LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffectHandler",
    "resolvePutCreatureFromOpponentGraveyardWithExile": "PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffectHandler",
    "resolveReturnCardFromGraveyard — pre-targeted": "ReturnCardFromGraveyardEffectHandler",
    "resolveReturnCardFromGraveyard — all graveyards search": "ReturnCardFromGraveyardEffectHandler",
    "resolveExileTargetPermanent — single target": "ExileTargetPermanentEffectHandler",
    "resolveExileTargetPermanent — multi-target": "ExileTargetPermanentEffectHandler",
    "resolveGainControlOfTargetPermanent": "GainControlOfTargetPermanentEffectHandler",
    "handleKnowledgePoolCastChoice": "KnowledgePoolExileAndCastEffectHandler",
    "handleMirrorOfFateChoice": "MirrorOfFateEffectHandler",
    "describeFilter": "ReturnCardFromGraveyardEffectHandler",
    "startNextEachPlayerBasicLandSearch": "DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffectHandler",
    "resolveDrawXCardsForTargetPlayer": "DrawXCardsForTargetPlayerEffectHandler",
    "resolveFlipCoinWinEffect": "FlipCoinWinEffectHandler",
    "resolveFlipTwoCoinsEffect": "FlipTwoCoinsEffectHandler",
    "resolveSacrificePermanentThen": "SacrificePermanentThenEffectHandler",
    "checkSearchRestriction": "SearchLibraryForCardsToHandEffectHandler",
    "startNextEachPlayerDiscard": "EachPlayerDiscardsEffectHandler",
    "resolveDestroyTargetAndEachPlayerSearchesBasicLandToBattlefield": "DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffectHandler",
}

SPECIAL_RENAME_ONLY = {
    "KeywordGrantHandlersTest": "GrantKeywordToTargetIfPermanentEffectHandlerTest",
}

STATIC_IMPORTS = """import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;"""

PI_IMPORTS = """import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;"""

DAMAGE_IMPORTS = """import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;"""


def read_monolith(relative: str) -> str:
    path = TEST_ROOT / relative
    if path.exists():
        return path.read_text(encoding="utf-8")
    git_path = f":magical-vibes-application/src/test/java/com/github/laxika/magicalvibes/{relative}"
    result = subprocess.run(
        ["git", "show", git_path],
        cwd=ROOT,
        capture_output=True,
        text=True,
        check=False,
    )
    if result.returncode != 0:
        raise FileNotFoundError(f"Cannot read monolith {relative}: {result.stderr}")
    return result.stdout


def find_matching_brace(text: str, open_index: int) -> int:
    depth = 0
    for i in range(open_index, len(text)):
        if text[i] == "{":
            depth += 1
        elif text[i] == "}":
            depth -= 1
            if depth == 0:
                return i
    raise ValueError("Unmatched brace")


def extract_handler_fields(content: str) -> dict[str, str]:
    return {
        match.group(2): match.group(1)
        for match in re.finditer(r"private\s+(\w+EffectHandler)\s+(\w+)\s*;", content)
    }


def extract_handler_inits(content: str) -> dict[str, str]:
    return {
        match.group(1): match.group(0)
        for match in re.finditer(r"(\w+)\s*=\s*new\s+(\w+EffectHandler)\([^;]*\);", content)
    }


def extract_nested_blocks(content: str) -> list[tuple[str, str, str]]:
    blocks = []
    pos = 0
    while True:
        idx = content.find("@Nested", pos)
        if idx == -1:
            break
        display_match = re.search(r'@DisplayName\("([^"]+)"\)', content[idx:idx + 300])
        display_name = display_match.group(1) if display_match else "unknown"
        class_match = re.search(r"class\s+(\w+)\s*\{", content[idx:])
        if not class_match:
            break
        brace_start = idx + class_match.end() - 1
        brace_end = find_matching_brace(content, brace_start)
        body = content[brace_start + 1:brace_end].strip()
        blocks.append((display_name, class_match.group(1), body))
        pos = brace_end + 1
    return blocks


def detect_handler_type(display_name: str, body: str, field_map: dict[str, str]) -> str | None:
    if display_name in DISPLAY_NAME_TO_HANDLER:
        return DISPLAY_NAME_TO_HANDLER[display_name]

    handler_calls = re.findall(r"(\w+Handler)\.resolve", body)
    if handler_calls:
        field = max(set(handler_calls), key=handler_calls.count)
        if field in field_map:
            return field_map[field]

    for field, handler_type in field_map.items():
        if re.search(rf"\b{re.escape(field)}\.resolve\(", body):
            return handler_type

    if "getSelfHandler" in body:
        effects = re.findall(r"new\s+(\w+Effect)\s*\(", body)
        if effects:
            effect = effects[0]
            if effect.endswith("Effect"):
                return effect[:-6] + "SelfEffectHandler"
        return None

    if "resolveEffect(" in body:
        typed = re.findall(r"(\w+Effect)\s+\w+\s*=\s*new\s+\1\s*\(", body)
        if typed:
            effect = typed[0]
            return effect[:-6] + "EffectHandler" if effect.endswith("Effect") else None
        effects = re.findall(r"new\s+(\w+Effect)\s*\(", body)
        if effects:
            effect = effects[0]
            if effect.endswith("Effect"):
                return effect[:-6] + "EffectHandler"
    return None


def extract_helpers_before_nested(content: str) -> str:
    idx = content.find("@Nested")
    if idx == -1:
        return ""
    preamble = content[:idx]
    for marker in ("// ===== Helper methods =====", "private Card create", "private static", "private StackEntry"):
        helper_start = preamble.find(marker)
        if helper_start != -1:
            helpers = preamble[helper_start:]
            helpers = re.sub(r"@BeforeEach\s+void setUp\(\)\s*\{[\s\S]*?\n    \}\n", "", helpers)
            return helpers.strip()
    return ""


def collect_imports(content: str) -> list[str]:
    return [line for line in content.splitlines() if line.startswith("import ") and "Nested" not in line]


def extract_class_preamble(content: str) -> str:
    idx = content.find("@Nested")
    if idx == -1:
        idx = content.rfind("}")
    return content[:idx]


def extract_setup_block(content: str, init_line: str) -> str:
    setup_match = re.search(r"@BeforeEach\s+void setUp\(\)\s*\{([\s\S]*?)\n    \}", content)
    if not setup_match:
        return f"        {init_line.strip()}\n"
    setup_body = setup_match.group(1)
    setup_body = re.sub(r"\s*\w+Handler\s*=\s*new\s+\w+EffectHandler\([^;]*;\s*", "\n", setup_body)
    return setup_body.rstrip() + f"\n        {init_line.strip()}\n"


def extract_field_block(content: str) -> str:
    class_start = content.find("class ")
    before_each = content.find("@BeforeEach")
    if class_start == -1 or before_each == -1:
        return ""
    block = content[class_start:before_each]
    lines = []
    for line in block.splitlines()[1:]:
        if line.strip().startswith("private") and "EffectHandler" in line:
            continue
        if line.strip():
            lines.append(line)
    return "\n".join(lines).rstrip()


def build_damage_test(content: str, handler_type: str, handler_field: str, init_line: str, body: str) -> str:
    imports = [i for i in collect_imports(content) if "Nested" not in i]
    model_imports = "\n".join(i for i in imports if not i.startswith("import static"))
    static_imports = "\n".join(i for i in imports if i.startswith("import static"))
    if not static_imports:
        static_imports = DAMAGE_IMPORTS.split("\n", 1)[1] if False else ""
    extra_static = """
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;"""
    return f"""package com.github.laxika.magicalvibes.service.effect.normalfx;

{model_imports}
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
{extra_static}

class {handler_type}Test extends AbstractDamageHandlerTest {{

    private {handler_type} {handler_field};

    @Override
    protected void setUpHandler() {{
        {init_line.strip()}
    }}

{indent(body, 4)}
}}
"""


def build_player_interaction_test(content: str, handler_type: str, body: str) -> str:
    imports = [i for i in collect_imports(content) if "Nested" not in i]
    model_imports = "\n".join(i for i in imports if not i.startswith("import static"))
    return f"""package com.github.laxika.magicalvibes.service.effect.normalfx;

{model_imports}
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class {handler_type}Test extends AbstractPlayerInteractionHandlerTest {{

{indent(body, 4)}
}}
"""


def build_static_test(handler_type: str, body: str, helpers: str) -> str:
    helper_block = indent(helpers.replace("private static", "protected static"), 4) + "\n\n" if helpers else ""
    return f"""package com.github.laxika.magicalvibes.service.effect.staticfx;

{STATIC_IMPORTS}

class {handler_type}Test extends AbstractStaticEffectHandlerTest {{

{helper_block}{indent(body, 4)}
}}
"""


def build_standalone_test(content: str, handler_type: str, handler_field: str, init_line: str, helpers: str, body: str) -> str:
    imports = [i for i in collect_imports(content) if "Nested" not in i]

    field_block = extract_field_block(content)
    setup_body = extract_setup_block(content, init_line)
    helpers_block = indent(helpers, 4) + "\n\n" if helpers else ""

    extra = [
        "import org.junit.jupiter.api.extension.ExtendWith;",
        "import org.mockito.junit.jupiter.MockitoExtension;",
        "import org.junit.jupiter.api.BeforeEach;",
        "import org.junit.jupiter.api.DisplayName;",
        "import org.junit.jupiter.api.Test;",
    ]
    filtered = []
    seen = set()
    for imp in imports + extra:
        simple = imp.replace("import ", "").replace(";", "").split(".")[-1]
        if simple not in seen:
            filtered.append(imp)
            seen.add(simple)
    handler_import = f"import com.github.laxika.magicalvibes.service.effect.normalfx.{handler_type};"
    if handler_type not in content:
        simple = handler_type
        if simple not in seen:
            filtered.append(handler_import)
            seen.add(simple)

    static_imports = [i for i in filtered if i.startswith("import static")]
    regular_imports = [i for i in filtered if not i.startswith("import static")]

    return f"""package com.github.laxika.magicalvibes.service.effect.normalfx;

{chr(10).join(regular_imports)}

{chr(10).join(static_imports)}

@ExtendWith(MockitoExtension.class)
class {handler_type}Test {{

{field_block}
    private {handler_type} {handler_field};

    @BeforeEach
    void setUp() {{
{setup_body}
    }}

{helpers_block}{indent(body, 4)}
}}
"""


def indent(text: str, spaces: int) -> str:
    pad = " " * spaces
    return "\n".join(pad + line if line.strip() else line for line in text.splitlines())


COMMON_TYPE_IMPORTS = {
    "GameQueryService": "com.github.laxika.magicalvibes.service.battlefield.GameQueryService",
    "CloneService": "com.github.laxika.magicalvibes.service.battlefield.CloneService",
    "CreatureControlService": "com.github.laxika.magicalvibes.service.battlefield.CreatureControlService",
    "BattlefieldEntryService": "com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService",
    "PermanentRemovalService": "com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService",
    "LegendRuleService": "com.github.laxika.magicalvibes.service.battlefield.LegendRuleService",
    "GraveyardService": "com.github.laxika.magicalvibes.service.graveyard.GraveyardService",
    "ExileService": "com.github.laxika.magicalvibes.service.exile.ExileService",
    "StateTriggerService": "com.github.laxika.magicalvibes.service.state.StateTriggerService",
    "CombatService": "com.github.laxika.magicalvibes.service.combat.CombatService",
    "DrawService": "com.github.laxika.magicalvibes.service.DrawService",
    "PlayerInputService": "com.github.laxika.magicalvibes.service.input.PlayerInputService",
    "SessionManager": "com.github.laxika.magicalvibes.networking.SessionManager",
    "CardViewFactory": "com.github.laxika.magicalvibes.networking.service.CardViewFactory",
    "TriggerCollectionService": "com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService",
    "GameBroadcastService": "com.github.laxika.magicalvibes.service.GameBroadcastService",
    "GameOutcomeService": "com.github.laxika.magicalvibes.service.GameOutcomeService",
    "DamagePreventionService": "com.github.laxika.magicalvibes.service.DamagePreventionService",
    "AuraAttachmentService": "com.github.laxika.magicalvibes.service.aura.AuraAttachmentService",
    "TurnCleanupService": "com.github.laxika.magicalvibes.service.turn.TurnCleanupService",
    "ValidTargetService": "com.github.laxika.magicalvibes.service.target.ValidTargetService",
    "TargetLegalityService": "com.github.laxika.magicalvibes.service.target.TargetLegalityService",
    "EffectHandlerRegistry": "com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry",
}


def fix_missing_imports(path: Path) -> None:
    content = path.read_text(encoding="utf-8")
    used_types = set(re.findall(r"@Mock\s+private\s+(\w+)", content))
    used_types.update(re.findall(r"@InjectMocks\s+private\s+(\w+)", content))
    used_types.update(re.findall(r"private final (\w+Support)", content))
    missing = []
    for type_name, fqcn in COMMON_TYPE_IMPORTS.items():
        if type_name in used_types and f"import {fqcn};" not in content and type_name in content:
            missing.append(f"import {fqcn};")
    if not missing:
        return
    package_end = content.find("\n\n", content.find("package "))
    if package_end == -1:
        return
    content = content[:package_end + 1] + "\n".join(missing) + content[package_end:]
    path.write_text(content, encoding="utf-8")


def fix_all_generated_imports() -> None:
    for directory in (NORMALFX_TEST, STATICFX_TEST):
        if not directory.exists():
            continue
        for path in directory.glob("*Test*.java"):
            if path.name.startswith("Abstract"):
                continue
            fix_missing_imports(path)


def clear_generated_tests() -> None:
    for directory in (NORMALFX_TEST, STATICFX_TEST):
        if not directory.exists():
            continue
        for path in directory.glob("*.java"):
            if path.name.startswith("Abstract"):
                continue
            if path.name.endswith("Test.java") or path.name.endswith("Tests.java"):
                path.unlink()


def process_monolith(relative: str) -> list[str]:
    content = read_monolith(relative)
    monolith_name = Path(relative).name
    monolith_stem = Path(relative).stem
    generated = []

    if monolith_stem in SPECIAL_RENAME_ONLY:
        target = NORMALFX_TEST / f"{SPECIAL_RENAME_ONLY[monolith_stem]}.java"
        text = content.replace("class KeywordGrantHandlersTest", f"class {SPECIAL_RENAME_ONLY[monolith_stem]}")
        text = text.replace("class KeywordGrantResolutionServiceTest", f"class {SPECIAL_RENAME_ONLY[monolith_stem]}")
        text = re.sub(r"package\s+[\w.]+;", "package com.github.laxika.magicalvibes.service.effect.normalfx;", text)
        if "import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;" not in text:
            text = text.replace(
                "import com.github.laxika.magicalvibes.service.effect.normalfx.GrantKeywordToTargetIfPermanentEffectHandler;",
                "import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;\nimport com.github.laxika.magicalvibes.service.effect.normalfx.GrantKeywordToTargetIfPermanentEffectHandler;",
            )
        target.write_text(text, encoding="utf-8")
        return [str(target)]

    nested_blocks = extract_nested_blocks(content)
    field_map = extract_handler_fields(content)
    handler_inits = extract_handler_inits(content)
    helpers = extract_helpers_before_nested(content)

    seen_handlers: dict[str, Path] = {}

    for display_name, _class_name, body in nested_blocks:
        handler_type = detect_handler_type(display_name, body, field_map)
        if not handler_type:
            print(f"WARNING: Could not detect handler for {monolith_name} / {display_name}")
            continue

        handler_field = next((f for f, t in field_map.items() if t == handler_type), None)
        if not handler_field:
            base = handler_type.replace("EffectHandler", "")
            handler_field = base[0].lower() + base[1:] + "Handler"

        init_line = handler_inits.get(handler_field, "")
        if not init_line:
            for field, line in handler_inits.items():
                if handler_type in line:
                    init_line = line
                    handler_field = field
                    break

        test_class = f"{handler_type}Test"

        if monolith_stem == "DamageHandlersTest":
            out = build_damage_test(content, handler_type, handler_field, init_line, body)
            out_path = NORMALFX_TEST / f"{test_class}.java"
        elif monolith_stem == "PlayerInteractionHandlersTest":
            out_path = NORMALFX_TEST / f"{test_class}.java"
            if test_class in seen_handlers:
                existing = seen_handlers[test_class].read_text(encoding="utf-8")
                merged_body = existing.rstrip().removesuffix("}") + "\n\n" + indent(body, 4) + "\n}\n"
                seen_handlers[test_class].write_text(merged_body, encoding="utf-8")
                print(f"Merged into {test_class}.java from {monolith_stem}::{display_name}")
                continue
            out = build_player_interaction_test(content, handler_type, body)
        elif monolith_stem == "StaticEffectHandlersTest":
            out = build_static_test(handler_type, body, helpers)
            out_path = STATICFX_TEST / f"{test_class}.java"
        else:
            out_path = NORMALFX_TEST / f"{test_class}.java"
            if test_class in seen_handlers:
                existing = seen_handlers[test_class].read_text(encoding="utf-8")
                merged_body = existing.rstrip().removesuffix("}") + "\n\n" + indent(body, 4) + "\n}\n"
                seen_handlers[test_class].write_text(merged_body, encoding="utf-8")
                print(f"Merged into {test_class}.java from {monolith_name}::{display_name}")
                continue
            out = build_standalone_test(content, handler_type, handler_field, init_line, helpers, body)

        out_path.write_text(out, encoding="utf-8")
        seen_handlers[test_class] = out_path
        generated.append(str(out_path))
        print(f"Generated {out_path.name} from {monolith_name}::{display_name}")

    return generated


def delete_monoliths() -> None:
    for relative in MONOLITH_PATHS:
        path = TEST_ROOT / relative
        if path.exists():
            path.unlink()
            print(f"Deleted {path.name}")


def main() -> None:
    NORMALFX_TEST.mkdir(parents=True, exist_ok=True)
    STATICFX_TEST.mkdir(parents=True, exist_ok=True)
    clear_generated_tests()

    all_generated = []
    for relative in MONOLITH_PATHS:
        try:
            all_generated.extend(process_monolith(relative))
        except FileNotFoundError as exc:
            print(exc)

    fix_all_generated_imports()
    delete_monoliths()
    print(f"\nTotal generated: {len(all_generated)}")


if __name__ == "__main__":
    main()
