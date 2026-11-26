package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardScanner;
import com.github.laxika.magicalvibes.cards.d.DimensionXPizzasaur;
import com.github.laxika.magicalvibes.cards.j.JeeringHomunculus;
import com.github.laxika.magicalvibes.cards.p.PalaceJailer;
import com.github.laxika.magicalvibes.cards.s.Shellshock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.AttachTargetEquipmentsToTargetCreatureThenEffect;
import com.github.laxika.magicalvibes.model.effect.AttachTargetToSourcePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;
import com.github.laxika.magicalvibes.model.effect.AttachTargetAuraToAnotherPermanentOfSameTypeEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreaturesByPositionEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCreaturesBlockedByTargetWallThenReturnFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentMayPlayWithOpponentTaxEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilOpponentBecomesMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MakeTargetAttackingCreatureBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureBlockableOnlyByFilterThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfSubjectEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PutTargetCreatureOnTopOrOptionalBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToTargetCreatureFromTargetingSpellOrAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterControlLossUnattachTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardOnDeathThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.amount.TargetManaValue;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.effect.DiscoverEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetPermanentIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SuspectEffect;
import com.github.laxika.magicalvibes.model.effect.TapAndPreventCombatDamageByTargetAndBlockersEffect;
import com.github.laxika.magicalvibes.model.effect.TapCombatOpponentsOfTargetAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToTargetFromColorlessSourcesEffect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exhaustiveness guard for {@link TargetPolarityClassifier}: every permanent-targeting
 * effect that any registered card puts in its spell ability or "when this enters" abilities must
 * classify to a non-null {@link TargetPolarity}. An unclassified shape falls into
 * {@code AiTargetSelector.chooseTarget}'s own-battlefield-first general fallback, which
 * silently aims harmful spells at the AI's own permanents (the Quicksilver Geyser / Stun
 * bug family). This test turns that silent regression into a build failure.
 */
class TargetPolarityGuardTest {

    @Test
    @DisplayName("Reflexive counter placement classifies only its initial target")
    void reflexiveCounterPlacementClassifiesOnlyItsInitialTarget() {
        GameTestHarness harness = new GameTestHarness();
        TargetPolarityClassifier classifier = createClassifier(harness);
        GameData gd = harness.getGameData();
        UUID aiPlayerId = harness.getPlayer2().getId();

        Map<CounterType, TargetPolarity> expected = Map.of(
                CounterType.PLUS_ONE_PLUS_ONE, TargetPolarity.BENEFICIAL,
                CounterType.MINUS_ONE_MINUS_ONE, TargetPolarity.HARMFUL,
                CounterType.CHARGE, TargetPolarity.NEUTRAL);
        expected.forEach((counterType, polarity) ->
                assertThat(classifier.classify(gd, SequenceEffect.of(
                        new PutCountersEffect(counterType, 2),
                        new ConditionalEffect(new HappenedThisWay(StepKind.PUT_COUNTERS),
                                new QueueReflexiveAbilityEffect(ReturnToHandEffect.target()))), aiPlayerId))
                        .as("initial %s counters, independently targeted reflexive removal", counterType)
                        .isEqualTo(polarity));
        assertThat(classifier.classifyCard(gd, new DimensionXPizzasaur(), aiPlayerId))
                .isEqualTo(TargetPolarity.BENEFICIAL);
    }

    @Test
    @DisplayName("A -1/-1 for each counted permanent is a debuff even while it counts nothing")
    void negativePerCountBoostIsHarmful() {
        GameTestHarness harness = new GameTestHarness();
        TargetPolarityClassifier classifier = createClassifier(harness);

        assertThat(classifier.classifyCard(harness.getGameData(),
                new com.github.laxika.magicalvibes.cards.p.PacksDisdain(), harness.getPlayer2().getId()))
                .isEqualTo(TargetPolarity.HARMFUL);
    }

