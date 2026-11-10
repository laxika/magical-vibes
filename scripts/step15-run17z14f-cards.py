#!/usr/bin/env python3
"""Step 15, run 17z14f: the cards whose abilities `CombatBlockService`'s block-step collectors read differently now
that each ability goes on the stack whole (row 17z14f; row 17z14h's list, `step15-run17z14-scan.py`).

The block-step collectors put each triggered ability on the stack as one object of its effects (CR 603.3), no longer
one object of every ability of a watch, so an ability split into several `addTrigger` calls is several objects. Each
card under `EDITS` was read against its Oracle text (Scryfall MCP) and prints one ability there; its calls become one
`addTrigger`, its effects in the order the Oracle text has them.

`BUSHIDO`: bushido is "whenever this creature blocks or becomes blocked, it gets +N/+N until end of turn"
(CR 702.45a), which triggers once each combat however many creatures the creature blocks (CR 509.3a): its "blocks"
half is `TriggerOptions.ONE_OR_MORE`, as three cards already wrote it. The block collector kept the other cards'
second bushido trigger of one declaration off the stack by the effect's class (`BushidoEffect`); it now reads the
multiplicity from the ability, so every bushido card and Sensei Golden-Tail's grant say it. The "becomes blocked"
half triggers once each combat as written (CR 509.3c) and stays a call of its own.

Usage: step15-run17z14f-cards.py [LETTER...] (run per letter module; no letter means all). Fails if an edit didn't
apply; idempotent.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent / "magical-vibes-card"
PKG = "src/main/java/com/github/laxika/magicalvibes/cards"

# card -> [(old, new)]
EDITS = {
    # "Whenever this creature attacks and isn't blocked, it assigns no combat damage this turn and defending player
    # loses 2 life." One ability; the unblocked collector joined the two calls into one object.
    "KeeperOfTresserhorn": [(
        """        addTrigger(Triggers.attacksUnblocked(), new AssignNoCombatDamageEffect());
        addTrigger(Triggers.attacksUnblocked(), new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER));""",
        """        addTrigger(Triggers.attacksUnblocked(), new AssignNoCombatDamageEffect(),
                new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER));""")],
    # "Whenever this creature attacks and isn't blocked, it assigns no combat damage this turn and defending player
    # loses 4 life." One ability, as Keeper of Tresserhorn's.
    "LimDLsPaladin": [(
        """        addTrigger(Triggers.attacksUnblocked(), new AssignNoCombatDamageEffect());
        addTrigger(Triggers.attacksUnblocked(), new LoseLifeEffect(4, LoseLifeRecipient.TARGET_PLAYER));""",
        """        addTrigger(Triggers.attacksUnblocked(), new AssignNoCombatDamageEffect(),
                new LoseLifeEffect(4, LoseLifeRecipient.TARGET_PLAYER));""")],
    # "Whenever this creature attacks a player and isn't blocked, that player's life total becomes 1. This creature
    # assigns no combat damage this combat." One ability of two sentences.
    "MasterOfCruelties": [(
        """        addTrigger(Triggers.attacksUnblocked(),
                TriggerOptions.DEFAULT.withEventCondition(new SourceAttackingPlayer()),
                new SetLifeTotalEffect(1, SetLifeTotalRecipient.TARGET_PLAYER));
        addTrigger(Triggers.attacksUnblocked(),
                TriggerOptions.DEFAULT.withEventCondition(new SourceAttackingPlayer()),
                AssignNoCombatDamageEffect.thisCombat());""",
        """        addTrigger(Triggers.attacksUnblocked(),
                TriggerOptions.DEFAULT.withEventCondition(new SourceAttackingPlayer()),
                new SetLifeTotalEffect(1, SetLifeTotalRecipient.TARGET_PLAYER),
                AssignNoCombatDamageEffect.thisCombat());""")],
}

# The cards with bushido (each printed "Bushido N" or, Fumiko the Lowblood, "Bushido X"; Sensei Golden-Tail grants
# bushido 1).
BUSHIDO = [
    "BattleMadRonin", "BrothersYamazaki", "DevotedRetainer", "FumikoTheLowblood", "HandOfCruelty", "HandOfHonor",
    "IizukaTheRuthless", "IndebtedSamurai", "InnerChamberGuard", "JadeAvenger", "KentaroTheSmilingCat",
    "KenzoTheHardhearted", "KitsuneBlademaster", "KitsuneDawnblade", "KondaLordOfEiganjo", "KondasHatamoto",
    "KurosTaken", "MothriderSamurai", "NagaoBoundByHonor", "NezumiRonin", "NumaiOutcast", "OpalEyeKondasYojimbo",
    "RoninCavekeeper", "RoninCliffrider", "RoninHoundmaster", "SamuraiEnforcers", "SamuraiOfThePaleCurtain",
    "SenseiGoldenTail", "SilverstormSamurai", "SokenzanRenegade", "SokenzanSpellblade", "TakenoSamuraiGeneral",
    "TakenosCavalry", "ToshiroUmezawa",
]
BLOCKS_BUSHIDO = re.compile(r"Triggers\.blocks\(\),(\s+)new BushidoEffect\(")
OPTIONS_IMPORT = "import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;\n"
TRIGGERS_IMPORT = "import com.github.laxika.magicalvibes.model.trigger.Triggers;\n"


def find(name):
    paths = list(ROOT.glob(f"*/{PKG}/*/{name}.java"))
    if len(paths) != 1:
        raise SystemExit(f"{name}: found {len(paths)} files")
    return paths[0].relative_to(ROOT).parts[0], paths[0]


def bushido(name, text):
    """`text` with the "blocks" half of its bushido written once each combat, and whether it changed."""
    edited = BLOCKS_BUSHIDO.sub(r"Triggers.blocks(), TriggerOptions.ONE_OR_MORE,\1new BushidoEffect(", text)
    if "Triggers.blocks(), TriggerOptions.ONE_OR_MORE," not in edited or BLOCKS_BUSHIDO.search(edited):
        raise SystemExit(f"{name}: bushido edit didn't apply")
    if OPTIONS_IMPORT not in edited:
        if edited.count(TRIGGERS_IMPORT) != 1:
            raise SystemExit(f"{name}: no place for the TriggerOptions import")
        edited = edited.replace(TRIGGERS_IMPORT, OPTIONS_IMPORT + TRIGGERS_IMPORT)
    return edited, edited != text


def main():
    letters = set(sys.argv[1:])
    for name, edits in EDITS.items():
        module, path = find(name)
        if letters and module not in letters:
            continue
        text = path.read_text()
        changed = False
        for old, new in edits:
            if new in text:
                continue
            if text.count(old) != 1:
                raise SystemExit(f"{name}: edit didn't apply")
            text = text.replace(old, new)
            changed = True
        if changed:
            path.write_text(text)
            print(f"edited {module}/{name}")
    for name in BUSHIDO:
        module, path = find(name)
        if letters and module not in letters:
            continue
        text, changed = bushido(name, path.read_text())
        if changed:
            path.write_text(text)
            print(f"edited {module}/{name}")
    listed = set(BUSHIDO) | {"ArabaMothrider", "CursedRonin", "IsaoEnlightenedBushi"}
    for path in ROOT.glob(f"*/{PKG}/*/*.java"):
        if "new BushidoEffect(" in path.read_text() and path.stem not in listed:
            raise SystemExit(f"{path.stem}: a bushido card this script doesn't list")


if __name__ == "__main__":
    main()
