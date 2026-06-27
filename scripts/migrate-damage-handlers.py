#!/usr/bin/env python3
"""Generate normalfx damage handlers from DamageResolutionService.java."""

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SRC = ROOT / "magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/combat/DamageResolutionService.java"
OUT = ROOT / "magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/effect/normalfx"

PACKAGE = "com.github.laxika.magicalvibes.service.effect.normalfx"

DAMAGE_SUPPORT_METHODS = [
    "dealCreatureDamage",
    "dealCreatureDamageUnpreventable",
    "destroyPermanent",
    "destroyAllLethal",
    "dealDamageAndDestroyIfLethal",
    "dealDamageAndDestroyIfLethalUnpreventable",
    "isDamageSourcePreventedWithLog",
    "resolveCreatureTargetDamage",
    "isDamagePreventedForCreature",
    "isSourcePermanentPreventedFromDealingDamage",
    "resolveAnyTargetDamage",
    "damageAllCreaturesOnBattlefield",
    "damageFilteredCreatures",
    "dealDamageToPlayer",
    "dealDividedDamageToAnyTargets",
    "countPermanentsAttachedToPlayer",
]

SERVICE_FIELDS = {
    "gameQueryService": "GameQueryService",
    "gameBroadcastService": "GameBroadcastService",
    "gameOutcomeService": "GameOutcomeService",
    "graveyardService": "GraveyardService",
    "permanentRemovalService": "PermanentRemovalService",
    "lifeSupport": "LifeSupport",
    "damageSupport": "DamageSupport",
}


def extract_method_body(content: str, start_pos: int) -> tuple[str, int]:
    """Extract method body from opening brace after signature."""
    brace = content.index("{", start_pos)
    depth = 0
    i = brace
    while i < len(content):
        c = content[i]
        if c == "{":
            depth += 1
        elif c == "}":
            depth -= 1
            if depth == 0:
                return content[brace + 1 : i], i + 1
        i += 1
    raise ValueError("Unbalanced braces")


def transform_body(body: str) -> tuple[str, set[str]]:
    deps: set[str] = set()

    for field in SERVICE_FIELDS:
        if re.search(rf"\b{field}\.", body):
            deps.add(field)

    for method in DAMAGE_SUPPORT_METHODS:
        # Replace bare calls (not already qualified)
        body = re.sub(
            rf"(?<![.\w]){method}\(",
            f"damageSupport.{method}(",
            body,
        )
        if f"damageSupport.{method}(" in body:
            deps.add("damageSupport")

    return body, deps


def parse_handlers(content: str) -> list[dict]:
    pattern = re.compile(
        r"    @HandlesEffect\((\w+)\.class\)\n    void (\w+)\(([^)]*)\) \{",
        re.MULTILINE,
    )
    handlers = []
    for m in pattern.finditer(content):
        effect_class = m.group(1)
        method_name = m.group(2)
        params = m.group(3).strip()
        body, end = extract_method_body(content, m.end() - 1)
        body, deps = transform_body(body)
        handlers.append(
            {
                "effect_class": effect_class,
                "handler_class": effect_class + "Handler",
                "method_name": method_name,
                "params": params,
                "body": body,
                "deps": deps,
            }
        )
    return handlers


def effect_import(effect_class: str) -> str:
    return f"import com.github.laxika.magicalvibes.model.effect.{effect_class};"


def service_imports(deps: set[str]) -> list[str]:
    imports = []
    mapping = {
        "gameQueryService": "com.github.laxika.magicalvibes.service.battlefield.GameQueryService",
        "gameBroadcastService": "com.github.laxika.magicalvibes.service.GameBroadcastService",
        "gameOutcomeService": "com.github.laxika.magicalvibes.service.GameOutcomeService",
        "graveyardService": "com.github.laxika.magicalvibes.service.graveyard.GraveyardService",
        "permanentRemovalService": "com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService",
        "lifeSupport": "com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport",
        "damageSupport": "com.github.laxika.magicalvibes.service.effect.normalfx.DamageSupport",
    }
    for dep in sorted(deps):
        if dep == "damageSupport":
            continue  # same package
        imports.append(f"import {mapping[dep]};")
    return imports