    @Test
    @DisplayName("Damage then token creation targets opposing creatures")
    void damageThenTokenCreationTargetsOpposingCreatures() {
        GameTestHarness harness = new GameTestHarness();
        TargetPolarityClassifier classifier = createClassifier(harness);

        assertThat(classifier.classifyCard(harness.getGameData(),
                new Shellshock(), harness.getPlayer2().getId()))
                .isEqualTo(TargetPolarity.HARMFUL_DAMAGE);
    }

    @Test
    @DisplayName("Classifies equipment attachments and counter removal")
    void classifiesEquipmentAttachmentsAndCounterRemoval() {
        GameTestHarness harness = new GameTestHarness();
        GameData gd = harness.getGameData();
        UUID aiPlayerId = harness.getPlayer2().getId();
        TargetPolarityClassifier classifier = createClassifier(harness);

        assertThat(classifier.classify(gd, new AttachTargetToSourcePermanentEffect(), aiPlayerId))
                .isEqualTo(TargetPolarity.BENEFICIAL);
        assertThat(classifier.classify(gd, new AttachTargetEquipmentsToTargetCreatureThenEffect(
                BoostEffect.target(1, 1)), aiPlayerId))
                .isEqualTo(TargetPolarity.BENEFICIAL);
        assertThat(classifier.classify(gd, RemoveAllCountersFromTargetPermanentEffect.fromTargetCreature(), aiPlayerId))
                .isEqualTo(TargetPolarity.HARMFUL);
        assertThat(classifier.classifyCard(gd,
                new com.github.laxika.magicalvibes.cards.t.ThievingSkydiver(), aiPlayerId))
                .isEqualTo(TargetPolarity.HARMFUL);
    }

    @Test
    @DisplayName("Removing counters of a fixed kind from a target is scored by the kind; a kind picked at resolution is neutral")
    void classifiesTargetCounterRemovalByKind() {
        GameTestHarness harness = new GameTestHarness();
        GameData gd = harness.getGameData();
        UUID aiPlayerId = harness.getPlayer2().getId();
        TargetPolarityClassifier classifier = createClassifier(harness);

        assertThat(classifier.classify(gd, RemoveCountersEffect.fromTarget(CounterType.MINUS_ONE_MINUS_ONE, 1),
                aiPlayerId)).isEqualTo(TargetPolarity.BENEFICIAL);
        assertThat(classifier.classify(gd, RemoveCountersEffect.fromTarget(CounterType.PLUS_ONE_PLUS_ONE, 1),
                aiPlayerId)).isEqualTo(TargetPolarity.HARMFUL);
        assertThat(classifier.classify(gd, RemoveCountersEffect.fromTarget(CounterType.LOYALTY, 3, null),
                aiPlayerId)).isEqualTo(TargetPolarity.HARMFUL);
        assertThat(classifier.classify(gd, RemoveCountersEffect.fromTarget(), aiPlayerId))
                .isEqualTo(TargetPolarity.NEUTRAL);
        assertThat(classifier.classify(gd, RemoveCountersEffect.fromSelf(CounterType.TIME, 1), aiPlayerId))
                .isNull();
        assertThat(classifier.classifyCard(gd,
                new com.github.laxika.magicalvibes.cards.m.MedicineRunner(), aiPlayerId))
                .isEqualTo(TargetPolarity.NEUTRAL);
    }

    @Test
    @DisplayName("Removing all -1/-1 counters from a target creature helps it")
    void classifiesRemovingAllMinusOneCountersFromATargetAsBeneficial() {
        GameTestHarness harness = new GameTestHarness();
        GameData gd = harness.getGameData();
        UUID aiPlayerId = harness.getPlayer2().getId();
        TargetPolarityClassifier classifier = createClassifier(harness);

        assertThat(classifier.classifyCard(gd,
                new com.github.laxika.magicalvibes.cards.h.HapatrasMark(), aiPlayerId))
                .isEqualTo(TargetPolarity.BENEFICIAL);
    }

