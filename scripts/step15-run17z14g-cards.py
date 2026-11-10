#!/usr/bin/env python3
"""Step 15, run 17z14g: the cards whose abilities `CombatDamageService`'s combat-damage collectors read differently now
that each ability goes on the stack whole (row 17z14g; row 17z14h's list, `step15-run17z14-scan.py`).

The combat-damage collectors put each triggered ability on the stack as one object of its effects (CR 603.3): the
attached collector no longer joins every ability of an Aura or Equipment, the graveyard "dealt combat damage" collector
no longer joins a card's abilities, and "deals combat damage to a player" no longer splits per effect, so an ability
split into several `addTrigger` calls is several objects. Each card under `EDITS` was read against its Oracle text
(Scryfall MCP) and prints one ability there; its calls become one `addTrigger`, its effects in the order the Oracle
text has them, an effect bound to its own target group where the ability has several or an untargeted effect beside
a targeted one (`target(...).bindEffect(...)`).

The class-keyed drops the collector made are what the cards say:

- `RENOWN`: renown is "when this creature deals combat damage to a player, if it isn't renowned, put N +1/+1 counters
  on it and it becomes renowned" (CR 702.112a), an intervening "if" (CR 603.4, CR 702.112c). The collector dropped a
  `RenownEffect` of a renowned creature by its class; every renown card and Aragorn, Hornburg Hero's grant write
  `TriggerOptions.DEFAULT.withInterveningIf(new NotCondition(new SourceIsRenowned()))`.
- Semblance Scanner: "whenever this or equipped creature deals combat damage to a player, if it's not a token, ...".
  The collector dropped its effect for a token by its class; the card writes the intervening "if" on the creature that
  dealt the damage, the event's triggering permanent.
- Optimus Prime, Autobot Leader: "when that creature deals combat damage to a player this turn, convert Optimus Prime"
  is a delayed triggered ability whose source is Optimus Prime (CR 603.7, CR 603.7e), not an ability of the chosen
  creature. The collector told it from the creature's abilities by its effect's class
  (`TransformGrantingPermanentEffect`: no additional triggers); the card registers the delayed ability
  (`RegisterDelayedWatchedCreaturesCombatDamageEffect.forChosenCreatureThisTurn`).

Usage: step15-run17z14g-cards.py [LETTER...] (run per letter module; no letter means all). Fails if an edit didn't
apply or a renown card isn't listed; idempotent.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent / "magical-vibes-card"
PKG = "src/main/java/com/github/laxika/magicalvibes/cards"
MODEL = "com.github.laxika.magicalvibes.model."

# card -> ([(old, new)], imports to add, imports to remove)
EDITS = {
    # "Whenever equipped creature deals combat damage to a player, draw two cards. Then discard two cards unless you
    # discard a Pirate card." One ability; the attached collector joined the two calls into one object.
    "ArmMountedAnchor": ([(
        """        addTrigger(Triggers.dealsCombatDamageToPlayer(), new DrawCardEffect(2));
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new DiscardTwoUnlessCardPredicateEffect(new CardSubtypePredicate(CardSubtype.PIRATE)));""",
        """        addTrigger(Triggers.dealsCombatDamageToPlayer(), new DrawCardEffect(2),
                new DiscardTwoUnlessCardPredicateEffect(new CardSubtypePredicate(CardSubtype.PIRATE)));""")], [], []),
    # "Whenever Bumi deals combat damage to a player, untap all lands you control. After this phase, there is an
    # additional combat phase. Only land creatures can attack during that combat phase." One ability; the two calls
    # went on the stack as two objects.
    "BumiUnleashed": ([(
        """        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsLandPredicate()));
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new AdditionalCombatPhaseEffect(1, new OnlyLandCreaturesCanAttackThisCombatEffect()));""",
        """        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsLandPredicate()),
                new AdditionalCombatPhaseEffect(1, new OnlyLandCreaturesCanAttackThisCombatEffect()));""")], [], []),
    # "Whenever equipped creature deals combat damage to a player, draw a card, then you may cast a spell from your
    # hand with mana value less than or equal to that damage without paying its mana cost." One ability.
    "BusterSword": ([(
        """        addTrigger(Triggers.dealsCombatDamageToPlayer(), new DrawCardEffect(1));
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayCastAnySpellFromHandWithoutPayingManaCostEffect(new EventValue()));""",
        """        addTrigger(Triggers.dealsCombatDamageToPlayer(), new DrawCardEffect(1),
                new MayCastAnySpellFromHandWithoutPayingManaCostEffect(new EventValue()));""")], [], []),
    # "Whenever Niv-Mizzet deals combat damage to a player, it deals X damage to any target, target player draws X
    # cards, and you gain X life, where X is ...". One ability of two target groups; the three calls went on the
    # stack as three objects, each determining X for itself.
    "NivMizzetGuildpact": ([(
        """        target(anyTarget).addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new DealDamageToAnyTargetEffect(colorPairs));
        target(anyPlayer).addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new DrawCardForTargetPlayerEffect(colorPairs, false, true));
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new GainLifeEffect(colorPairs));""",
        """        CardEffect damage = new DealDamageToAnyTargetEffect(colorPairs);
        CardEffect draw = new DrawCardForTargetPlayerEffect(colorPairs, false, true);
        addTrigger(Triggers.dealsCombatDamageToPlayer(), damage, draw, new GainLifeEffect(colorPairs));
        target(anyTarget).bindEffect(damage);
        target(anyPlayer).bindEffect(draw);""")], ["effect.CardEffect"], []),
    # "Whenever equipped creature deals combat damage to a player, you create a 2/2 green Wolf creature token and that
    # player mills ten cards." One ability.
    "SwordOfBodyAndMind": ([(
        """        // Triggered: whenever equipped creature deals combat damage to a player,
        // you create a 2/2 green Wolf creature token
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new CreateTokenEffect(1, "Wolf", 2, 2,
                        CardColor.GREEN, List.of(CardSubtype.WOLF),
                        Set.of(), Set.of()));

        // Triggered: ...and that player mills ten cards
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new MillEffect(10, MillRecipient.TARGET_PLAYER));""",
        """        // Triggered: whenever equipped creature deals combat damage to a player,
        // you create a 2/2 green Wolf creature token and that player mills ten cards
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new CreateTokenEffect(1, "Wolf", 2, 2,
                        CardColor.GREEN, List.of(CardSubtype.WOLF),
                        Set.of(), Set.of()),
                new MillEffect(10, MillRecipient.TARGET_PLAYER));""")], [], []),
    # "Whenever equipped creature deals combat damage to a player, that player discards a card and you untap all
    # lands you control." One ability.
    "SwordOfFeastAndFamine": ([(
        """        // Triggered: whenever equipped creature deals combat damage to a player,
        // that player discards a card
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER));

        // Triggered: ...and you untap all lands you control
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsLandPredicate()));""",
        """        // Triggered: whenever equipped creature deals combat damage to a player,
        // that player discards a card and you untap all lands you control
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER),
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsLandPredicate()));""")], [], []),
    # "Whenever equipped creature deals combat damage to a player, exile the top two cards of your library. You may
    # play those cards this turn. You may play an additional land this turn." One ability.
    "SwordOfForgeAndFrontier": ([(
        """        addTrigger(Triggers.dealsCombatDamageToPlayer(), new ExileTopCardMayPlayThisTurnEffect(2, false));
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new PlayAdditionalLandsEffect(1));""",
        """        addTrigger(Triggers.dealsCombatDamageToPlayer(), new ExileTopCardMayPlayThisTurnEffect(2, false),
                new PlayAdditionalLandsEffect(1));""")], [], []),
    # "Whenever equipped creature deals combat damage to a player, destroy up to one target planeswalker and up to
    # one target artifact." One ability of two target groups.
    "SwordOfSinewAndSteel": ([(
        """        target(new PermanentPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(), "Target must be a planeswalker"), 0, 1)
                .addTrigger(Triggers.dealsCombatDamageToPlayer(), new DestroyTargetPermanentEffect());
        target(TargetFilters.artifact(), 0, 1)
                .addTrigger(Triggers.dealsCombatDamageToPlayer(), new DestroyTargetPermanentEffect());""",
        """        CardEffect destroyPlaneswalker = new DestroyTargetPermanentEffect();
        CardEffect destroyArtifact = new DestroyTargetPermanentEffect();
        addTrigger(Triggers.dealsCombatDamageToPlayer(), destroyPlaneswalker, destroyArtifact);
        target(new PermanentPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(), "Target must be a planeswalker"), 0, 1)
                .bindEffect(destroyPlaneswalker);
        target(TargetFilters.artifact(), 0, 1).bindEffect(destroyArtifact);""")], ["effect.CardEffect"], []),
    # "Whenever equipped creature deals combat damage to a player, this Equipment deals damage to that player equal to
    # the number of cards in their hand and you gain 1 life for each card in your hand." One ability.
    "SwordOfWarAndPeace": ([(
        """        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new DealDamageToPlayersEffect(new CardsInHand(CountScope.TARGET_PLAYER), DamageRecipient.TARGET_PLAYER));

        // Triggered: ...and you gain 1 life for each card in your hand
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new GainLifeEffect(new CardsInHand(CountScope.CONTROLLER)));""",
        """        // and you gain 1 life for each card in your hand
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new DealDamageToPlayersEffect(new CardsInHand(CountScope.TARGET_PLAYER), DamageRecipient.TARGET_PLAYER),
                new GainLifeEffect(new CardsInHand(CountScope.CONTROLLER)));""")], [], []),
    # "Whenever equipped creature deals combat damage to a player, create a Treasure token. When you next cast an
    # instant or sorcery spell this turn, copy that spell. You may choose new targets for the copy." One ability.
    "SwordOfWealthAndPower": ([(
        """        addTrigger(Triggers.dealsCombatDamageToPlayer(), CreateTokenEffect.ofTreasureToken(1));
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new CopyNextInstantOrSorceryCastThisTurnEffect());""",
        """        addTrigger(Triggers.dealsCombatDamageToPlayer(), CreateTokenEffect.ofTreasureToken(1),
                new CopyNextInstantOrSorceryCastThisTurnEffect());""")], [], []),
    # "Whenever combat damage is dealt to you or a planeswalker you control, if this card is in your graveyard,
    # destroy target attacking creature, then put this card on top of your library." One ability; the graveyard
    # collector joined the two calls into one target choice.
    "VengefulPharaoh": ([(
        """        target(TargetFilters.attackingCreature())
                .addTrigger(new TriggerCondition(Triggers.youOrYourPlaneswalkerDealtCombatDamage(),
                                AbilityZones.GRAVEYARD),
                        TriggerOptions.DEFAULT.withInterveningIf(new SourceCardInGraveyard()),
                        new DestroyTargetPermanentEffect());
        addTrigger(new TriggerCondition(Triggers.youOrYourPlaneswalkerDealtCombatDamage(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceCardInGraveyard()),
                new PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(0));""",
        """        CardEffect destroy = new DestroyTargetPermanentEffect();
        addTrigger(new TriggerCondition(Triggers.youOrYourPlaneswalkerDealtCombatDamage(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceCardInGraveyard()),
                destroy, new PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(0));
        target(TargetFilters.attackingCreature()).bindEffect(destroy);""")], ["effect.CardEffect"], []),
    # "Whenever Semblance Scanner or equipped creature deals combat damage to a player, if it's not a token, conjure a
    # duplicate of it into your hand." The intervening "if" is the card's, on the creature that dealt the damage.
    "SemblanceScanner": ([(
        """        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new ConjureDuplicateOfCombatDamageSourceIntoHandEffect());""",
        """        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new NotCondition(new TriggeringPermanentMatches(new PermanentIsTokenPredicate()))),
                new ConjureDuplicateOfCombatDamageSourceIntoHandEffect());""")],
        ["condition.NotCondition", "condition.TriggeringPermanentMatches", "filter.PermanentIsTokenPredicate",
         "trigger.TriggerOptions"], []),
    # "Whenever you attack, bolster 2. The chosen creature gains trample until end of turn. When that creature deals
    # combat damage to a player this turn, convert Optimus Prime." The last sentence creates a delayed triggered
    # ability (CR 603.7).
    "OptimusPrimeAutobotLeader": ([(
        """                new GrantEffectToChosenCreatureUntilEndOfTurnEffect(
                        new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(),
                        new TransformGrantingPermanentEffect()))));""",
        """                RegisterDelayedWatchedCreaturesCombatDamageEffect.forChosenCreatureThisTurn(
                        List.of(new TransformGrantingPermanentEffect()))));""")],
        ["effect.RegisterDelayedWatchedCreaturesCombatDamageEffect", "java.util.List"],
        ["effect.GrantEffectToChosenCreatureUntilEndOfTurnEffect", "TriggeredAbility"]),
}

# The cards with renown (each printed "Renown N"; Aragorn, Hornburg Hero grants renown 1).
RENOWN = [
    "AcolyteOfTheInferno", "AkroanSergeant", "AragornHornburgHero", "CitadelCastellan", "ConstableOfTheRealm",
    "ConsulsLieutenant", "FirefiendElemental", "GoblinGloryChaser", "HonoredHierarch", "KnightOfThePilgrimsRoad",
    "KytheonsIrregulars", "OutlandColossus", "PharikasDisciple", "RelicSeeker", "RhoxMaulers", "ScabClanBerserker",
    "StalwartAven", "TopanFreeblade", "UndercityTroll", "ValeronWardens", "WarOracle",
]
IF_NOT_RENOWNED = "TriggerOptions.DEFAULT.withInterveningIf(new NotCondition(new SourceIsRenowned())),"
PRINTED_RENOWN = re.compile(r"( *)addTrigger\(Triggers\.dealsCombatDamageToPlayer\(\), new RenownEffect\((\d+)\)\);")
GRANTED_RENOWN = re.compile(r"new TriggeredAbility\(Triggers\.dealsCombatDamageToPlayer\(\),\n( *)new RenownEffect\(")
RENOWN_IMPORTS = ["condition.NotCondition", "condition.SourceIsRenowned", "trigger.TriggerOptions"]


def find(name):
    paths = list(ROOT.glob(f"*/{PKG}/*/{name}.java"))
    if len(paths) != 1:
        raise SystemExit(f"{name}: found {len(paths)} files")
    return paths[0].relative_to(ROOT).parts[0], paths[0]


def qualified(name):
    return name if name.startswith("java.") else MODEL + name


def add_import(name, text, short):
    """`text` with `import <short>;` among its imports, in order (the `java.` imports are a group of their own)."""
    line = f"import {qualified(short)};\n"
    if line in text:
        return text
    imports = [found for found in re.finditer(r"^import ([\w.]+);\n", text, re.M)]
    group = [found for found in imports if found.group(1).startswith("java.") == short.startswith("java.")]
    if not group:
        if not short.startswith("java."):
            raise SystemExit(f"{name}: no place for the import of {short}")
        return text[:imports[-1].end()] + "\n" + line + text[imports[-1].end():]
    for found in group:
        if found.group(1) > qualified(short):
            return text[:found.start()] + line + text[found.start():]
    return text[:group[-1].end()] + line + text[group[-1].end():]


def remove_import(name, text, short):
    """`text` without `import <short>;`, which nothing in it uses any more."""
    line = f"import {qualified(short)};\n"
    simple = short.rsplit(".", 1)[-1]
    if line not in text:
        return text
    rest = text.replace(line, "")
    if re.search(rf"\b{simple}\b", rest):
        raise SystemExit(f"{name}: {simple} is still used")
    return rest


def renown(name, text):
    """`text` with renown's intervening "if" on its ability, and whether it changed."""
    edited = PRINTED_RENOWN.sub(
        lambda found: (f"{found.group(1)}addTrigger(Triggers.dealsCombatDamageToPlayer(),\n"
                       f"{found.group(1)}        {IF_NOT_RENOWNED}\n"
                       f"{found.group(1)}        new RenownEffect({found.group(2)}));"), text)
    edited = GRANTED_RENOWN.sub(
        lambda found: (f"new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(),\n"
                       f"{found.group(1)}{IF_NOT_RENOWNED}\n{found.group(1)}new RenownEffect("), edited)
    if edited.count(IF_NOT_RENOWNED) != edited.count("new RenownEffect(") or IF_NOT_RENOWNED not in edited:
        raise SystemExit(f"{name}: renown edit didn't apply")
    for short in RENOWN_IMPORTS:
        edited = add_import(name, edited, short)
    return edited, edited != text


def main():
    letters = set(sys.argv[1:])
    for name, (edits, added, removed) in EDITS.items():
        module, path = find(name)
        if letters and module not in letters:
            continue
        original = text = path.read_text()
        for old, new in edits:
            if new in text:
                continue
            if text.count(old) != 1:
                raise SystemExit(f"{name}: edit didn't apply")
            text = text.replace(old, new)
        for short in added:
            text = add_import(name, text, short)
        for short in removed:
            text = remove_import(name, text, short)
        if text != original:
            path.write_text(text)
            print(f"edited {module}/{name}")
    for name in RENOWN:
        module, path = find(name)
        if letters and module not in letters:
            continue
        text, changed = renown(name, path.read_text())
        if changed:
            path.write_text(text)
            print(f"edited {module}/{name}")
    for path in ROOT.glob(f"*/{PKG}/*/*.java"):
        if "new RenownEffect(" in path.read_text() and path.stem not in RENOWN:
            raise SystemExit(f"{path.stem}: a renown card this script doesn't list")


if __name__ == "__main__":
    main()