def build_resolve_params(params: str) -> str:
    if not params:
        return "GameData gameData, StackEntry entry, CardEffect effect"
  # has effect param already
    if "CardEffect" in params or "Effect" in params.split(",")[-1]:
        return params
    return params


def build_resolve_body(handler: dict) -> str:
    params = handler["params"]
    effect_class = handler["effect_class"]
    body = handler["body"]

    # NormalEffectHandlerBean.resolve always receives CardEffect — cast when the original method used a typed effect.
    if f"{effect_class} effect" in params:
        var = "e"
        cast = f"        var {var} = ({effect_class}) effect;\n"
        body = re.sub(r"\beffect\.", f"{var}.", body)
        body = re.sub(r"\beffect\)", f"{var})", body)
        body = re.sub(r"\beffect,", f"{var},", body)
        body = re.sub(r"\(effect\.", f"({var}.", body)
        return cast + body

    return body


def generate_handler(handler: dict) -> str:
    deps = handler["deps"]
    effect_class = handler["effect_class"]
    handler_class = handler["handler_class"]
    body = build_resolve_body(handler)

    imports = [
        "package com.github.laxika.magicalvibes.service.effect.normalfx;",
        "",
        "import com.github.laxika.magicalvibes.model.GameData;",
        "import com.github.laxika.magicalvibes.model.StackEntry;",
        "import com.github.laxika.magicalvibes.model.effect.CardEffect;",
        effect_import(effect_class),
    ]
    imports.extend(service_imports(deps))

    # Add common imports based on body content
    extra_imports = []
    if "UUID" in body:
        extra_imports.append("import java.util.UUID;")
    if "List<" in body or "List.of" in body or "new ArrayList" in body:
        extra_imports.append("import java.util.ArrayList;")
        extra_imports.append("import java.util.List;")
    if "Map<" in body or "Map.of" in body:
        extra_imports.append("import java.util.Map;")
    if "Predicate<" in body:
        extra_imports.append("import java.util.function.Predicate;")
    if "Permanent" in body:
        extra_imports.append("import com.github.laxika.magicalvibes.model.Permanent;")
    if "Card" in body:
        extra_imports.append("import com.github.laxika.magicalvibes.model.Card;")
    if "CardType" in body:
        extra_imports.append("import com.github.laxika.magicalvibes.model.CardType;")
    if "CardSubtype" in body:
        extra_imports.append("import com.github.laxika.magicalvibes.model.CardSubtype;")
    if "CardColor" in body:
        extra_imports.append("import com.github.laxika.magicalvibes.model.CardColor;")
    if "Keyword" in body:
        extra_imports.append("import com.github.laxika.magicalvibes.model.Keyword;")
    if "CounterType" in body:
        extra_imports.append("import com.github.laxika.magicalvibes.model.CounterType;")
    if "StackEntryType" in body:
        extra_imports.append("import com.github.laxika.magicalvibes.model.StackEntryType;")
    if "FilterContext" in body:
        extra_imports.append("import com.github.laxika.magicalvibes.model.filter.FilterContext;")
    if "PermanentPredicate" in body:
        extra_imports.append("import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;")
    if "ConcurrentHashMap" in body:
        extra_imports.append("import java.util.concurrent.ConcurrentHashMap;")
    if "Collection<" in body:
        extra_imports.append("import java.util.Collection;")

    imports.extend(extra_imports)
    imports.append("import lombok.RequiredArgsConstructor;")
    if "log." in body:
        imports.append("import lombok.extern.slf4j.Slf4j;")
    imports.append("import org.springframework.stereotype.Component;")

    # dedupe imports preserving order
    seen = set()
    unique_imports = []
    for line in imports:
        if line.startswith("import ") and line in seen:
            continue
        if line.startswith("import "):
            seen.add(line)
        unique_imports.append(line)

    fields = []
    ctor_params = []
    for dep in ["damageSupport", "gameQueryService", "gameBroadcastService", "gameOutcomeService",
                "graveyardService", "permanentRemovalService", "lifeSupport"]:
        if dep in deps:
            fields.append(f"    private final {SERVICE_FIELDS[dep]} {dep};")
            ctor_params.append(dep)

    fields_block = "\n".join(fields)

    slf4j = "@Slf4j\n" if "log." in body else ""

    return f"""{chr(10).join(unique_imports)}

{slf4j}@Component
@RequiredArgsConstructor
public class {handler_class} implements NormalEffectHandlerBean {{

{fields_block}

    @Override
    public Class<? extends CardEffect> handledEffect() {{
        return {effect_class}.class;
    }}

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {{
{body}
    }}
}}
"""