    @Test
    @DisplayName("Classifies representative permanent targeting shapes by direction")
    void classifiesRepresentativePermanentTargetingShapesByDirection() {
        GameTestHarness harness = new GameTestHarness();
        GameData gd = harness.getGameData();
        UUID aiPlayerId = harness.getPlayer2().getId();
        TargetPolarityClassifier classifier = createClassifier(harness);

        assertThat(classifier.classify(gd,
                new FlipCoinWinEffect(BoostEffect.target(1, 1)), aiPlayerId))
                .isEqualTo(TargetPolarity.BENEFICIAL);
        assertThat(classifier.classify(gd, new MakeTargetAttackingCreatureBlockedEffect(), aiPlayerId))
                .isEqualTo(TargetPolarity.HARMFUL);
        assertThat(classifier.classify(gd, new PhaseOutEffect(Subjects.target()), aiPlayerId))
                .isEqualTo(TargetPolarity.HARMFUL_REMOVAL);
        assertThat(classifier.classify(gd, new PhaseOutEffect(Subjects.source()), aiPlayerId))
                .isNull();
        assertThat(classifier.classify(gd,
                new PutTargetCreatureOnTopOrOptionalBottomOfLibraryEffect(
                        new PermanentColorInPredicate(Set.of(CardColor.RED))), aiPlayerId))
                .isEqualTo(TargetPolarity.HARMFUL_REMOVAL);
        assertThat(classifier.classify(
                gd, new TapCombatOpponentsOfTargetAtEndOfCombatEffect(), aiPlayerId))
                .isEqualTo(TargetPolarity.BENEFICIAL);
        assertThat(classifier.classify(
                gd, new AttachTargetAuraToAnotherPermanentOfSameTypeEffect(), aiPlayerId))
                .isEqualTo(TargetPolarity.NEUTRAL);
        assertThat(classifier.classify(gd,
                new MayEffect(new EnteringCreatureFightsTargetCreatureEffect(), "Fight?"), aiPlayerId))
                .isEqualTo(TargetPolarity.HARMFUL);
        assertThat(classifier.classify(gd,
                new ExileTargetPermanentMayPlayWithOpponentTaxEffect(), aiPlayerId))
                .isEqualTo(TargetPolarity.HARMFUL_REMOVAL);
        assertThat(classifier.classify(gd,
                new DestroyTargetPermanentEffect(), aiPlayerId))
                .isEqualTo(TargetPolarity.HARMFUL_REMOVAL);
        assertThat(classifier.classify(gd,
                new ReturnTargetCardOnDeathThisTurnEffect(), aiPlayerId))
                .isEqualTo(TargetPolarity.BENEFICIAL);
        assertThat(classifier.classify(gd,
                new GrantSubtypeToTargetCreatureEffect(CardSubtype.TREASURE), aiPlayerId))
                .isEqualTo(TargetPolarity.HARMFUL);
        assertThat(classifier.classify(gd,
                new SetCardTypesEffect(Set.of(CardType.ARTIFACT), Subjects.target()), aiPlayerId))
                .isEqualTo(TargetPolarity.HARMFUL);
        assertThat(classifier.classify(gd,
                new ShuffleTargetPermanentIntoLibraryEffect(
                        new DiscoverEffect(new TargetManaValue()), ThenEffectRecipient.TARGET_OWNER), aiPlayerId))
                .isEqualTo(TargetPolarity.HARMFUL_REMOVAL);
        assertThat(classifier.classify(gd,
                new AttachSourceEquipmentToTargetCreatureEffect(Subjects.thisWay(StepKind.CHOOSE), Subjects.target()),
                aiPlayerId))
                .isEqualTo(TargetPolarity.BENEFICIAL);
        assertThat(classifier.classify(gd,
                DealDamageEffect.toEachOther(Subjects.target(), Subjects.allCreatures(), new SourcePower()),
                aiPlayerId))
                .isEqualTo(TargetPolarity.BENEFICIAL);
        assertThat(classifier.classify(gd,
                RegisterControlLossUnattachTriggerEffect.whenControlLost(), aiPlayerId))
                .isEqualTo(TargetPolarity.NEUTRAL);
        assertThat(classifier.classify(gd,
                new DestroyCreaturesBlockedByTargetWallThenReturnFromGraveyardEffect(), aiPlayerId))
                .isEqualTo(TargetPolarity.BENEFICIAL);
        assertThat(classifier.classify(gd,
                new PreventDamageToTargetCreatureFromTargetingSpellOrAbilityEffect(), aiPlayerId))
                .isEqualTo(TargetPolarity.BENEFICIAL);
        assertThat(classifier.classify(gd,
                new PreventAllDamageToTargetFromColorlessSourcesEffect(), aiPlayerId))
                .isEqualTo(TargetPolarity.BENEFICIAL);
        assertThat(classifier.classify(gd,
                new TapAndPreventCombatDamageByTargetAndBlockersEffect(), aiPlayerId))
                .isEqualTo(TargetPolarity.NEUTRAL);
        assertThat(classifier.classify(gd, new SuspectEffect(Subjects.target()), aiPlayerId))
                .isEqualTo(TargetPolarity.NEUTRAL);
        assertThat(classifier.classify(gd, new SuspectEffect(Subjects.source()), aiPlayerId))
                .isNull();
    }

