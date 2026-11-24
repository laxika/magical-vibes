package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.service.battlefield.DelayedPermanentActions;
import com.github.laxika.magicalvibes.service.cast.PerpetualCardCastCostSupport;
import com.github.laxika.magicalvibes.testutil.FloatingCombatRules;
import com.github.laxika.magicalvibes.model.effect.SkipKind;
import com.github.laxika.magicalvibes.testutil.DelayedPermanentMoves;
import com.github.laxika.magicalvibes.testutil.TestStackDoor;

import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.AlternativeCostKind;
import com.github.laxika.magicalvibes.model.ExileCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.model.GrantedAlternateCast;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.BattlefieldEntryCard;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPowerToughnessModifier;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DepartureBatch;
import com.github.laxika.magicalvibes.model.DepartureCause;
import com.github.laxika.magicalvibes.model.DepartureIntent;
import com.github.laxika.magicalvibes.model.DepartureSnapshot;
import com.github.laxika.magicalvibes.model.DepartureStage;
import com.github.laxika.magicalvibes.model.EachPlayerRummageState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.EmpowerNextCreatureSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToNextMatchingSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GrantedCardAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastTopInstantOrSorceryFromGraveyardUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessBoostEffect;
import com.github.laxika.magicalvibes.model.effect.OnlyLandCreaturesCanAttackThisCombatEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.rules.DepartedAttachments;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.StagedHandExile;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.rules.RulesEventBatch;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeEvent;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("scryfall")
class GameDataDeepCopyTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
    }

    @Test
    @DisplayName("Deep copy keeps a card choice step's answers and what a parked resolution chose")
    void deepCopyKeepsCardChoiceAndResolutionChoices() {
        UUID picked = UUID.randomUUID();
        gd.graveyardTargetOperation.chooseCardResume = true;
        gd.graveyardTargetOperation.chooseCardAnswer = List.of(picked);
        gd.graveyardTargetOperation.chooseCardPicked = List.of(picked);
        gd.graveyardTargetOperation.chooseCardIndex = 2;
        StackEntry parked = new StackEntry(
                StackEntryType.SORCERY_SPELL, new GrizzlyBears(), player1.getId(), "Parked spell", List.of());
        parked.setChosenParity(com.github.laxika.magicalvibes.model.ManaValueParity.ODD);
        parked.setChosenCardName("Grizzly Bears");
        parked.setChosenCardType(com.github.laxika.magicalvibes.model.CardType.ARTIFACT);
        gd.pendingEffectResolutionEntry = parked;

        GameData copy = gd.simulationCopy();

        assertThat(copy.graveyardTargetOperation.chooseCardResume).isTrue();
        assertThat(copy.graveyardTargetOperation.chooseCardAnswer).containsExactly(picked);
        assertThat(copy.graveyardTargetOperation.chooseCardPicked).containsExactly(picked);
        assertThat(copy.graveyardTargetOperation.chooseCardIndex).isEqualTo(2);
        assertThat(copy.pendingEffectResolutionEntry.getChosenParity())
                .isEqualTo(com.github.laxika.magicalvibes.model.ManaValueParity.ODD);
        assertThat(copy.pendingEffectResolutionEntry.getChosenCardName()).isEqualTo("Grizzly Bears");
        assertThat(copy.pendingEffectResolutionEntry.getChosenCardType())
                .isEqualTo(com.github.laxika.magicalvibes.model.CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Deep copy keeps the damage shields in force and reduces them independently")
    void deepCopyKeepsShieldsInForce() {
        UUID playerId = UUID.randomUUID();
        gd.addPlayerDamagePreventionShield("Test", playerId, playerId, 3, false);
        gd.addShield(new com.github.laxika.magicalvibes.model.ChannelHarmShield(playerId, null, UUID.randomUUID()));

        GameData copy = gd.simulationCopy();
        int prevented = copy.usePlayerDamagePreventionShields(playerId, false, 2);
        copy.endShields(com.github.laxika.magicalvibes.model.ChannelHarmShield.class, shield -> true);

        assertThat(prevented).isEqualTo(2);
        assertThat(copy.playerDamagePreventionShield(playerId, false)).isEqualTo(1);
        assertThat(copy.hasShields(com.github.laxika.magicalvibes.model.ChannelHarmShield.class)).isFalse();
        assertThat(gd.playerDamagePreventionShield(playerId, false)).isEqualTo(3);
        assertThat(gd.hasShields(com.github.laxika.magicalvibes.model.ChannelHarmShield.class)).isTrue();
        assertThat(gd.simulationCopy().playerDamagePreventionShield(playerId, false)).isEqualTo(3);
    }

    @Test
    @DisplayName("Deep copy keeps a permanent's prevention and regeneration shields and uses them independently")
    void deepCopyKeepsPermanentShields() {
        UUID permanentId = UUID.randomUUID();
        gd.addPermanentDamagePreventionShield(permanentId, 3);
        gd.addRegenerationShields(permanentId, 1);

        GameData copy = gd.simulationCopy();
        copy.usePermanentDamagePreventionShields(permanentId, false, 2);
        copy.useOldestEffectOnPermanent(permanentId, com.github.laxika.magicalvibes.model.RegenerationShield.class);

        assertThat(copy.permanentDamagePreventionShield(permanentId)).isEqualTo(1);
        assertThat(copy.regenerationShieldCount(permanentId)).isZero();
        assertThat(gd.permanentDamagePreventionShield(permanentId)).isEqualTo(3);
        assertThat(gd.regenerationShieldCount(permanentId)).isEqualTo(1);
    }

    @Test
    @DisplayName("Deep copy keeps floating effects that begin later in an independent list")
    void deepCopyKeepsScheduledFloatingEffects() {
        FloatingContinuousEffect scheduled = gd.addFloatingEffect(FloatingContinuousEffect.onPlayer("Test", null,
                player1.getId(), new GainLifeEffect(1), player2.getId(), Duration.DURING_AFFECTED_PLAYERS_NEXT_TURN));

        GameData copy = gd.simulationCopy();
        copy.scheduledFloatingEffects.clear();

        assertThat(gd.scheduledFloatingEffects).containsExactly(scheduled);
        assertThat(gd.simulationCopy().scheduledFloatingEffects).containsExactly(scheduled);
    }

    @Test
    @DisplayName("Deep copy keeps a permanent's \"doesn't untap during its next untap steps\" effects independent")
    void deepCopyKeepsNextUntapStepSkipsIndependent() {
        UUID permanentId = UUID.randomUUID();
        gd.skipNextUntapSteps("Test", null, player1.getId(), permanentId, 2, null);

        GameData copy = gd.simulationCopy();
        copy.useNextUntapStepSkips(permanentId, player1.getId());

        assertThat(copy.nextUntapStepsSkipped(permanentId)).isEqualTo(1);
        assertThat(gd.nextUntapStepsSkipped(permanentId)).isEqualTo(2);
    }

    @Test
    @DisplayName("Deep copy keeps the effects a departed permanent had as it left, in independent lists")
    void deepCopyKeepsDepartedPermanentEffectsIndependent() {
        UUID departedId = UUID.randomUUID();
        FloatingContinuousEffect boost = gd.addPowerToughnessBoost("Test", null, player1.getId(), departedId, 2, 2,
                Duration.UNTIL_END_OF_TURN);
        gd.endFloatingEffectsOnDepartedPermanent(departedId);

        GameData copy = gd.simulationCopy();
        copy.departedPermanentEffects.get(departedId).clear();

        assertThat(gd.floatingEffectsOnPermanent(departedId, PowerToughnessBoostEffect.class)).containsExactly(boost);
        assertThat(gd.simulationCopy().floatingEffectsOnPermanent(departedId, PowerToughnessBoostEffect.class))
                .containsExactly(boost);
    }

    @Test
    @DisplayName("Deep copy keeps a departed permanent's last-known card on an independent snapshot")
    void deepCopyKeepsDepartedSnapshotLastKnownCard() {
        Card card = new GrizzlyBears();
        Permanent departed = new Permanent(card);
        Card lastKnown = card.createRuntimeCopy();
        departed.setLastKnownCard(lastKnown);
        gd.departedPermanentSnapshots.put(card.getId(), departed);

        GameData copy = gd.simulationCopy();
        copy.departedPermanentSnapshots.get(card.getId()).setLastKnownCard(null);

        assertThat(gd.departedPermanentSnapshots.get(card.getId()).getLastKnownCard()).isSameAs(lastKnown);
        assertThat(gd.simulationCopy().departedPermanentSnapshots.get(card.getId()).getLastKnownCard())
                .isSameAs(lastKnown);
    }

    @Test
    @DisplayName("Deep copy preserves primitive fields")
    void deepCopyPreservesPrimitives() {
        gd.turnNumber = 5;
        gd.currentStep = TurnStep.POSTCOMBAT_MAIN;
        gd.activePlayerId = player1.getId();
        gd.playerLifeTotals.put(player1.getId(), 15);
        gd.playerLifeTotals.put(player2.getId(), 8);

        GameData copy = gd.simulationCopy();

        assertThat(copy.turnNumber).isEqualTo(5);
        assertThat(copy.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        assertThat(copy.activePlayerId).isEqualTo(player1.getId());
        assertThat(copy.playerLifeTotals.get(player1.getId())).isEqualTo(15);
        assertThat(copy.playerLifeTotals.get(player2.getId())).isEqualTo(8);
    }

    @Test
    @DisplayName("Deep copy keeps delayed abilities with no stated duration in an independent list")
    void deepCopyKeepsUntilTriggeredDelayedAbilitiesIndependently() {
        Card source = new GrizzlyBears();
        UUID watchedId = UUID.randomUUID();
        TemporaryGlobalTriggeredAbility delayed = TemporaryGlobalTriggeredAbility.about(player1.getId(), source,
                new TriggeredAbility(Triggers.leavesBattlefield(ObjectRelation.THIS), new DrawCardEffect(1)),
                watchedId, null, false);
        gd.temporaryGlobalTriggeredAbilities.add(delayed);

        GameData copy = gd.simulationCopy();
        copy.temporaryGlobalTriggeredAbilities.clear();

        assertThat(gd.temporaryGlobalTriggeredAbilities).containsExactly(delayed);
        assertThat(gd.simulationCopy().temporaryGlobalTriggeredAbilities).singleElement()
                .satisfies(copied -> {
                    assertThat(copied.untilTriggered()).isTrue();
                    assertThat(copied.watchedPermanentId()).isEqualTo(watchedId);
                });
    }

    @Test
    @DisplayName("Deep copy keeps the counters of every kind on exiled cards, time counters included, and the copy's changes stay its own")
    void deepCopyKeepsExiledCardCountersIndependently() {
        UUID cardId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        gd.setExiledCardCounters(cardId, CounterType.DELAY, 2);
        gd.addExiledCardCounters(cardId, CounterType.ICE, 1);
        gd.addExiledCardCounters(otherId, CounterType.SILVER, 1);
        gd.setExiledCardCounters(otherId, CounterType.TIME, 3);

        GameData copy = gd.simulationCopy();
        copy.setExiledCardCounters(cardId, CounterType.DELAY, 1);
        copy.setExiledCardCounters(otherId, CounterType.TIME, 2);
        copy.setExiledCardCounters(cardId, CounterType.ICE, 0);
        copy.addExiledCardCounters(otherId, CounterType.HIT, 3);

        assertThat(gd.exiledCardCounterCount(cardId, CounterType.DELAY)).isEqualTo(2);
        assertThat(gd.exiledCardHasCounter(cardId, CounterType.ICE)).isTrue();
        assertThat(gd.exiledCardHasCounter(otherId, CounterType.HIT)).isFalse();
        assertThat(copy.exiledCardCounterCount(cardId, CounterType.DELAY)).isEqualTo(1);
        assertThat(copy.exiledCardHasCounter(cardId, CounterType.ICE)).isFalse();
        assertThat(copy.exiledCardCounterCount(otherId, CounterType.HIT)).isEqualTo(3);
        assertThat(copy.exiledCardHasCounter(otherId, CounterType.SILVER)).isTrue();
        assertThat(gd.exiledCardCounterCount(otherId, CounterType.TIME)).isEqualTo(3);
        assertThat(copy.exiledCardCounterCount(otherId, CounterType.TIME)).isEqualTo(2);
        assertThat(gd.simulationCopy().exiledCardsWithCounter(CounterType.DELAY)).containsEntry(cardId, 2);
    }

    @Test
    @DisplayName("Deep copy creates independent battlefield collections")
    void deepCopyIndependentBattlefields() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        GameData copy = gd.simulationCopy();

        // Verify the copy has the creature
        assertThat(copy.playerBattlefields.get(player1.getId())).hasSize(
                gd.playerBattlefields.get(player1.getId()).size());

        // Modify the copy — original should be unaffected
        copy.playerBattlefields.get(player1.getId()).clear();
        assertThat(gd.playerBattlefields.get(player1.getId())).isNotEmpty();
    }

    @Test
    @DisplayName("Deep copy creates independent Permanent objects")
    void deepCopyIndependentPermanents() {
        harness.addToBattlefield(player1, new SerraAngel());

        GameData copy = gd.simulationCopy();

        // Modify the copy's permanent
        Permanent copyPerm = copy.playerBattlefields.get(player1.getId()).getFirst();
        copyPerm.tap();

        // Original should be unaffected
        Permanent origPerm = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(origPerm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Deep copy and snapshots keep which alternative cost a permanent and a spell were cast for")
    void deepCopyKeepsAlternativeCostKind() {
        Permanent dashed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        dashed.setAlternativeCost(AlternativeCostKind.DASH);
        StackEntry spell = TestStackDoor.triggered(new SerraAngel(), player1.getId(), "Spell", List.of());
        spell.setAlternativeCost(AlternativeCostKind.DASH);
        gd.stack.add(spell);

        GameData copy = gd.simulationCopy();

        assertThat(copy.playerBattlefields.get(player1.getId()).getFirst().isDashCostPaid()).isTrue();
        assertThat(copy.stack.getFirst().isDashCostPaid()).isTrue();
        assertThat(new Permanent(dashed).isDashCostPaid()).isTrue();
        dashed.setAlternateCost(true);
        assertThat(dashed.isDashCostPaid()).isTrue();
        dashed.setAlternateCost(false);
        assertThat(dashed.isAlternateCost()).isFalse();
    }

    @Test
    @DisplayName("Deep copy keeps how many of its controller's upkeeps began since a permanent came under control")
    void deepCopyPreservesControllerUpkeepsSinceControlled() {
        Permanent origPerm = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        origPerm.markControllerUpkeepStarted();

        GameData copy = gd.simulationCopy();
        Permanent copyPerm = copy.playerBattlefields.get(player1.getId()).getFirst();
        copyPerm.markControllerUpkeepStarted();

        assertThat(copyPerm.getControllerUpkeepsSinceControlled()).isEqualTo(2);
        assertThat(origPerm.getControllerUpkeepsSinceControlled()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deep copy keeps a permanent's granted abilities, and the copy's grants stay its own")
    void deepCopyPreservesGrantedAbilitiesIndependently() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent origPerm = gd.playerBattlefields.get(player1.getId()).getFirst();
        PermanentGrants.abilityUntilEndOfTurn(gd, origPerm,
                new TriggeredAbility(Triggers.youLoseControlOfThis(), new DrawCardEffect(1)));
        PermanentGrants.ability(gd, origPerm,
                new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU), new DrawCardEffect(2)));

        GameData copy = gd.simulationCopy();
        Permanent copyPerm = copy.playerBattlefields.get(player1.getId()).getFirst();
        PermanentGrants.ability(copy, copyPerm,
                new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU), new DrawCardEffect(4)));

        assertThat(PermanentGrants.grantedTriggerEffects(copy, copyPerm, Triggers.youLoseControlOfThis()))
                .containsExactly(new DrawCardEffect(1));
        assertThat(PermanentGrants.grantedTriggerEffects(copy, copyPerm, Triggers.upkeep(ActivePlayer.YOU)))
                .containsExactly(new DrawCardEffect(2), new DrawCardEffect(4));
        assertThat(PermanentGrants.grantedTriggerEffects(gd, origPerm, Triggers.upkeep(ActivePlayer.YOU)))
                .containsExactly(new DrawCardEffect(2));
    }

    @Test
    @DisplayName("Deep copy keeps whether a triggering permanent had lost its printed abilities as it left")
    void deepCopyKeepsLastKnownLostPrintedAbilities() {
        Permanent dying = new Permanent(new GrizzlyBears());
        dying.setLastKnownLostPrintedAbilities(true);
        StackEntry entry = TestStackDoor.triggered(new SerraAngel(), player1.getId(), "Death trigger", List.of());
        entry.setTriggeringPermanent(dying, new DepartedAttachments(List.of(), List.of(), java.util.Map.of()));
        gd.stack.add(entry);

        GameData copy = gd.simulationCopy();

        assertThat(copy.stack.getFirst().getTriggeringPermanentSnapshot().isLastKnownLostPrintedAbilities()).isTrue();
    }

    @Test
    @DisplayName("Deep copy keeps perpetual ability grants, and the copy's grants stay its own")
    void deepCopyPreservesPerpetualGrantsIndependently() {
        Card card = new GrizzlyBears();
        gd.grantPerpetualAbility(card.getId(),
                new TriggeredAbility(Triggers.youLoseControlOfThis(), new DrawCardEffect(1)));

        GameData copy = gd.simulationCopy();
        copy.grantPerpetualAbility(card.getId(),
                new TriggeredAbility(Triggers.youLoseControlOfThis(), new DrawCardEffect(2)));

        assertThat(copy.perpetualTriggerEffects(card.getId(), Triggers.youLoseControlOfThis(), Zone.BATTLEFIELD))
                .containsExactly(new DrawCardEffect(1), new DrawCardEffect(2));
        assertThat(gd.perpetualTriggerEffects(card.getId(), Triggers.youLoseControlOfThis(), Zone.BATTLEFIELD))
                .containsExactly(new DrawCardEffect(1));
    }

    @Test
    @DisplayName("Deep copy keeps perpetual keywords and P/T changes, and the copy's changes stay its own")
    void deepCopyPreservesPerpetualKeywordsAndPowerToughnessIndependently() {
        Card card = new GrizzlyBears();
        gd.grantPerpetualKeywords(card.getId(), Set.of(Keyword.HASTE));
        gd.perpetualCardPowerToughnessModifiers.put(card.getId(), new CardPowerToughnessModifier(1, 1));

        GameData copy = gd.simulationCopy();
        copy.grantPerpetualKeywords(card.getId(), Set.of(Keyword.FLYING));
        copy.perpetualCardPowerToughnessModifiers.merge(card.getId(), new CardPowerToughnessModifier(2, 2),
                (current, added) -> current.add(added.power(), added.toughness()));

        assertThat(copy.perpetualCardKeywords.get(card.getId()))
                .containsExactlyInAnyOrder(Keyword.HASTE, Keyword.FLYING);
        assertThat(gd.perpetualCardKeywords.get(card.getId())).containsExactly(Keyword.HASTE);
        assertThat(copy.perpetualCardPowerToughnessModifiers.get(card.getId()))
                .isEqualTo(new CardPowerToughnessModifier(3, 3));
        assertThat(gd.perpetualCardPowerToughnessModifiers.get(card.getId()))
                .isEqualTo(new CardPowerToughnessModifier(1, 1));
    }

    @Test
    @DisplayName("Deep copy keeps perpetual cast-cost changes in their one store, and the copy's changes stay its own")
    void deepCopyPreservesPerpetualCastCostChangesIndependently() {
        Card cheaper = new GrizzlyBears();
        Card dearer = new GrizzlyBears();
        PerpetualCardCastCostSupport.remember(gd, cheaper, 1);
        PerpetualCardCastCostSupport.rememberIncrease(gd, dearer, 2);

        GameData copy = gd.simulationCopy();
        PerpetualCardCastCostSupport.remember(copy, cheaper, 2);
        PerpetualCardCastCostSupport.remember(copy, dearer, 2);

        assertThat(PerpetualCardCastCostSupport.changeFor(copy, cheaper)).isEqualTo(-3);
        assertThat(copy.perpetualGenericCastCostIncreases).doesNotContainKey(dearer.getId());
        assertThat(PerpetualCardCastCostSupport.changeFor(gd, cheaper)).isEqualTo(-1);
        assertThat(PerpetualCardCastCostSupport.changeFor(gd, dearer)).isEqualTo(2);
    }

    @Test
    @DisplayName("Deep copy keeps perpetual alternative costs in their one store, and the copy's grants stay its own")
    void deepCopyPreservesPerpetualAlternateCastsIndependently() {
        Card card = new GrizzlyBears();
        GrantedAlternateCast evoke = new GrantedAlternateCast(new AlternateHandCast(List.of(
                new ExileCardsFromHandCastingCost(null, "a card"))), true);
        GrantedAlternateCast red = new GrantedAlternateCast(new AlternateHandCast(List.of(
                new ManaCastingCost("{R}"))), false);
        gd.grantPerpetualAlternateCast(card.getId(), evoke);

        GameData copy = gd.simulationCopy();
        copy.grantPerpetualAlternateCast(card.getId(), red);
        copy.grantPerpetualAlternateCast(card.getId(), evoke);

        assertThat(copy.perpetualAlternateCasts.get(card.getId())).containsExactly(evoke, red);
        assertThat(gd.perpetualAlternateCasts.get(card.getId())).containsExactly(evoke);
    }

    @Test
    @DisplayName("Deep copy keeps perpetually granted graveyard abilities, and the copy's grants stay its own")
    void deepCopyPreservesPerpetualGraveyardAbilityGrantsIndependently() {
        Card card = new GrizzlyBears();
        ActivatedAbility unearth = Card.unearthAbility("{5}").withZones(AbilityZones.GRAVEYARD);
        gd.grantPerpetualAbility(card.getId(), unearth);

        GameData copy = gd.simulationCopy();
        ActivatedAbility second = Card.unearthAbility("{2}").withZones(AbilityZones.GRAVEYARD);
        copy.grantPerpetualAbility(card.getId(), second);

        assertThat(copy.perpetualAbilities(card.getId())).containsExactly(unearth, second);
        assertThat(gd.perpetualAbilities(card.getId())).containsExactly(unearth);
    }

    @Test
    @DisplayName("Deep copy keeps pending rules events and known zones, emits nothing itself and carries no observer")
    void deepCopyPreservesPendingRulesEventsIndependently() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card drawn = gd.playerDecks.get(player1.getId()).getFirst();
        gd.drainRulesEvents();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bears));

        GameData copy = gd.simulationCopy();

        assertThat(copy.rulesObserver).isNull();
        List<RulesEventBatch> pending = copy.rulesEvents.pending();
        assertThat(pending).hasSize(1);
        ZoneChangeEvent copied = (ZoneChangeEvent) pending.getFirst().events().getFirst();
        ZoneChangeEvent original = (ZoneChangeEvent) gd.rulesEvents.pending().getFirst().events().getFirst();
        assertThat(copied.to()).isEqualTo(Zone.HAND);
        assertThat(copied.permanent().getId()).isEqualTo(bears.getId());
        assertThat(copied.permanent()).isNotSameAs(original.permanent());

        copy.playerDecks.get(player1.getId()).removeFirst();
        copy.playerHands.get(player1.getId()).add(drawn);
        copy.drainRulesEvents();

        assertThat(copy.rulesEvents.pending()).isEmpty();
        assertThat(gd.rulesEvents.pending()).hasSize(1);
        assertThat(copy.rulesEvents.zoneOf(drawn.getId())).isEqualTo(Zone.HAND);
        assertThat(gd.rulesEvents.zoneOf(drawn.getId())).isEqualTo(Zone.LIBRARY);
    }

    @Test
    @DisplayName("Deep copy preserves Card reference sharing")
    void deepCopySharesCardReferences() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        GameData copy = gd.simulationCopy();

        Card origCard = gd.playerBattlefields.get(player1.getId()).getFirst().getCard();
        Card copyCard = copy.playerBattlefields.get(player1.getId()).getFirst().getCard();

        // Same Card object (not deep copied — immutable)
        assertThat(copyCard).isSameAs(origCard);
    }

    @Test
    @DisplayName("Deep copy preserves Permanent IDs (references stay valid)")
    void deepCopyPreservesPermanentIds() {
        harness.addToBattlefield(player1, new SerraAngel());

        GameData copy = gd.simulationCopy();

        Permanent origPerm = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent copyPerm = copy.playerBattlefields.get(player1.getId()).getFirst();

        assertThat(copyPerm.getId()).isEqualTo(origPerm.getId());
    }

    @Test
    @DisplayName("Deep copy creates independent hand collections")
    void deepCopyIndependentHands() {
        GameData copy = gd.simulationCopy();

        int origHandSize = gd.playerHands.get(player1.getId()).size();
        copy.playerHands.get(player1.getId()).clear();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(origHandSize);
    }

    @Test
    @DisplayName("Deep copy preserves mana pool values independently")
    void deepCopyIndependentManaPools() {
        harness.addMana(player1, ManaColor.RED, 3);

        GameData copy = gd.simulationCopy();

        assertThat(copy.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);

        // Modify copy
        copy.playerManaPools.get(player1.getId()).add(ManaColor.RED);

        // Original unchanged
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    @DisplayName("Deep copy preserves life totals independently")
    void deepCopyIndependentLifeTotals() {
        gd.playerLifeTotals.put(player1.getId(), 12);

        GameData copy = gd.simulationCopy();
        copy.playerLifeTotals.put(player1.getId(), 5);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Deep copy preserves player ordering")
    void deepCopyPreservesPlayerOrder() {
        GameData copy = gd.simulationCopy();

        assertThat(copy.orderedPlayerIds).containsExactlyElementsOf(gd.orderedPlayerIds);
        assertThat(copy.playerIds).containsExactlyInAnyOrderElementsOf(gd.playerIds);
    }

    @Test
    @DisplayName("Deep copy preserves stack entries independently")
    void deepCopyIndependentStack() {
        // The stack is empty after setup, verify copy is also empty
        GameData copy = gd.simulationCopy();
        assertThat(copy.stack).isEmpty();
        assertThat(copy.stack).isNotSameAs(gd.stack);
    }

    @Test
    @DisplayName("Deep copy keeps the attachments recorded for a departed permanent on an entry and a delayed ability")
    void deepCopyKeepsRecordedAttachmentsOfDepartedPermanent() {
        UUID departedId = UUID.randomUUID();
        UUID equipmentId = UUID.randomUUID();
        DepartedAttachments attachments = new DepartedAttachments(List.of(), List.of(equipmentId));
        StackEntry entry = TestStackDoor.triggered(new SerraAngel(), player1.getId(), "Block trigger", List.of());
        entry.rememberLastKnownPermanentAttachments(departedId, attachments);
        gd.stack.add(entry);
        gd.temporaryGlobalTriggeredAbilities.add(
                com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility.atNextStep(
                        com.github.laxika.magicalvibes.model.trigger.Triggers.endOfCombat(), player1.getId(),
                        new SerraAngel(), null, departedId,
                        new com.github.laxika.magicalvibes.model.effect.DrawCardEffect(1))
                        .withReferents(com.github.laxika.magicalvibes.model.DelayedReferents
                                .permanents(List.of(departedId)).withPermanentAttachments(attachments)));

        GameData copy = gd.simulationCopy();

        assertThat(copy.stack.getFirst()).isNotSameAs(entry);
        assertThat(copy.stack.getFirst().lastKnownPermanentAttachments(departedId).equipmentPermanentIds())
                .containsExactly(equipmentId);
        assertThat(copy.temporaryGlobalTriggeredAbilities).singleElement()
                .satisfies(delayed -> assertThat(delayed.referents().permanentAttachments().equipmentPermanentIds())
                        .containsExactly(equipmentId));
    }

    @Test
    @DisplayName("Deep copy keeps the record of an ability the stack door parked for its target, independently")
    void deepCopyKeepsParkedTriggerRecord() {
        StackEntry record = TestStackDoor.triggered(new SerraAngel(), player1.getId(), "Block trigger", List.of());
        record.setParkedAtStackDoor(true);
        TestStackDoor.handOff(gd, record);
        gd.queueInteraction(new com.github.laxika.magicalvibes.model.PermanentChoiceContext.AttackTriggerTarget(
                new SerraAngel(), player1.getId(), List.of(), null, player1.getId(), null, null)
                .withTriggerRecord(record));

        GameData copy = gd.simulationCopy();
        StackEntry copied = copy.stackEntriesAndTriggerRecords().getFirst();

        assertThat(copied).isNotSameAs(record);
        assertThat(copied.isParkedAtStackDoor()).isTrue();
        copied.setParkedAtStackDoor(false);
        assertThat(record.isParkedAtStackDoor()).isTrue();
    }

    @Test
    @DisplayName("Deep copy keeps a \"when you next ...\" delayed ability, which triggers once, in a list of its own")
    void deepCopyKeepsNextDelayedAbilities() {
        TemporaryGlobalTriggeredAbility next = TemporaryGlobalTriggeredAbility.nextThisTurn(player1.getId(),
                new SerraAngel(), new CopyNextInstantOrSorceryCastThisTurnEffect(2).delayedAbility(), null);
        gd.temporaryGlobalTriggeredAbilities.add(next);

        GameData copy = gd.simulationCopy();
        assertThat(copy.temporaryGlobalTriggeredAbilities).singleElement()
                .satisfies(delayed -> assertThat(delayed.triggersOnce()).isTrue());
        copy.temporaryGlobalTriggeredAbilities.clear();

        assertThat(gd.temporaryGlobalTriggeredAbilities).containsExactly(next);
    }

    @Test
    @DisplayName("Deep copy keeps a boon's remaining uses, and using it in the copy leaves the original's")
    void deepCopyKeepsBoonUses() {
        TemporaryGlobalTriggeredAbility boon = TemporaryGlobalTriggeredAbility.boon(player1.getId(), new SerraAngel(),
                new TriggeredAbility(Triggers.youSacrificeOneOrMorePermanents(), new DrawCardEffect(1)), null, 2);
        gd.temporaryGlobalTriggeredAbilities.add(boon);

        GameData copy = gd.simulationCopy();
        copy.useDelayedAbility(copy.temporaryGlobalTriggeredAbilities.getFirst());

        assertThat(copy.temporaryGlobalTriggeredAbilities).singleElement()
                .satisfies(delayed -> assertThat(delayed.uses()).isEqualTo(1));
        assertThat(gd.temporaryGlobalTriggeredAbilities).containsExactly(boon);
        assertThat(boon.uses()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deep copy keeps a parked record's mark of an ability a resolution made")
    void deepCopyKeepsMadeByResolutionMark() {
        StackEntry record = TestStackDoor.triggered(new SerraAngel(), player1.getId(), "Reflexive ability", List.of());
        record.setParkedAtStackDoor(true);
        record.setMadeByResolution(true);
        gd.queueInteraction(new com.github.laxika.magicalvibes.model.PermanentChoiceContext.AttackTriggerTarget(
                new SerraAngel(), player1.getId(), List.of(), null, player1.getId(), null, null)
                .withTriggerRecord(record));

        GameData copy = gd.simulationCopy();
        var copied = (com.github.laxika.magicalvibes.model.PermanentChoiceContext.AttackTriggerTarget)
                copy.pendingInteractions.getFirst();

        assertThat(copied.triggerRecord()).isNotSameAs(record);
        assertThat(copied.triggerRecord().isMadeByResolution()).isTrue();
        copied.triggerRecord().setMadeByResolution(false);
        assertThat(record.isMadeByResolution()).isTrue();
    }

    @Test
    @DisplayName("Deep copy gives a queued door choice its own record, with its planar source copied")
    void deepCopyGivesQueuedDoorChoiceItsOwnRecord() {
        StackEntry record = TestStackDoor.triggered(new SerraAngel(), player1.getId(), "Chaos trigger", List.of());
        record.setParkedAtStackDoor(true);
        record.setSourcePlanarObject(new com.github.laxika.magicalvibes.model.planar.PlanarObject(new SerraAngel(), 1));
        gd.queueInteraction(new com.github.laxika.magicalvibes.model.PermanentChoiceContext.AttackTriggerTarget(
                new SerraAngel(), player1.getId(), List.of(), null, player1.getId(), null, null)
                .withTriggerRecord(record));
        var original = (com.github.laxika.magicalvibes.model.PermanentChoiceContext.AttackTriggerTarget)
                gd.pendingInteractions.getFirst();

        GameData copy = gd.simulationCopy();
        var copied = (com.github.laxika.magicalvibes.model.PermanentChoiceContext.AttackTriggerTarget)
                copy.pendingInteractions.getFirst();

        assertThat(copied.triggerRecord()).isNotSameAs(original.triggerRecord());
        copied.triggerRecord().getSourcePlanarObject().getCounters()
                .put(com.github.laxika.magicalvibes.model.CounterType.CHARGE, 2);
        copied.triggerRecord().setParkedAtStackDoor(false);
        assertThat(original.triggerRecord().getSourcePlanarObject().getCounters()).isEmpty();
        assertThat(original.triggerRecord().isParkedAtStackDoor()).isTrue();
    }

    @Test
    @DisplayName("Deep copy keeps a triggered ability on the stack released by the stack door")
    void deepCopyKeepsTriggeredEntryReleasedToStack() {
        StackEntry entry = TestStackDoor.triggered(new SerraAngel(), player1.getId(), "Attack trigger", List.of());
        gd.stack.add(entry);

        GameData copy = gd.simulationCopy();

        assertThat(copy.stack.getFirst()).isNotSameAs(entry);
        assertThat(copy.stack.getFirst().isReleasedToStack()).isTrue();
    }

    @Test
    @DisplayName("Deep copy keeps whether a gift was promised for a stack entry")
    void deepCopyKeepsPromisedGift() {
        StackEntry entry = TestStackDoor.triggered(new SerraAngel(), player1.getId(), "Enters ability", List.of());
        entry.setGiftPromised(true);
        gd.stack.add(entry);

        GameData copy = gd.simulationCopy();

        assertThat(copy.stack.getFirst().isGiftPromised()).isTrue();
    }

    @Test
    @DisplayName("Deep copy gives a queued two-target choice the stack door parked a record of its own")
    void deepCopyKeepsParkedCounterMoveRecordIndependently() {
        StackEntry record = TestStackDoor.triggered(new SerraAngel(), player1.getId(), "Attack trigger", List.of());
        record.setParkedAtStackDoor(true);
        gd.queueInteraction(new com.github.laxika.magicalvibes.model.PermanentChoiceContext.AttackCounterMoveFirstTarget(
                new SerraAngel(), player1.getId(), List.of(), null, player2.getId(), record));

        GameData copy = gd.simulationCopy();
        var copied = copy.peekPendingInteraction(
                com.github.laxika.magicalvibes.model.PermanentChoiceContext.AttackCounterMoveFirstTarget.class);

        assertThat(copied.triggerRecord()).isNotSameAs(record);
        assertThat(copied.triggerRecord().isParkedAtStackDoor()).isTrue();
        copied.triggerRecord().setParkedAtStackDoor(false);
        assertThat(record.isParkedAtStackDoor()).isTrue();
    }

    @Test
    @DisplayName("Deep copy keeps whether a parked ability's attacking source caused it, independently")
    void deepCopyKeepsTriggeredBySourceAttacking() {
        StackEntry record = TestStackDoor.triggered(new SerraAngel(), player1.getId(), "Attack trigger", List.of());
        record.setParkedAtStackDoor(true);
        record.setTriggeredBySourceAttacking(true);
        TestStackDoor.handOff(gd, record);
        gd.queueInteraction(new com.github.laxika.magicalvibes.model.PermanentChoiceContext.AttackTriggerTarget(
                new SerraAngel(), player1.getId(), List.of(), null, player1.getId(), null, null)
                .withTriggerRecord(record));

        GameData copy = gd.simulationCopy();
        StackEntry copied = copy.stackEntriesAndTriggerRecords().getFirst();

        assertThat(copied.isTriggeredBySourceAttacking()).isTrue();
        copied.setTriggeredBySourceAttacking(false);
        assertThat(record.isTriggeredBySourceAttacking()).isTrue();
    }

    @Test
    @DisplayName("Deep copy keeps a stack entry's step results, and a step recorded on the copy stays in the copy")
    void deepCopyKeepsStepResultsIndependently() {
        StackEntry entry = TestStackDoor.triggered(new SerraAngel(), player1.getId(), "Resolving spell", List.of());
        UUID drawnCardId = UUID.randomUUID();
        entry.recordStep(StepKind.DRAW, StepOutcome.ofObjects(List.of(drawnCardId), List.of(player1.getId())));
        gd.stack.add(entry);

        GameData copy = gd.simulationCopy();
        StackEntry copied = copy.stack.getFirst();
        copied.recordStep(StepKind.DRAW, StepOutcome.NONE);
        copied.addToStep(StepKind.DEAL_DAMAGE, new StepOutcome(3, List.of(), List.of(player2.getId())));

        assertThat(entry.getResolutionResults().outcome(StepKind.DRAW).objectIds()).containsExactly(drawnCardId);
        assertThat(entry.getResolutionResults().outcome(StepKind.DEAL_DAMAGE)).isEqualTo(StepOutcome.NONE);
        assertThat(copied.getResolutionResults().outcome(StepKind.DEAL_DAMAGE).amount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Deep copy keeps the last-known keywords of a triggering permanent")
    void deepCopyKeepsLastKnownKeywordsOfTriggeringPermanent() {
        Permanent dying = new Permanent(new GrizzlyBears());
        dying.setLastKnownKeywords(Set.of(Keyword.LIFELINK));
        StackEntry entry = TestStackDoor.triggered(new SerraAngel(), player1.getId(), "Death trigger", List.of());
        entry.setTriggeringPermanent(dying, new DepartedAttachments(List.of(), List.of(), java.util.Map.of()));
        gd.stack.add(entry);

        GameData copy = gd.simulationCopy();

        assertThat(copy.stack.getFirst().getTriggeringPermanentSnapshot().getLastKnownKeywords())
                .containsExactly(Keyword.LIFELINK);
    }

    @Test
    @DisplayName("Deep copy keeps a stack entry's triggering permanent and attachments, independently")
    void deepCopyKeepsTriggeringPermanentIndependently() {
        Permanent dying = new Permanent(new GrizzlyBears());
        dying.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        UUID auraCardId = UUID.randomUUID();
        StackEntry entry = TestStackDoor.triggered(new SerraAngel(), player1.getId(), "Death trigger", List.of());
        entry.setTriggeringPermanent(dying, new DepartedAttachments(List.of(auraCardId), List.of(),
                java.util.Map.of(auraCardId, player2.getId())));
        gd.stack.add(entry);

        GameData copy = gd.simulationCopy();
        StackEntry copied = copy.stack.getFirst();
        copied.getTriggeringPermanentSnapshot().setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);

        assertThat(copied.getTriggeringPermanentSnapshot()).isNotSameAs(entry.getTriggeringPermanentSnapshot());
        assertThat(copied.getTriggeringAttachments().auraCardIds()).containsExactly(auraCardId);
        assertThat(copied.getTriggeringAttachments().auraCountControlledBy(player2.getId())).isEqualTo(1);
        assertThat(entry.triggeringPermanentCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Deep copy preserves the aliasing between the may-targeting entry and the suspended resolution entry")
    void deepCopyPreservesMayTargetingAliasing() {
        // CR 603.5 resolution-time targeting: the chosen target is set through
        // resolvedMayTargetingEntry and resolution resumes through
        // pendingEffectResolutionEntry — both must reference the same object.
        StackEntry suspended = TestStackDoor.triggered(new GrizzlyBears(), player1.getId(), "Test trigger", List.of());
        gd.pendingEffectResolutionEntry = suspended;
        gd.resolvedMayTargetingEntry = suspended;

        GameData copy = gd.simulationCopy();

        assertThat(copy.pendingEffectResolutionEntry).isNotSameAs(suspended);
        assertThat(copy.resolvedMayTargetingEntry).isSameAs(copy.pendingEffectResolutionEntry);
    }

    @Test
    @DisplayName("Deep copy keeps distinct may-targeting and suspended resolution entries independent")
    void deepCopyKeepsDistinctResolutionEntriesIndependent() {
        gd.pendingEffectResolutionEntry = TestStackDoor.triggered(new GrizzlyBears(), player1.getId(), "Suspended", List.of());
        gd.resolvedMayTargetingEntry = TestStackDoor.triggered(new SerraAngel(), player1.getId(), "Targeting", List.of());

        GameData copy = gd.simulationCopy();

        assertThat(copy.pendingEffectResolutionEntry).isNotSameAs(gd.pendingEffectResolutionEntry);
        assertThat(copy.resolvedMayTargetingEntry).isNotSameAs(gd.resolvedMayTargetingEntry);
        assertThat(copy.resolvedMayTargetingEntry).isNotSameAs(copy.pendingEffectResolutionEntry);
    }

    @Test
    @DisplayName("Deep copy keeps a parked resolution, its nested resolution and a parked spell's stack object linked")
    void deepCopyKeepsParkedResolutionsLinked() {
        // A sorcery that asked a question is parked, still on the stack, behind the nested resolution of the accepted
        // option, which is itself parked behind one more that waits for an answer.
        StackEntry spell = new StackEntry(
                StackEntryType.SORCERY_SPELL, new GrizzlyBears(), player1.getId(), "Parked spell", List.of());
        StackEntry firstNested = TestStackDoor.triggered(new GrizzlyBears(), player2.getId(), "First option", List.of());
        StackEntry secondNested = TestStackDoor.triggered(new SerraAngel(), player2.getId(), "Second option", List.of());
        gd.stack.add(spell);
        gd.parkedOuterResolutions.push(new GameData.ParkedResolution(spell, 2, firstNested));
        gd.parkedOuterResolutions.push(new GameData.ParkedResolution(firstNested, 1, secondNested));
        gd.pendingEffectResolutionEntry = secondNested;

        GameData copy = gd.simulationCopy();

        assertThat(copy.parkedOuterResolutions).hasSize(2);
        GameData.ParkedResolution inner = copy.parkedOuterResolutions.pop();
        GameData.ParkedResolution outer = copy.parkedOuterResolutions.pop();
        assertThat(inner.nested()).isSameAs(copy.pendingEffectResolutionEntry).isNotSameAs(secondNested);
        assertThat(inner.index()).isEqualTo(1);
        assertThat(inner.entry()).isSameAs(outer.nested()).isNotSameAs(firstNested);
        assertThat(outer.index()).isEqualTo(2);
        assertThat(outer.entry()).isSameAs(copy.stack.getLast()).isNotSameAs(spell);
        assertThat(gd.parkedOuterResolutions).hasSize(2);
    }

    @Test
    @DisplayName("Deep copy keeps a \"during that combat\" effect waiting for its combat phase independently")
    void deepCopyKeepsADuringThatCombatEffectIndependently() {
        FloatingContinuousEffect scheduled = gd.addFloatingEffect(FloatingContinuousEffect.onPlayer("Bumi, Unleashed",
                null, player1.getId(), new OnlyLandCreaturesCanAttackThisCombatEffect(), null,
                Duration.duringCombatPhase(2)));

        GameData copy = gd.simulationCopy();

        assertThat(copy.scheduledFloatingEffects).containsExactly(scheduled);
        assertThat(copy.floatingEffects).doesNotContain(scheduled);
        copy.replaceScheduledFloatingEffects(effect -> true, effect -> effect.withDuration(
                effect.duration().laterCombatPhase(1)));
        assertThat(copy.scheduledFloatingEffects).singleElement()
                .satisfies(effect -> assertThat(effect.duration().combatPhase()).isEqualTo(3));
        assertThat(gd.scheduledFloatingEffects).containsExactly(scheduled);
    }

    @Test
    @DisplayName("Deep copy preserves turn-scoped counters independently")
    void deepCopyPreservesTurnScopedCounters() {
        UUID p1 = player1.getId();
        gd.lifeLostThisTurn.put(p1, 6);
        FloatingCombatRules.skipNext(gd, p1, SkipKind.COMBAT_PHASE, 2);
        gd.lastClashWonByController.put(p1, true);
        gd.permanentTypesCastFromGraveyardThisTurn.put(p1, new HashSet<>(Set.of(CardType.CREATURE)));

        GameData copy = gd.simulationCopy();

        // These persist until cleanup, so a copy taken mid-turn must carry them.
        assertThat(copy.lifeLostThisTurn).containsEntry(p1, 6);
        assertThat(copy.skipNextCount(p1, SkipKind.COMBAT_PHASE)).isEqualTo(2);
        assertThat(copy.lastClashWonByController).containsEntry(p1, true);
        assertThat(copy.permanentTypesCastFromGraveyardThisTurn.get(p1)).containsExactly(CardType.CREATURE);

        // Independent — simulating a turn must not write back into the real game.
        copy.lifeLostThisTurn.put(p1, 99);
        copy.permanentTypesCastFromGraveyardThisTurn.get(p1).add(CardType.LAND);
        assertThat(gd.lifeLostThisTurn).containsEntry(p1, 6);
        assertThat(gd.permanentTypesCastFromGraveyardThisTurn.get(p1)).containsExactly(CardType.CREATURE);
    }

    @Test
    void deepCopyPreservesCreaturesThatEnlistedThisCombatIndependently() {
        UUID attackerId = UUID.randomUUID();
        gd.creaturesThatEnlistedThisCombat.add(attackerId);

        GameData copy = gd.simulationCopy();

        // Aradesh, the Founder's intervening "if" reads it again as its ability resolves in the simulated combat.
        assertThat(copy.creaturesThatEnlistedThisCombat).containsExactly(attackerId);
        copy.creaturesThatEnlistedThisCombat.add(UUID.randomUUID());
        assertThat(gd.creaturesThatEnlistedThisCombat).containsExactly(attackerId);
    }

    @Test
    @DisplayName("Deep copy preserves simultaneous-death granted-trigger snapshots independently")
    void deepCopyPreservesSimultaneousDeathGrantedTriggerSnapshots() {
        UUID permanentId = UUID.randomUUID();
        TriggeredAbility ability = new TriggeredAbility(Triggers.creatureDies(ObjectRelation.ANY),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
        gd.simultaneousDyingGrantedZoneChangeAbilities.put(permanentId, List.of(ability));

        GameData copy = gd.simulationCopy();

        assertThat(copy.simultaneousDyingGrantedZoneChangeAbilities.get(permanentId)).containsExactly(ability);
        copy.simultaneousDyingGrantedZoneChangeAbilities.clear();
        assertThat(gd.simultaneousDyingGrantedZoneChangeAbilities).containsKey(permanentId);
    }

    @Test
    @DisplayName("Deep copy preserves held simultaneous discard, exile and hand-put choices independently")
    void deepCopyPreservesHeldSimultaneousChoices() {
        UUID playerId = UUID.randomUUID();
        Card card = new GrizzlyBears();
        gd.deferredDiscardChooserId = playerId;
        gd.deferredDiscardSelections.add(new EachPlayerRummageState.SelectedDiscard(playerId, card.getId()));
        gd.stagedHandExiles.add(new StagedHandExile(playerId, card, null));
        gd.holdingHandBattlefieldPuts = true;
        gd.heldHandBattlefieldPuts.add(new BattlefieldEntryCard(playerId, playerId, card, Zone.HAND, null));

        GameData copy = gd.simulationCopy();

        assertThat(copy.deferredDiscardChooserId).isEqualTo(playerId);
        assertThat(copy.deferredDiscardSelections).containsExactlyElementsOf(gd.deferredDiscardSelections);
        assertThat(copy.stagedHandExiles).containsExactlyElementsOf(gd.stagedHandExiles);
        assertThat(copy.holdingHandBattlefieldPuts).isTrue();
        assertThat(copy.heldHandBattlefieldPuts).containsExactlyElementsOf(gd.heldHandBattlefieldPuts);
        copy.deferredDiscardSelections.clear();
        copy.stagedHandExiles.clear();
        copy.heldHandBattlefieldPuts.clear();
        assertThat(gd.deferredDiscardSelections).hasSize(1);
        assertThat(gd.stagedHandExiles).hasSize(1);
        assertThat(gd.heldHandBattlefieldPuts).hasSize(1);
    }

    @Test
    @DisplayName("Deep copy preserves spell-cast payment tracking independently")
    void deepCopyPreservesSpellCastPaymentTracking() {
        UUID cardId = UUID.randomUUID();
        gd.spellCastManaSpent.put(cardId, 5);
        gd.spellCastConvergeValue.put(cardId, 3);
        gd.spellCastColorsSpent.put(cardId, EnumSet.of(ManaColor.RED, ManaColor.BLUE));
        EnumMap<ManaColor, Integer> spentByColor = new EnumMap<>(ManaColor.class);
        spentByColor.put(ManaColor.RED, 2);
        gd.spellCastManaSpentByColor.put(cardId, spentByColor);
        EnumMap<ManaColor, Integer> spentOnX = new EnumMap<>(ManaColor.class);
        spentOnX.put(ManaColor.GREEN, 2);
        gd.spellCastManaSpentOnX.put(cardId, spentOnX);

        GameData copy = gd.simulationCopy();

        assertThat(copy.spellCastManaSpent).containsEntry(cardId, 5);
        assertThat(copy.spellCastConvergeValue).containsEntry(cardId, 3);
        assertThat(copy.spellCastColorsSpent.get(cardId)).containsExactlyInAnyOrder(ManaColor.RED, ManaColor.BLUE);
        assertThat(copy.spellCastManaSpentByColor.get(cardId)).containsEntry(ManaColor.RED, 2);
        assertThat(copy.spellCastManaSpentOnX.get(cardId)).containsEntry(ManaColor.GREEN, 2);

        // Independent — the nested EnumSet/EnumMap are copied, not aliased.
        copy.spellCastColorsSpent.get(cardId).add(ManaColor.WHITE);
        copy.spellCastManaSpentByColor.get(cardId).put(ManaColor.RED, 7);
        copy.spellCastManaSpentOnX.get(cardId).put(ManaColor.GREEN, 7);
        assertThat(gd.spellCastColorsSpent.get(cardId)).containsExactlyInAnyOrder(ManaColor.RED, ManaColor.BLUE);
        assertThat(gd.spellCastManaSpentByColor.get(cardId)).containsEntry(ManaColor.RED, 2);
        assertThat(gd.spellCastManaSpentOnX.get(cardId)).containsEntry(ManaColor.GREEN, 2);
    }

    @Test
    @DisplayName("Deep copy preserves until-end-of-turn casting permissions independently")
    void deepCopyPreservesUntilEndOfTurnPermissions() {
        UUID cardId = UUID.randomUUID();
        gd.addFloatingEffect(FloatingContinuousEffect.onCard("Past in Flames", null, player1.getId(),
                GrantedCardAbilityEffect.of(GrantedCardAbilityEffect.Ability.FLASHBACK), cardId, Zone.GRAVEYARD,
                Duration.UNTIL_END_OF_TURN));
        gd.addFloatingEffect(FloatingContinuousEffect.onPlayer("Bosium Strip", null, player1.getId(),
                new MayCastTopInstantOrSorceryFromGraveyardUntilEndOfTurnEffect(), player1.getId(),
                Duration.UNTIL_END_OF_TURN));

        GameData copy = gd.simulationCopy();

        assertThat(copy.hasGrantedCardAbility(cardId, GrantedCardAbilityEffect.Ability.FLASHBACK)).isTrue();
        assertThat(copy.hasFloatingEffectOnPlayer(player1.getId(),
                MayCastTopInstantOrSorceryFromGraveyardUntilEndOfTurnEffect.class)).isTrue();

        copy.endFloatingEffectsOnCard(cardId);
        copy.floatingEffectsOnPlayer(player1.getId(), MayCastTopInstantOrSorceryFromGraveyardUntilEndOfTurnEffect.class)
                .forEach(copy::endFloatingEffect);
        assertThat(gd.hasGrantedCardAbility(cardId, GrantedCardAbilityEffect.Ability.FLASHBACK)).isTrue();
        assertThat(gd.hasFloatingEffectOnPlayer(player1.getId(),
                MayCastTopInstantOrSorceryFromGraveyardUntilEndOfTurnEffect.class)).isTrue();
    }

    @Test
    @DisplayName("Deep copy preserves next-spell flash grants independently")
    void deepCopyPreservesNextSpellFlashGrants() {
        gd.addFloatingEffect(FloatingContinuousEffect.onPlayer("Quicken", null, player1.getId(),
                new GrantFlashToNextMatchingSpellEffect(new CardTypePredicate(CardType.SORCERY)), player1.getId(),
                Duration.UNTIL_MATCHING_SPELL_CAST));

        GameData copy = gd.simulationCopy();

        assertThat(copy.floatingEffectsOnPlayer(player1.getId(), GrantFlashToNextMatchingSpellEffect.class))
                .hasSize(1);

        copy.floatingEffectsOnPlayer(player1.getId(), GrantFlashToNextMatchingSpellEffect.class)
                .forEach(copy::endFloatingEffect);
        assertThat(gd.floatingEffectsOnPlayer(player1.getId(), GrantFlashToNextMatchingSpellEffect.class))
                .hasSize(1);
    }

    @Test
    @DisplayName("Deep copy preserves next-creature-spell empowerments independently")
    void deepCopyPreservesNextCreatureSpellEmpowerments() {
        EmpowerNextCreatureSpellThisTurnEffect empowerment = new EmpowerNextCreatureSpellThisTurnEffect(true, 1);
        gd.addFloatingEffect(FloatingContinuousEffect.onPlayer("Savage Summoning", null, player1.getId(),
                empowerment, player1.getId(), Duration.UNTIL_MATCHING_SPELL_CAST));
        UUID spellId = UUID.randomUUID();
        gd.spellAdditionalEnterCounters.put(spellId, 2);

        GameData copy = gd.simulationCopy();

        assertThat(copy.floatingPlayerEffects(player1.getId(), EmpowerNextCreatureSpellThisTurnEffect.class))
                .containsExactly(empowerment);
        assertThat(copy.spellAdditionalEnterCounters).containsEntry(spellId, 2);

        copy.floatingEffectsOnPlayer(player1.getId(), EmpowerNextCreatureSpellThisTurnEffect.class)
                .forEach(copy::endFloatingEffect);
        copy.spellAdditionalEnterCounters.clear();
        assertThat(gd.floatingPlayerEffects(player1.getId(), EmpowerNextCreatureSpellThisTurnEffect.class))
                .containsExactly(empowerment);
        assertThat(gd.spellAdditionalEnterCounters).containsEntry(spellId, 2);
    }

    @Test
    @DisplayName("Deep copy preserves a pending departure batch independently")
    void deepCopyPreservesPendingDepartureBatch() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SerraAngel());
        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent angel = gd.playerBattlefields.get(player2.getId()).getFirst();
        Permanent bearsSnapshot = new Permanent(bears);
        bearsSnapshot.setLastKnownPower(2);
        bearsSnapshot.setLastKnownToughness(2);
        UUID ownerStackEntryId = UUID.randomUUID();
        ChoiceContext.DeathExileReplacement toHand = new ChoiceContext.DeathExileReplacement(
                "Return it to its owner's hand", ChoiceContext.DeathExileReplacement.Kind.RETURN_TO_HAND,
                null, null, null, null);
        DepartureBatch batch = DepartureBatch.of(List.of(
                        new DepartureIntent(bears.getId(), DepartureCause.STATE_BASED_DESTROY),
                        new DepartureIntent(angel.getId(), DepartureCause.SACRIFICE, player1.getId())))
                .withDestructionAnswer(bears.getId(), "INTRINSIC")
                .withStage(DepartureStage.CHOOSE)
                .withSnapshot(new DepartureSnapshot(bearsSnapshot, player1.getId(), true,
                        Map.of(Keyword.PERSIST, 0, Keyword.UNDYING, 0), List.of()))
                .withReplacementAnswer(bears.getId(), toHand)
                .awaitingAnswer(angel.getId())
                .ownedBy(ownerStackEntryId);
        gd.pendingDepartureBatch = batch;

        GameData copy = gd.simulationCopy();

        DepartureBatch copied = copy.pendingDepartureBatch;
        assertThat(copied).isNotNull().isNotSameAs(batch);
        assertThat(copied)
                .usingRecursiveComparison()
                .withEqualsForType((a, b) -> a.getId().equals(b.getId())
                        && Objects.equals(a.getLastKnownPower(), b.getLastKnownPower())
                        && Objects.equals(a.getLastKnownToughness(), b.getLastKnownToughness()), Permanent.class)
                .isEqualTo(batch);
        assertThat(copied.isAwaitingAnswer()).isTrue();
        assertThat(copied.ownerStackEntryId()).isEqualTo(ownerStackEntryId);
        Permanent copiedSnapshot = copied.snapshots().get(bears.getId()).permanent();
        assertThat(copiedSnapshot).isNotSameAs(bearsSnapshot);

        copiedSnapshot.setLastKnownPower(7);
        copy.pendingDepartureBatch = copied.withReplacementAnswer(angel.getId(), toHand)
                .withStage(DepartureStage.PERFORM);

        assertThat(gd.pendingDepartureBatch).isSameAs(batch);
        assertThat(batch.stage()).isEqualTo(DepartureStage.CHOOSE);
        assertThat(batch.awaitingAnswerIds()).containsExactly(angel.getId());
        assertThat(batch.replacementAnswers()).containsOnlyKeys(bears.getId());
        assertThat(bearsSnapshot.getLastKnownPower()).isEqualTo(2);
    }

    @Test
    void deepCopyPreservesTheResolutionThatScheduledADelayedAction() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.resolutionOrdinal = 7;
        DelayedPermanentActions.scheduleOwn(gd, player1.getId(), bears, new DelayedPermanentAction(
                bears.getId(), DelayedPermanentActionKind.EXILE_TOKEN_AT_END_STEP), ActivePlayer.ANY);

        GameData copy = gd.simulationCopy();

        assertThat(copy.resolutionOrdinal).isEqualTo(7);
        assertThat(DelayedPermanentMoves.waiting(copy)).hasSize(1)
                .allSatisfy(action -> assertThat(action.schedulingResolution()).isEqualTo(7));

        copy.resolutionOrdinal++;
        DelayedPermanentActions.scheduleOwn(copy, player1.getId(), bears, new DelayedPermanentAction(
                bears.getId(), DelayedPermanentActionKind.EXILE_TOKEN_AT_END_STEP), ActivePlayer.ANY);

        assertThat(gd.resolutionOrdinal).isEqualTo(7);
        assertThat(DelayedPermanentMoves.waiting(gd)).hasSize(1);
        assertThat(DelayedPermanentMoves.waiting(copy)).last()
                .satisfies(action -> assertThat(action.schedulingResolution()).isEqualTo(8));
    }

    @Test
    @DisplayName("Deep copy keeps the parked record of a target-player-then-graveyard-cards choice independently")
    void deepCopyKeepsTheRecordOfATargetPlayerGraveyardChoiceIndependently() {
        StackEntry record = TestStackDoor.triggered(new SerraAngel(), player1.getId(), "Enters ability", List.of());
        gd.interaction.setPermanentChoiceContext(new PermanentChoiceContext.TargetPlayerGraveyardCardsTrigger(
                record.getCard(), player1.getId(), record,
                new com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromGraveyardIntoLibraryEffect(
                        null, Integer.MAX_VALUE)));

        GameData copy = gd.simulationCopy();
        var copied = (PermanentChoiceContext.TargetPlayerGraveyardCardsTrigger) copy.interaction.permanentChoiceContext();
        copied.record().setXValue(3);

        assertThat(copied.record()).isNotSameAs(record);
        assertThat(record.getXValue()).isZero();
    }

    @Test
    @DisplayName("Deep copy keeps the stays of Auras put into graveyards for not being attached")
    void deepCopyKeepsUnattachedAuraStaysIndependently() {
        java.util.UUID aura = java.util.UUID.randomUUID();
        gd.unattachedAuraGraveyardStays.put(aura, 4L);

        GameData copy = gd.simulationCopy();
        copy.unattachedAuraGraveyardStays.put(java.util.UUID.randomUUID(), 5L);

        assertThat(copy.unattachedAuraGraveyardStays).containsEntry(aura, 4L);
        assertThat(gd.unattachedAuraGraveyardStays).containsOnlyKeys(aura);
    }
}