def generate_damage_support(content: str) -> str:
    helper_start = content.index("    /**\n     * Applies damage to a creature")
    helper_end = content.index("    /**\n     * Resolves {@link FirstTargetDealsPowerDamageToSecondTargetEffect}")
    helpers = content[helper_start:helper_end]

    div_start = content.index("    /**\n     * Deals divided damage to any number of targets")
    div_end = content.index("    /**\n     * Resolves {@link SacrificeSelfAndDealDamageToDamagedPlayerEffect}")
    deal_divided = content[div_start:div_end]

    count_start = content.index("    /**\n     * Counts the permanents currently attached")
    count_end = content.index("    /**\n     * Resolves {@link EnchantedCreatureDealsDamageToItsOwnerEffect}")
    count_method = content[count_start:count_end]

    combined = helpers + "\n" + deal_divided + "\n" + count_method

    # Make methods public, fix visibility
    combined = re.sub(r"\n    private ", "\n    public ", combined)
    combined = re.sub(r"\n    void process", "\n    public void process", combined)

    header = '''package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.DamageRedirectShield;
import com.github.laxika.magicalvibes.model.SourceDamageRedirectShield;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.CounterType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Predicate;

/**
 * Shared damage helpers used by every "normal" Damage effect handler and by other services
 * (input handlers, combat). Extracted verbatim from {@code DamageResolutionService}; behavior
 * (routing, prevention, lethal-damage deferral, trigger order) is identical.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DamageSupport {

    private final GraveyardService graveyardService;
    private final DamagePreventionService damagePreventionService;
    private final GameOutcomeService gameOutcomeService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;
    private final LifeSupport lifeSupport;

'''

    return header + combined + "\n}\n"


def generate_factory_entries(handlers: list[dict]) -> str:
    lines = []
    for h in handlers:
        deps = h["deps"]
        ctor_args = []
        for dep in ["damageSupport", "gameQueryService", "gameBroadcastService", "gameOutcomeService",
                    "graveyardService", "permanentRemovalService", "lifeSupport"]:
            if dep in deps:
                ctor_args.append(dep)
        args = ", ".join(ctor_args)
        lines.append(f"                new {h['handler_class']}({args}),")
    return "\n".join(lines)


def main():
    content = SRC.read_text(encoding="utf-8")
    handlers = parse_handlers(content)
    print(f"Generating {len(handlers)} handlers...")

    OUT.mkdir(parents=True, exist_ok=True)

    # DamageSupport
    support_path = OUT / "DamageSupport.java"
    support_path.write_text(generate_damage_support(content), encoding="utf-8")
    print(f"Wrote {support_path.name}")

    for h in handlers:
        path = OUT / f"{h['handler_class']}.java"
        path.write_text(generate_handler(h), encoding="utf-8")

    factory_entries = generate_factory_entries(handlers)
    entries_path = ROOT / "scripts/damage-factory-entries.txt"
    entries_path.write_text(factory_entries, encoding="utf-8")
    print(f"Wrote factory entries to {entries_path}")
    print("Done.")


if __name__ == "__main__":
    main()