    @Test
    @DisplayName("Monarch duration exile classifies as harmful removal")
    void monarchDurationExileClassifiesAsHarmfulRemoval() {
        GameTestHarness harness = new GameTestHarness();
        GameData gd = harness.getGameData();
        UUID aiPlayerId = harness.getPlayer2().getId();
        TargetPolarityClassifier classifier = createClassifier(harness);

        assertThat(classifier.classify(gd,
                new ExileTargetPermanentUntilOpponentBecomesMonarchEffect(), aiPlayerId))
                .isEqualTo(TargetPolarity.HARMFUL_REMOVAL);
        assertThat(classifier.classifyCard(gd, new PalaceJailer(), aiPlayerId))
                .isEqualTo(TargetPolarity.HARMFUL_REMOVAL);
    }

    @Test
    @DisplayName("Optional goad targets opposing creatures")
    void optionalGoadTargetsOpposingCreatures() {
        GameTestHarness harness = new GameTestHarness();
        TargetPolarityClassifier classifier = createClassifier(harness);

        assertThat(classifier.classifyCard(harness.getGameData(),
                new JeeringHomunculus(), harness.getPlayer2().getId()))
                .isEqualTo(TargetPolarity.HARMFUL);
    }

    @Test
    @DisplayName("Recent permanent targeting cards choose the intended board")
    void recentPermanentTargetingCardsChooseTheIntendedBoard() {
        GameTestHarness harness = new GameTestHarness();
        GameData gd = harness.getGameData();
        UUID aiPlayerId = harness.getPlayer2().getId();
        TargetPolarityClassifier classifier = createClassifier(harness);

        assertThat(classifier.classifyCard(gd,
                new com.github.laxika.magicalvibes.cards.t.TownRazerTyrant(), aiPlayerId))
                .isEqualTo(TargetPolarity.HARMFUL);
        assertThat(classifier.classifyCard(gd,
                new com.github.laxika.magicalvibes.cards.r.RavenousPursuit(), aiPlayerId))
                .isEqualTo(TargetPolarity.HARMFUL);
        assertThat(classifier.classifyCard(gd,
                new com.github.laxika.magicalvibes.cards.s.SinisterReflections(), aiPlayerId))
                .isEqualTo(TargetPolarity.BENEFICIAL);
        assertThat(classifier.classifyCard(gd,
                new com.github.laxika.magicalvibes.cards.h.HeroicSacrifice(), aiPlayerId))
                .isEqualTo(TargetPolarity.BENEFICIAL);
        assertThat(classifier.classifyCard(gd,
                new com.github.laxika.magicalvibes.cards.m.MisleadingSignpost(), aiPlayerId))
                .isEqualTo(TargetPolarity.NEUTRAL);
        assertThat(classifier.classify(gd,
                new com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTargetCreatureEffect(-1, 0),
                aiPlayerId)).isEqualTo(TargetPolarity.HARMFUL);
    }

