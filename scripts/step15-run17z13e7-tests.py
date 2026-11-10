#!/usr/bin/env python3
"""Step 15, run 17z13e7: the collector tests follow the watches' new defaults.

- `MiscTriggerCollectorServiceTest`: the Relic Bind case asserted the deleted modal route's mode choice; the modal goes
  on the stack through the enchanted-tap default with its record, its mode announced from the stack. The energy case
  asserted the old default's bare `SelfTriggeredAbilityTarget`; it asserts the any-target choice with the player and
  the energy gained. New: the record of each new default (collect evidence, the first investigation, a foretell, a tax
  payment, a lore counter, counters put, loyalty and other counters removed, a control change, a mana ability
  resolving, an untargeted energy ability). The unused `AmountEvaluationService` mock goes with the service's.
- `DamageTriggerCollectorServiceTest`: the counters-put case called the deleted damage handler; the default is
  `MiscTriggerCollectorService`'s, whose test has it.
- `SpellCastTriggerCollectorServiceTest`: the Leering Emblem, Chalice-style counter and Lurking Predators cases called the
  deleted class-keyed cast handlers; the readers collect an unfiltered cast ability as "any spell" through the generic
  cast collector, which they now call (with their banner comments gone).

Idempotent.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-application/src/test/java/com/github/laxika/magicalvibes/service/trigger"


def edit(path, pairs):
    text = path.read_text()
    for old, new in pairs:
        if new and new in text:
            continue
        if old not in text:
            sys.exit(f"{path.name}: pattern not found: {old[:80]!r}")
        text = text.replace(old, new, 1)
    path.write_text(text)


MISC_IMPORTS = [
    "com.github.laxika.magicalvibes.model.effect.ChooseOneEffect",
    "com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect",
    "com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect",
    "com.github.laxika.magicalvibes.model.effect.SeekEffect",
    "com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect",
    "com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter",
]

OLD_MODAL = '''    @Test
    void enchantedPermanentTapModalWaitsForModeAndTargetBeforeGoingOnTheStack() {
        Permanent aura = createPermanent("Modal aura");
        Permanent tapped = createPermanent("Artifact");
        var effect = new com.github.laxika.magicalvibes.model.effect.ChooseOneEffect(List.of(
                new com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.ChooseOneOption("Gain",
                        new com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect(1)),
                new com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.ChooseOneOption("Damage",
                        new com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect(1))));

        registry.dispatch(match(aura, player1Id, effect), TapWatch.ON_ENCHANTED_PERMANENT_TAPPED,
                effect, new TriggerContext.EnchantedPermanentTap(tapped, player2Id));

        assertThat(gd.stack).isEmpty();
        var choice = gd.pollPendingInteraction(
                com.github.laxika.magicalvibes.model.PermanentChoiceContext.TriggeredModalTrigger.class);
        assertThat(choice).isNotNull();
        assertThat(choice.controllerId()).isEqualTo(player1Id);
        assertThat(choice.sourcePermanentId()).isEqualTo(aura.getId());
        assertThat(choice.effect()).isSameAs(effect);
        assertThat(choice.triggeringPermanentId()).isEqualTo(tapped.getId());
    }
'''

NEW_MODAL = '''    @Test
    @DisplayName("Relic Bind's \\"choose one\\" goes on the stack with the enchanted-tap record, its modes' targets its own")
    void enchantedPermanentTapModalGoesOnTheStackWithTheRecord() {
        Permanent aura = createPermanent("Modal aura");
        Permanent tapped = createPermanent("Artifact");
        var effect = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Gain", new TargetPlayerGainsLifeEffect(1)),
                new ChooseOneEffect.ChooseOneOption("Damage", new DealDamageToTargetPlayerOrPlaneswalkerEffect(1))));

        registry.dispatch(match(aura, player1Id, effect), TapWatch.ON_ENCHANTED_PERMANENT_TAPPED,
                effect, new TriggerContext.EnchantedPermanentTap(tapped, player2Id));

        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.TriggeredModalTrigger.class)).isFalse();
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            assertThat(entry.getSourcePermanentId()).isEqualTo(aura.getId());
            assertThat(entry.getSourcePermanentSnapshot()).isNotNull();
            assertThat(entry.getTriggeringPermanentId()).isEqualTo(tapped.getId());
            assertThat(entry.getTriggeringPermanentControllerId()).isEqualTo(player2Id);
            assertThat(entry.getTargetId()).isNull();
        });
    }
'''

OLD_ENERGY = '''    @Test
    @DisplayName("energy-gain default trigger queues the original effect")
    void energyGainDefaultTriggerQueuesEffect() {
        Permanent perm = createPermanent("Fabrication Module");
        var effect = new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1);
        var ctx = new TriggerContext.EnergyGain(player1Id, 1);

        boolean result = registry.dispatch(
                match(perm, player1Id, effect), CounterWatch.ON_CONTROLLER_GETS_ENERGY, effect, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).isEmpty();
        PermanentChoiceContext.SelfTriggeredAbilityTarget pending =
                gd.peekPendingInteraction(PermanentChoiceContext.SelfTriggeredAbilityTarget.class);
        assertThat(pending).isNotNull();
        assertThat(pending.effects()).containsExactly(effect);
        assertThat(pending.controllerId()).isEqualTo(player1Id);
        assertThat(pending.sourcePermanentId()).isEqualTo(perm.getId());
    }
'''

NEW_ENERGY = '''    @Test
    @DisplayName("energy-gain default: a targeted ability's choice carries the player and the energy gained")
    void energyGainDefaultTargetChoiceCarriesTheRecord() {
        Permanent perm = createPermanent("Fabrication Module");
        var effect = new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1);
        var ctx = new TriggerContext.EnergyGain(player1Id, 2);

        boolean result = registry.dispatch(
                match(perm, player1Id, effect), CounterWatch.ON_CONTROLLER_GETS_ENERGY, effect, ctx);

        assertThat(result).isTrue();
        assertThat(gd.stack).isEmpty();
        var pending = gd.peekPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class);
        assertThat(pending).isNotNull();
        assertThat(pending.effects()).containsExactly(effect);
        assertThat(pending.controllerId()).isEqualTo(player1Id);
        assertThat(pending.sourcePermanentId()).isEqualTo(perm.getId());
        assertThat(pending.sourcePermanentSnapshot()).isNotNull();
        assertThat(pending.triggeringPlayerId()).isEqualTo(player1Id);
        assertThat(pending.eventValue()).isEqualTo(2);
    }

    /**
     * The defaults of run 17z13e7's watches: each records the event whatever the effect is
     * ({@code MiscTriggerCollectorService.queueEventAbility}).
     */
    @Nested
    @DisplayName("Player, counter and control events are recorded whatever the effects are")
    class EventRecords {

        @Test
        @DisplayName("Energy: an untargeted ability records the player and the energy gained")
        void energyGainRecordsThePlayerAndTheAmount() {
            Permanent perm = createPermanent("Territorial Gorger");
            var effect = new BoostSelfEffect(2, 2);

            registry.dispatch(match(perm, player1Id, effect), CounterWatch.ON_CONTROLLER_GETS_ENERGY, effect,
                    new TriggerContext.EnergyGain(player1Id, 3));

            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getEffectsToResolve()).containsExactly(effect);
                assertThat(entry.getTriggeringPlayerId()).isEqualTo(player1Id);
                assertThat(entry.getEventValue()).isEqualTo(3);
                assertThat(entry.getSourcePermanentSnapshot()).isNotNull();
                assertThat(entry.getTargetId()).isNull();
            });
        }

        @Test
        @DisplayName("Collect evidence, the first investigation and a foretell record the acting player")
        void playerActionsRecordThePlayer() {
            Permanent perm = createPermanent("Surveillance Monitor");
            var token = CreateTokenEffect.ofClueToken(1);
            Card foretold = createCard("Foretold card");

            registry.dispatch(match(perm, player1Id, token), ActionWatch.ON_CONTROLLER_COLLECTS_EVIDENCE, token,
                    new TriggerContext.CollectEvidence(player1Id));
            registry.dispatch(match(perm, player1Id, token), ActionWatch.ON_CONTROLLER_INVESTIGATES, token,
                    new TriggerContext.Investigate(player1Id));
            var boost = new BoostSelfEffect(2, 0);
            registry.dispatch(match(perm, player1Id, boost), ActionWatch.ON_CONTROLLER_FORETELLS, boost,
                    new TriggerContext.Foretell(player1Id, foretold));

            assertThat(gd.stack).hasSize(3).allSatisfy(entry -> {
                assertThat(entry.getTriggeringPlayerId()).isEqualTo(player1Id);
                assertThat(entry.getSourcePermanentId()).isEqualTo(perm.getId());
                assertThat(entry.getSourcePermanentSnapshot()).isNotNull();
            });
            assertThat(gd.stack.getLast().getEffectsToResolve()).containsExactly(boost);
            assertThat(gd.stack.getLast().getTriggeringCardId()).isEqualTo(foretold.getId());
        }

        @Test
        @DisplayName("Tax payment: the paying opponent is \\"that player\\" and the mana paid the event value")
        void taxPaymentRecordsThePayerAndTheManaPaid() {
            Permanent perm = createPermanent("Tax Taker");
            var effect = CreateTokenEffect.ofTreasureToken(new EventValue());

            registry.dispatch(match(perm, player1Id, effect), ActionWatch.ON_OPPONENT_PAYS_TAX, effect,
                    new TriggerContext.TaxPayment(player2Id, player1Id, 3));

            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getEffectsToResolve()).containsExactly(effect);
                assertThat(entry.getEventValue()).isEqualTo(3);
                assertThat(entry.getTriggeringPlayerId()).isEqualTo(player2Id);
                assertThat(entry.getTargetId()).isEqualTo(player2Id);
                assertThat(entry.isNonTargeting()).isTrue();
                assertThat(entry.getSourcePermanentSnapshot()).isNotNull();
            });
        }

        @Test
        @DisplayName("Counters put: the target choice carries the placing player and the count")
        void countersPlacedCarriesTheCountAsTheEventValue() {
            Permanent watcher = createPermanent("All Will Be One");
            var damage = new DealDamageToAnyTargetEffect(new EventValue());

            boolean result = registry.dispatch(match(watcher, player1Id, damage),
                    CounterWatch.ON_YOU_PUT_COUNTERS_ON_PERMANENT_OR_PLAYER, damage,
                    new TriggerContext.CountersPlaced(player1Id, 4));

            assertThat(result).isTrue();
            var pending = gd.peekPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class);
            assertThat(pending.effects()).containsExactly(damage);
            assertThat(pending.eventValue()).isEqualTo(4);
            assertThat(pending.triggeringPlayerId()).isEqualTo(player1Id);
            assertThat(pending.sourcePermanentSnapshot()).isNotNull();
        }

        @Test
        @DisplayName("Lore counter: the target choice carries the Saga as the triggering permanent")
        void loreCounterRecordsTheSaga() {
            Permanent sigurd = createPermanent("Sigurd");
            Permanent saga = createPermanent("Saga");
            var effect = new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1);

            registry.dispatch(match(sigurd, player1Id, effect), CounterWatch.ON_YOU_PUT_LORE_COUNTERS_ON_SAGA,
                    effect, new TriggerContext.LoreCounterPlaced(saga, player1Id));

            var pending = gd.peekPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class);
            assertThat(pending.triggeringPermanentId()).isEqualTo(saga.getId());
            assertThat(pending.triggeringPermanentControllerId()).isEqualTo(player1Id);
            assertThat(pending.triggeringPlayerId()).isEqualTo(player1Id);
        }

        @Test
        @DisplayName("Loyalty counters removed: \\"target opponent or planeswalker\\" with the count removed")
        void loyaltyCountersRemovedCarriesTheCountAndTheOpponentFilter() {
            Permanent chandra = createPermanent("Chandra");
            var damage = new DealDamageToTargetPlayerOrPlaneswalkerEffect(new EventValue(), PlayerRelation.OPPONENT);

            registry.dispatch(match(chandra, player1Id, damage), CounterWatch.ON_SELF_LOYALTY_COUNTERS_REMOVED,
                    damage, new TriggerContext.LoyaltyCountersRemoved(chandra, 2));

            var pending = gd.peekPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class);
            assertThat(pending.effects()).containsExactly(damage);
            assertThat(pending.eventValue()).isEqualTo(2);
            assertThat(pending.triggeringPermanentId()).isEqualTo(chandra.getId());
            assertThat(pending.targetFilter()).isInstanceOf(AnyTargetPredicateTargetFilter.class);
        }

        @Test
        @DisplayName("Counters removed from a permanent you control: the permanent and the count")
        void countersRemovedRecordsThePermanent() {
            Permanent armorer = createPermanent("Cloudsculpt Armorer");
            Permanent shielded = createPermanent("Shielded creature");
            var seek = new SeekEffect(new CardTypePredicate(CardType.CREATURE));

            registry.dispatch(match(armorer, player1Id, seek), CounterWatch.ON_ALLY_COUNTERS_REMOVED_FROM_PERMANENT,
                    seek, new TriggerContext.CountersRemovedFromPermanent(shielded, 1));

            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getEffectsToResolve()).containsExactly(seek);
                assertThat(entry.getTriggeringPermanentId()).isEqualTo(shielded.getId());
                assertThat(entry.getTriggeringPermanentControllerId()).isEqualTo(player1Id);
                assertThat(entry.getEventValue()).isEqualTo(1);
            });
        }

        @Test
        @DisplayName("Control change: the permanent and its new controller as \\"that player\\"")
        void controlChangeRecordsThePermanentAndTheNewController() {
            Permanent zidane = createPermanent("Zidane");
            Permanent stolen = createPermanent("Stolen permanent");
            var effect = CreateTokenEffect.ofTreasureToken(1);

            registry.dispatch(match(zidane, player1Id, effect), MiscWatch.ON_OPPONENT_GAINS_CONTROL_OF_YOUR_PERMANENT,
                    effect, new TriggerContext.PermanentControlChanged(stolen, player1Id, player2Id));

            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getTriggeringPermanentId()).isEqualTo(stolen.getId());
                assertThat(entry.getTriggeringPermanentControllerId()).isEqualTo(player2Id);
                assertThat(entry.getTriggeringPlayerId()).isEqualTo(player2Id);
                assertThat(entry.getTargetId()).isEqualTo(player2Id);
                assertThat(entry.isNonTargeting()).isTrue();
            });
        }

        @Test
        @DisplayName("Mana ability resolved: the mana produced is the event value")
        void manaAbilityResolvedRecordsTheManaProduced() {
            Permanent creature = createPermanent("Elf");
            var effect = new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new EventValue());

            registry.dispatch(match(creature, player1Id, effect), SpellAbilityWatch.ON_SELF_MANA_ABILITY_RESOLVES,
                    effect, new TriggerContext.ManaAbilityResolved(player1Id, 2));

            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getEffectsToResolve()).containsExactly(effect);
                assertThat(entry.getEventValue()).isEqualTo(2);
                assertThat(entry.getTriggeringPlayerId()).isEqualTo(player1Id);
                assertThat(entry.getSourcePermanentSnapshot()).isNotNull();
            });
        }
    }
'''

OLD_DAMAGE_CASE = '''        @Test
        @DisplayName("Counters put: the authored damage goes to the target choice with the counters as its event value")
        void countersPlacedCarriesTheCountAsTheEventValue() {
            Permanent watcher = createPermanent("All Will Be One");
            var damage = new DealDamageToAnyTargetEffect(new EventValue());
            var ctx = new TriggerContext.CountersPlaced(player1Id, 4);

            boolean result = registry.dispatch(match(watcher, player1Id, damage),
                    CounterWatch.ON_YOU_PUT_COUNTERS_ON_PERMANENT_OR_PLAYER, damage, ctx);

            assertThat(result).isTrue();
            var pending = gd.peekPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class);
            assertThat(pending.effects()).containsExactly(damage);
            assertThat(pending.spellManaSpentX()).isEqualTo(4);
        }

'''

CAST_CALLS = [
    ("ControllerBoostEquippedOnSpellCast", "SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL", "youCastSpell"),
    ("OpponentCounterUnlessPays", "SpellAbilityWatch.ON_OPPONENT_CASTS_SPELL", "opponentCastsSpell"),
    ("OpponentRevealTopCard", "SpellAbilityWatch.ON_OPPONENT_CASTS_SPELL", "opponentCastsSpell"),
]
CAST_BANNERS = [
    "    // ===== ON_CONTROLLER_CASTS_SPELL — BoostEquippedCreatureUntilEndOfTurnEffect =====\n\n",
    "    // ===== ON_OPPONENT_CASTS_SPELL — CounterUnlessPaysEffect =====\n\n",
    "    // ===== ON_OPPONENT_CASTS_SPELL — RevealTopCardCreatureToBattlefieldOrMayBottomEffect =====\n\n",
]


def misc():
    path = TESTS / "MiscTriggerCollectorServiceTest.java"
    text = path.read_text()
    for imp in MISC_IMPORTS:
        line = f"import {imp};\n"
        if line not in text:
            text = text.replace("import com.github.laxika.magicalvibes.model.effect.CardEffect;\n",
                                "import com.github.laxika.magicalvibes.model.effect.CardEffect;\n" + line, 1)
    text = text.replace("    @Mock\n    private AmountEvaluationService amountEvaluationService;\n\n", "")
    text = text.replace("import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;\n", "")
    # A first version of the new Relic Bind case asserted the tapped permanent's controller as "that player".
    text = text.replace("its mode announced there\")", "its modes' targets its own\")")
    if "void enchantedPermanentTapModalGoesOnTheStackWithTheRecord()" in text:
        start = text.index("void enchantedPermanentTapModalGoesOnTheStackWithTheRecord()")
        end = text.index("\n    }\n", start)
        text = text[:start] + text[start:end].replace(
            "            assertThat(entry.getTargetId()).isEqualTo(player2Id);\n"
            "            assertThat(entry.isNonTargeting()).isTrue();\n",
            "            assertThat(entry.getTargetId()).isNull();\n") + text[end:]
    path.write_text(text)
    edit(path, [(OLD_MODAL, NEW_MODAL), (OLD_ENERGY, NEW_ENERGY)])


def damage():
    path = TESTS / "DamageTriggerCollectorServiceTest.java"
    text = path.read_text()
    text = text.replace(OLD_DAMAGE_CASE, "")
    path.write_text(text)


def cast():
    path = TESTS / "SpellCastTriggerCollectorServiceTest.java"
    text = path.read_text()
    for banner in CAST_BANNERS:
        text = text.replace(banner, "")
    for nested, watch, factory in CAST_CALLS:
        start = text.index(f"    class {nested} {{")
        end = text.index("\n    }\n", start)
        body = text[start:end]
        body = re.sub(r"registry\.dispatch\(\s*match\(perm, player1Id, effect\),\s*" + re.escape(watch)
                      + r", effect, ctx\)",
                      f"sut.collectCastAbility(match(perm, player1Id, effect),\n"
                      f"                    castAbility(Triggers.{factory}().anySpell(), effect), ctx, false)", body)
        # The generic collector judges the "any spell" filter through the (mocked) predicate service.
        body = re.sub(r"\n(            )((?:boolean result = )?sut\.collectCastAbility\()",
                      r"\n\1everySpellMatches();\n\1\2", body.replace("            everySpellMatches();\n", ""))
        text = text[:start] + body + text[end:]
    path.write_text(text)
    if "registry.dispatch(\n                    match(perm, player1Id, effect),\n                    SpellAbilityWatch.ON_OPPONENT_CASTS_SPELL" in text:
        sys.exit("a cast handler call is left")


def main():
    misc()
    damage()
    cast()


if __name__ == "__main__":
    main()
