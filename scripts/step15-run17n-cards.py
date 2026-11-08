#!/usr/bin/env python3
"""Step 15, run 17n: conditions on which an ability exists are its event condition.

- "As this enters, choose X or Y. • X — [ability]" (the Sieges, Battle of Hoover Dam, Catch of the Day's ward mode,
  Mirrodin Besieged, Phenomenon Investigators, Struggle for Project Purity): the ability exists only under the chosen
  mode, so `SourceHasChosenMode` is its event condition (run 17i's rule), judged as it triggers and not again as it
  resolves, where 14 cards wrote it as an intervening "if" that `TriggerCollectionService.dispatchSlotEffect` judged
  again by its class and unwrapped.

- "Whenever a land an opponent controls enters, if that player …" (Archaeomancer's Map's "controls more lands than you",
  Confounding Conundrum's and Tunnel Ignus's "had another land enter the battlefield under their control this turn")
  is an intervening "if" (CR 603.4) the cards wrote as an effect's `ConditionalEffect`, which the opponent-land collector
  judged by its class; it is the ability's intervening "if", the land-count one with `CountScope.TARGET_PLAYER` ("that
  player"). Invader Parasite's "with the same name as the exiled card" is part of the trigger event: its event
  condition.

Idempotent.

Usage: step15-run17n-cards.py <letter module dir>...
"""
import pathlib
import sys

CHOSEN_MODE_CARDS = set(("BattleOfHooverDam BarrensteppeSiege CatchOfTheDay CitadelSiege FrontierSiege GlacierwoodSiege "
                         "HollowmurkSiege MonasterySiege MirrodinBesieged OutpostSiege PhenomenonInvestigators "
                         "PalaceSiege StruggleForProjectPurity WindcragSiege").split())


LAND_ENTERS = {
    "ArchaeomancersMap": ("""                new ConditionalEffect(
                        new TargetPlayerControlsMoreLandsThanController(),
                        new MayEffect(
                                new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.LAND), "land"),
                                "Put a land card from your hand onto the battlefield?")));""",
                          """                TriggerOptions.DEFAULT.withInterveningIf(new TargetPlayerControlsMoreLandsThanController()),
                new MayEffect(
                        new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.LAND), "land"),
                        "Put a land card from your hand onto the battlefield?"));"""),
    "ConfoundingConundrum": ("""                new ConditionalEffect(
                        new PermanentEnteredThisTurn(new CardTypePredicate(CardType.LAND), 2),
                        new ReturnPermanentControlledByPlayerToHandEffect(
                                new PermanentIsLandPredicate(), "land")));""",
                             """                TriggerOptions.DEFAULT.withInterveningIf(new PermanentEnteredThisTurn(
                        new CardTypePredicate(CardType.LAND), 2, CountScope.TARGET_PLAYER)),
                new ReturnPermanentControlledByPlayerToHandEffect(new PermanentIsLandPredicate(), "land"));"""),
    "TunnelIgnus": ("""                new ConditionalEffect(new PermanentEnteredThisTurn(new CardTypePredicate(CardType.LAND), 2), new DealDamageToPlayersEffect(3, DamageRecipient.TARGET_PLAYER)));""",
                    """                TriggerOptions.DEFAULT.withInterveningIf(new PermanentEnteredThisTurn(
                        new CardTypePredicate(CardType.LAND), 2, CountScope.TARGET_PLAYER)),
                new DealDamageToPlayersEffect(3, DamageRecipient.TARGET_PLAYER));"""),
    "InvaderParasite": ("""                new ConditionalEffect(new ImprintedCardNameMatchesEnteringPermanent(), 
                        new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER)
                ));""",
                        """                TriggerOptions.DEFAULT.withEventCondition(new ImprintedCardNameMatchesEnteringPermanent()),
                new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER));"""),
}
IMPORTS = {"TriggerOptions": "com.github.laxika.magicalvibes.model.trigger.TriggerOptions",
           "CountScope": "com.github.laxika.magicalvibes.model.amount.CountScope"}


def fix_imports(source):
    import re
    for name, qualified in IMPORTS.items():
        if re.search(r"\b%s\b" % name, source.split("public class", 1)[-1]) and f"import {qualified};" not in source:
            first = re.search(r"^import ", source, flags=re.M)
            source = source[:first.start()] + f"import {qualified};\n" + source[first.start():]
    body = source[list(re.finditer(r"^import [\w.]+;\n", source, flags=re.M))[-1].end():]
    for m in list(re.finditer(r"^import (com\.github\.laxika\.magicalvibes\.model\.effect\.ConditionalEffect);\n", source, flags=re.M)):
        if not re.search(r"\bConditionalEffect\b", body):
            source = source.replace(m.group(0), "")
    return source


def convert(name, source):
    if name in LAND_ENTERS:
        before, after = LAND_ENTERS[name]
        if before in source:
            source = fix_imports(source.replace(before, after))
        return source
    if name in CHOSEN_MODE_CARDS:
        source = source.replace(".withInterveningIf(new SourceHasChosenMode(", ".withEventCondition(new SourceHasChosenMode(")
    return source


def main():
    changed = []
    for module in sys.argv[1:]:
        for path in sorted(pathlib.Path(module).glob("src/main/java/**/*.java")):
            source = path.read_text()
            result = convert(path.stem, source)
            if result != source:
                path.write_text(result)
                changed.append(path.stem)
    print(f"{len(changed)} card classes changed: {' '.join(changed)}")


if __name__ == "__main__":
    main()