    @Test
    @DisplayName("Avatar's Wrath, Overblaze and Pack Hunt aim at the AI's own creature")
    void sparedDoubledAndCopiedTargetsAreTheAisOwn() {
        GameTestHarness harness = new GameTestHarness();
        GameData gd = harness.getGameData();
        UUID aiPlayerId = harness.getPlayer2().getId();
        TargetPolarityClassifier classifier = createClassifier(harness);

        assertThat(classifier.classifyCard(gd,
                new com.github.laxika.magicalvibes.cards.a.AvatarsWrath(), aiPlayerId))
                .isEqualTo(TargetPolarity.BENEFICIAL);
        assertThat(classifier.classifyCard(gd,
                new com.github.laxika.magicalvibes.cards.o.Overblaze(), aiPlayerId))
                .isEqualTo(TargetPolarity.BENEFICIAL);
        assertThat(classifier.classifyCard(gd,
                new com.github.laxika.magicalvibes.cards.p.PackHunt(), aiPlayerId))
                .isEqualTo(TargetPolarity.BENEFICIAL);
    }

    @Test
    @DisplayName("Perpetual abilities use their granted effect to choose targets")
    void perpetualAbilitiesUseTheirGrantedEffectToChooseTargets() {
        GameTestHarness harness = new GameTestHarness();
        GameData gd = harness.getGameData();
        UUID aiPlayerId = harness.getPlayer2().getId();
        TargetPolarityClassifier classifier = createClassifier(harness);

        assertThat(classifier.classify(gd,
                new com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantStaticEffectToTargetCreatureEffect(
                        new com.github.laxika.magicalvibes.model.effect.MustAttackEffect()), aiPlayerId))
                .isEqualTo(TargetPolarity.HARMFUL);
        assertThat(classifier.classify(gd,
                new com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantStaticEffectToTargetCreatureEffect(
                        new com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect()), aiPlayerId))
                .isEqualTo(TargetPolarity.BENEFICIAL);
        assertThat(classifier.classify(gd,
                new com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect(
                        new TriggeredAbility(Triggers.upkeep(ActivePlayer.ANY),
                                com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect.toCards(
                                        com.github.laxika.magicalvibes.model.effect.Subjects.sourceObjectCard(), -1, -1))),
                aiPlayerId)).isEqualTo(TargetPolarity.HARMFUL);
    }

    @Test
    @DisplayName("Classifies cloaking and combat reassignment")
    void classifiesCloakingAndCombatReassignment() {
        GameTestHarness harness = new GameTestHarness();
        TargetPolarityClassifier classifier = createClassifier(harness);
        GameData gd = harness.getGameData();
        UUID aiPlayerId = harness.getPlayer2().getId();

        assertThat(classifier.classify(gd,
                com.github.laxika.magicalvibes.model.effect.ExileFaceDownPileEffect.thenCloak(
                        new com.github.laxika.magicalvibes.model.effect.ExileFaceDownPileEffect.PermanentsAndTopCards(
                                com.github.laxika.magicalvibes.model.effect.Subjects.target(), 2)),
                aiPlayerId)).isEqualTo(TargetPolarity.BENEFICIAL);
        assertThat(classifier.classify(gd,
                com.github.laxika.magicalvibes.model.effect.BalduvianWarlordEffect.thenMayReblock(), aiPlayerId))
                .isEqualTo(TargetPolarity.NEUTRAL);
        assertThat(classifier.classify(gd,
                com.github.laxika.magicalvibes.model.effect.ExchangeControlOfPermanentAndSpellEffect.targetCreatureAndTargetSpell(),
                aiPlayerId)).isEqualTo(TargetPolarity.NEUTRAL);
        assertThat(classifier.classify(gd,
                new com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect(
                        com.github.laxika.magicalvibes.model.effect.Duration.UNTIL_END_OF_TURN)
                        .gainedBy(com.github.laxika.magicalvibes.model.effect.Subjects.targetPlayer(0)),
                aiPlayerId)).isEqualTo(TargetPolarity.NEUTRAL);
        assertThat(classifier.classify(gd,
                new com.github.laxika.magicalvibes.model.effect.MustBlockEachAttackingCreatureThisTurnEffect(), aiPlayerId))
                .isEqualTo(TargetPolarity.NEUTRAL);
    }

