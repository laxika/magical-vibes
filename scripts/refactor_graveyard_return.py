#!/usr/bin/env python3
"""Generate GraveyardReturnSupport and per-effect handlers from GraveyardReturnResolutionService."""

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SRC = ROOT / "magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/graveyard/GraveyardReturnResolutionService.java"
NORMALFX = ROOT / "magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/effect/normalfx"

FIELD_NAMES = [
    "battlefieldEntryService",
    "permanentRemovalService",
    "legendRuleService",
    "gameQueryService",
    "gameBroadcastService",
    "playerInputService",
    "lifeSupport",
    "exileService",
    "cardViewFactory",
]

HELPER_METHODS = [
    "resolvePreTargeted",
    "resolvePreTargetedById",
    "resolveReturnAll",
    "resolveReturnAtRandom",
    "resolveFromControllersGraveyard",
    "resolveFromAllGraveyards",
    "processTargetedGraveyardCards",
    "moveCardToDestination",
    "putCardOntoBattlefield",
    "putCardOntoBattlefieldWithHasteAndExile",
    "isCardBlockedFromEnteringFromZone",
    "applyPermanentGrants",
    "exileCardFromAnyGraveyard",
    "handleCreatureEtbAndLegendRule",
    "applyLifeGainEqualToManaValue",
    "trackStolenCreature",
    "stealFromOpponentGraveyard",
    "createTokenCopyFromCard",
    "beginGraveyardExileChoice",
    "beginNextGraveyardReturnFromQueue",
    "putCardOntoBattlefieldFromExile",
    "buildCardPileDescription",
]

TYPE_MAP = {
    "battlefieldEntryService": "BattlefieldEntryService",
    "permanentRemovalService": "PermanentRemovalService",
    "legendRuleService": "LegendRuleService",
    "gameQueryService": "GameQueryService",
    "gameBroadcastService": "GameBroadcastService",
    "playerInputService": "PlayerInputService",
    "lifeSupport": "LifeSupport",
    "exileService": "ExileService",
    "cardViewFactory": "CardViewFactory",
    "graveyardReturnSupport": "GraveyardReturnSupport",
}

IMPORT_MAP = {
    "lifeSupport": "import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;",
    "battlefieldEntryService": "import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;",
    "permanentRemovalService": "import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;",
    "legendRuleService": "import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;",
    "gameQueryService": "import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;",
    "gameBroadcastService": "import com.github.laxika.magicalvibes.service.GameBroadcastService;",
    "playerInputService": "import com.github.laxika.magicalvibes.service.input.PlayerInputService;",
    "exileService": "import com.github.laxika.magicalvibes.service.exile.ExileService;",
    "cardViewFactory": "import com.github.laxika.magicalvibes.networking.service.CardViewFactory;",
    "graveyardReturnSupport": "import com.github.laxika.magicalvibes.service.effect.normalfx.GraveyardReturnSupport;",
}

HANDLER_PATTERN = re.compile(
    r"(    /\*\*.*?\*/\r?\n)?"
    r"    @HandlesEffect\((\w+)\.class\)\r?\n"
    r"    (?:private |public )?void \w+\(([^\)]*)\) \{([\s\S]*?)^    \}\r?\n",
    re.MULTILINE,
)


def strip_handles_effect_methods(text: str) -> str:
    return HANDLER_PATTERN.sub("", text)


def rewrite_support(content: str) -> str:
    support = content
    support = support.replace(
        "package com.github.laxika.magicalvibes.service.graveyard;",
        "package com.github.laxika.magicalvibes.service.effect.normalfx;",
    )
    if "LifeSupport lifeSupport" in support and "import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;" not in support:
        support = support.replace(
            "import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;",
            "import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;\nimport com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;",
        )
    support = re.sub(
        r"import com\.github\.laxika\.magicalvibes\.service\.effect\.HandlesEffect;\r?\n",
        "",
        support,
    )
    support = support.replace("@Service", "@Component")
    support = support.replace(
        "import org.springframework.stereotype.Service;",
        "import org.springframework.stereotype.Component;",
    )
    support = support.replace(
        "public class GraveyardReturnResolutionService",
        "public class GraveyardReturnSupport",
    )
    support = re.sub(
        r"/\*\*[\s\S]*?Resolves all graveyard-related effects[\s\S]*?\*/\r?\n",
        """/**
 * Shared graveyard return/exile helpers used by every normal Graveyard Return effect handler
 * and by input handlers (graveyard choice, may ability pile separation).
 *
 * <p>Extracted verbatim from {@code GraveyardReturnResolutionService}; behavior is identical.
 */
""",
        support,
        count=1,
    )
    support = strip_handles_effect_methods(support)
    # Remove orphaned javadoc left when @HandlesEffect methods were stripped
    support = re.sub(
        r"    /\*\*[\s\S]*?\*/\r?\n\r?\n(?=    public void )",
        "",
        support,
    )
    support = support.replace("    private void ", "    public void ")
    support = support.replace("    private boolean ", "    public boolean ")
    support = support.replace("    private record ", "    public record ")
    support = support.replace("    private StolenCreatureResult ", "    public StolenCreatureResult ")
    support = support.replace("    void beginGraveyardExileChoice", "    public void beginGraveyardExileChoice")
    return support


