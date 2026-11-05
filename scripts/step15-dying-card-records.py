#!/usr/bin/env python3
"""Step 15 run 1: the dying-card effects whose only component is the bound card id lose it.

Each record `X(UUID id) implements CardEffect, DyingCreatureCardAwareEffect` with a no-arg
constructor `this(null)` and a `boundToDyingCard` override becomes `X() implements CardEffect`:
its handler reads the triggering card from the stack entry. Rerunnable: a converted file has no
`DyingCreatureCardAwareEffect` left and is skipped.
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
EFFECTS = ROOT / "magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/effect"

SIMPLE = [
    "ExileDyingCreatureAndCreateSpiritTokenCopyEffect",
    "ExileDyingInstantOrSorceryAndMayCastUntilNextTurnEffect",
    "ExileTriggeringCreatureAndTrackWithSourceEffect",
    "ExileTriggeringCreatureFromGraveyardEffect",
    "ExileTriggeringCreatureWithHitCounterEffect",
    "ImprintDyingCreatureEffect",
    "PutTriggeringCardFromGraveyardOnBottomOfLibraryEffect",
    "PutTriggeringCardFromGraveyardOnTopOfLibraryEffect",
    "ReturnDyingCreatureToBattlefieldFaceDownAsCybermanEffect",
    "ReturnDyingCreatureToOwnerBattlefieldFaceDownThenTurnFaceUpEffect",
    "ReturnDyingOpponentCreatureAsTreasureEffect",
    "ReturnOtherCreatureCardsWithTotalPowerFromGraveyardToBattlefieldEffect",
    "ReturnTriggeringCreatureToBattlefieldEffect",
    "SuspectReturnedPermanentEffect",
    "TheMasterGallifreysEndEffect",
]


def convert(name: str) -> bool:
    path = EFFECTS / f"{name}.java"
    text = path.read_text()
    if "DyingCreatureCardAwareEffect" not in text:
        return False
    text = re.sub(rf"public record {name}\(UUID \w+\)\s*\n\s*implements CardEffect, DyingCreatureCardAwareEffect \{{",
                  f"public record {name}() implements CardEffect {{", text)
    text = re.sub(rf"\n    public {name}\(\) \{{\n        this\(null\);\n    \}}\n", "\n", text)
    text = re.sub(r"\n    @Override\n    public CardEffect boundToDyingCard\(UUID \w+\) \{\n[^}]*\}\n", "\n", text)
    text = re.sub(r"\n *\* @param \w+ [^@]*?(?=\n \*/)", "", text, flags=re.S)
    text = re.sub(r"\{\n\s*\n+", "{\n", text)
    text = re.sub(r"\n\s*\n+\}", "\n}", text)
    text = re.sub(r"\{\n\}\n?$", "{\n}\n", text)
    if "UUID" not in text.split("import java.util.UUID;", 1)[-1]:
        text = text.replace("import java.util.UUID;\n", "")
    text = re.sub(r"\n{3,}", "\n\n", text)
    if "DyingCreatureCardAwareEffect" in text or "boundToDyingCard" in text:
        sys.exit(f"{name}: not fully converted")
    path.write_text(text)
    return True


for record in SIMPLE:
    print(("converted " if convert(record) else "skipped   ") + record)