    @Test
    @DisplayName("Every permanent targeting spell or enter-the-battlefield effect classifies")
    void everyPermanentTargetingSpellOrEtbEffectClassifies() {
        GameTestHarness harness = new GameTestHarness();
        GameData gd = harness.getGameData();
        UUID aiPlayerId = harness.getPlayer2().getId();
        TargetPolarityClassifier classifier = createClassifier(harness);

        Map<String, List<String>> unclassified = new TreeMap<>();
        Set<Class<?>> seenCardClasses = new HashSet<>();
        for (List<CardPrinting> printings : CardScanner.scan().values()) {
            for (CardPrinting printing : printings) {
                Card card = printing.factory().get();
                if (!seenCardClasses.add(card.getClass())) {
                    continue;
                }
                List<CardEffect> effects = new ArrayList<>(card.getCastingCostsAndSpellEffects());
                effects.addAll(card.getTriggerEffects(Triggers.enters(ObjectRelation.THIS)));
                for (CardEffect effect : effects) {
                    if (!effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
                        continue;
                    }
                    if (classifier.classify(gd, effect, aiPlayerId) == null) {
                        List<String> cards = unclassified.computeIfAbsent(
                                effect.getClass().getSimpleName(), k -> new ArrayList<>());
                        if (cards.size() < 5) {
                            cards.add(card.getClass().getSimpleName());
                        }
                    }
                }
            }
        }

        assertThat(unclassified)
                .withFailMessage("""
                        These permanent-targeting effect shapes have no TargetPolarity, so the AI \
                        would route them through the own-battlefield-first fallback (harmful spells \
                        would target the AI's own permanents). Add a rule for each to \
                        TargetPolarityClassifier.classify — HARMFUL_REMOVAL / HARMFUL_DAMAGE / \
                        HARMFUL aim at the opponent, BENEFICIAL at the AI's own board, NEUTRAL \
                        keeps the fallback deliberately. Unclassified (effect -> example cards): %s"""
                        .formatted(unclassified))
                .isEmpty();
    }

    @Test
    @DisplayName("A destroy step over every target, the first step of a destroy-and-return, is harmful removal")
    void destroyAndReturnTargetsOpposingPermanents() {
        GameTestHarness harness = new GameTestHarness();
        TargetPolarityClassifier classifier = createClassifier(harness);
        assertThat(classifier.classify(harness.getGameData(),
                new com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect(
                        com.github.laxika.magicalvibes.model.effect.Subjects.allTargets()),
                harness.getPlayer2().getId()))
                .isEqualTo(TargetPolarity.HARMFUL_REMOVAL);
    }