def parse_imports(content: str) -> dict[str, str]:
    imports: dict[str, str] = {}
    for line in content.splitlines():
        if not line.startswith("import "):
            continue
        stmt = line.strip().removesuffix(";")
        simple = stmt.rsplit(".", 1)[-1]
        if simple != "*":
            imports[simple] = line.strip()
    return imports


def imports_for_body(body: str, source_imports: dict[str, str]) -> list[str]:
    needed: list[str] = []
    for simple, line in sorted(source_imports.items()):
        if simple in {"HandlesEffect", "Service", "GraveyardReturnResolutionService"}:
            continue
        if re.search(rf"\b{re.escape(simple)}\b", body):
            needed.append(line)
    extras = {
        "UUID": "import java.util.UUID;",
        "List": "import java.util.List;",
        "ArrayList": "import java.util.ArrayList;",
        "HashMap": "import java.util.HashMap;",
        "HashSet": "import java.util.HashSet;",
        "LinkedHashSet": "import java.util.LinkedHashSet;",
        "Map": "import java.util.Map;",
        "Set": "import java.util.Set;",
        "EnumSet": "import java.util.EnumSet;",
        "IntStream": "import java.util.stream.IntStream;",
    }
    for simple, line in extras.items():
        if re.search(rf"\b{re.escape(simple)}\b", body) and line not in needed:
            needed.append(line)
    return needed


def prefix_helper_calls(body: str) -> str:
    for hm in HELPER_METHODS:
        body = re.sub(rf"(?<![.\w]){re.escape(hm)}\(", f"graveyardReturnSupport.{hm}(", body)
    return body


def used_fields(body: str) -> list[str]:
    found = []
    for f in FIELD_NAMES:
        if f in body:
            found.append(f)
    if "graveyardReturnSupport" in body:
        found.append("graveyardReturnSupport")
    # preserve order, unique
    seen = set()
    out = []
    for f in found:
        if f not in seen:
            seen.add(f)
            out.append(f)
    return out


def build_handler(effect_class: str, params: str, body: str, source_imports: dict[str, str]) -> str:
    body = prefix_helper_calls(body)
    fields = used_fields(body)

    effect_param_match = re.search(r"(\w+Effect) (\w+)", params)
    if effect_param_match:
        effect_param = effect_param_match.group(2)
        body = re.sub(rf"\b{re.escape(effect_param)}\b", "e", body)
        body = f"        var e = ({effect_class}) effect;\n{body}"

    deps_decl = "\n".join(
        f"    private final {TYPE_MAP.get(f, f)} {f};" for f in fields
    )

    imports = [
        "import com.github.laxika.magicalvibes.model.GameData;",
        "import com.github.laxika.magicalvibes.model.StackEntry;",
        "import com.github.laxika.magicalvibes.model.effect.CardEffect;",
        f"import com.github.laxika.magicalvibes.model.effect.{effect_class};",
    ]
    imports.extend(imports_for_body(body, source_imports))
    for f in fields:
        if f in IMPORT_MAP and IMPORT_MAP[f] not in imports:
            imports.append(IMPORT_MAP[f])
    imports = sorted(dict.fromkeys(imports))

    uses_log = re.search(r"\blog\.", body) is not None
    lombok_imports = ["import lombok.RequiredArgsConstructor;"]
    if uses_log:
        lombok_imports.insert(0, "import lombok.extern.slf4j.Slf4j;")
    slf4j = "@Slf4j\n" if uses_log else ""

    handler_class = effect_class + "Handler"
    return f"""package com.github.laxika.magicalvibes.service.effect.normalfx;

{chr(10).join(imports)}

{chr(10).join(lombok_imports)}
import org.springframework.stereotype.Component;

{slf4j}@Component
@RequiredArgsConstructor
public class {handler_class} implements NormalEffectHandlerBean {{

{deps_decl}

    @Override
    public Class<? extends CardEffect> handledEffect() {{
        return {effect_class}.class;
    }}

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {{
{body}    }}
}}
"""


def main() -> None:
    content = SRC.read_text(encoding="utf-8")
    source_imports = parse_imports(content)
    support = rewrite_support(content)
    (NORMALFX / "GraveyardReturnSupport.java").write_text(support, encoding="utf-8", newline="\n")
    print("Wrote GraveyardReturnSupport.java")

    handlers = list(HANDLER_PATTERN.finditer(content))
    print(f"Found {len(handlers)} handlers")
    for m in handlers:
        effect_class = m.group(2)
        params = m.group(3).strip()
        body = m.group(4)
        handler_src = build_handler(effect_class, params, body, source_imports)
        path = NORMALFX / f"{effect_class}Handler.java"
        path.write_text(handler_src, encoding="utf-8", newline="\n")
        print(f"Wrote {path.name}")


if __name__ == "__main__":
    main()