    @Test
    @DisplayName("Classifies positional boosts and blocking restrictions")
    void classifiesPositionalBoostsAndBlockingRestrictions() {
        GameTestHarness harness = new GameTestHarness();
        GameData gd = harness.getGameData();
        UUID aiPlayerId = harness.getPlayer2().getId();
        TargetPolarityClassifier classifier = createClassifier(harness);

        assertThat(classifier.classify(gd, new BoostTargetCreaturesByPositionEffect(List.of(
                BoostEffect.target(3, 3), BoostEffect.target(2, 2))), aiPlayerId))
                .isEqualTo(TargetPolarity.BENEFICIAL);
        assertThat(classifier.classify(gd, new BoostTargetCreaturesByPositionEffect(List.of(
                BoostEffect.target(3, 3), BoostEffect.target(-1, -1))), aiPlayerId))
                .isEqualTo(TargetPolarity.HARMFUL);
        assertThat(classifier.classify(gd, new MakeCreatureBlockableOnlyByFilterThisTurnEffect(
                new com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate(CardSubtype.WALL),
                "Walls"), aiPlayerId)).isEqualTo(TargetPolarity.BENEFICIAL);
        assertThat(classifier.classify(gd, BecomeCopyOfSubjectEffect.of(Subjects.target(1)).by(Subjects.target(0)), aiPlayerId))
                .isEqualTo(TargetPolarity.NEUTRAL);
    }

    @Test
    @DisplayName("Classifies every die roll branch and prioritizes harm over benefits")
    void classifiesEveryDieRollBranchAndPrioritizesHarmOverBenefits() {
        GameTestHarness harness = new GameTestHarness();
        GameData gd = harness.getGameData();
        UUID aiPlayerId = harness.getPlayer2().getId();
        TargetPolarityClassifier classifier = createClassifier(harness);

        for (int branchIndex = 0; branchIndex < 4; branchIndex++) {
            CardEffect[] branches = {null, null, null, null};
            branches[branchIndex] = ReturnToHandEffect.target();
            branches[(branchIndex + 1) % branches.length] = BoostEffect.target(1, 1);
            RollD20Effect roll = RollD20Effect.withSubtractedAmount(new Fixed(1),
                    branches[0], branches[1], branches[2], branches[3]);

            assertThat(classifier.classify(gd, roll, aiPlayerId))
                    .as("removal in d20 branch %s", branchIndex)
                    .isEqualTo(TargetPolarity.HARMFUL_REMOVAL);
        }
        assertThat(classifier.classify(gd, new RollD20Effect(null,
                BoostEffect.target(1, 1)), aiPlayerId))
                .isEqualTo(TargetPolarity.BENEFICIAL);
    }

    @Test
    @DisplayName("Preventing the next damage a target creature would deal aims at an opponent's creature")
    void preventingATargetCreaturesNextDamageIsHarmful() {
        GameTestHarness harness = new GameTestHarness();
        TargetPolarityClassifier classifier = createClassifier(harness);

        assertThat(classifier.classifyCard(harness.getGameData(),
                new com.github.laxika.magicalvibes.cards.a.AweStrike(), harness.getPlayer2().getId()))
                .isEqualTo(TargetPolarity.HARMFUL);
        assertThat(classifier.classifyCard(harness.getGameData(),
                new com.github.laxika.magicalvibes.cards.d.DazzlingReflection(), harness.getPlayer2().getId()))
                .isEqualTo(TargetPolarity.HARMFUL);
    }

    @Test
    @DisplayName("Damage to each target is harmful damage; damage to each creature a target player controls aims at no permanent")
    void setDamagePolarity() {
        GameTestHarness harness = new GameTestHarness();
        TargetPolarityClassifier classifier = createClassifier(harness);
        GameData gd = harness.getGameData();
        UUID aiPlayerId = harness.getPlayer2().getId();

        assertThat(classifier.classifyCard(gd, new com.github.laxika.magicalvibes.cards.j.JayasImmolatingInferno(),
                aiPlayerId)).isEqualTo(TargetPolarity.HARMFUL_DAMAGE);
        assertThat(classifier.classifyCard(gd, new com.github.laxika.magicalvibes.cards.s.Simoon(), aiPlayerId))
                .isNull();
    }

    private TargetPolarityClassifier createClassifier(GameTestHarness harness) {
        AmountEvaluationService amountEvaluationService = new AmountEvaluationService(
                new PredicateEvaluationService(harness.getGameQueryService()), harness.getGameQueryService());
        return new TargetPolarityClassifier(amountEvaluationService);
    }
}
