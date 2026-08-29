package com.github.laxika.magicalvibes.ai;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.action.DelayedPlusOneCounters;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.CreatureSpellEmpowerment;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
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
    @DisplayName("Deep copy preserves the aliasing between the may-targeting entry and the suspended resolution entry")
    void deepCopyPreservesMayTargetingAliasing() {
        // CR 603.5 resolution-time targeting: the chosen target is set through
        // resolvedMayTargetingEntry and resolution resumes through
        // pendingEffectResolutionEntry — both must reference the same object.
        StackEntry suspended = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY, new GrizzlyBears(), player1.getId(), "Test trigger", List.of());
        gd.pendingEffectResolutionEntry = suspended;
        gd.resolvedMayTargetingEntry = suspended;

        GameData copy = gd.simulationCopy();

        assertThat(copy.pendingEffectResolutionEntry).isNotSameAs(suspended);
        assertThat(copy.resolvedMayTargetingEntry).isSameAs(copy.pendingEffectResolutionEntry);
    }

    @Test
    @DisplayName("Deep copy keeps distinct may-targeting and suspended resolution entries independent")
    void deepCopyKeepsDistinctResolutionEntriesIndependent() {
        gd.pendingEffectResolutionEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY, new GrizzlyBears(), player1.getId(), "Suspended", List.of());
        gd.resolvedMayTargetingEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY, new SerraAngel(), player1.getId(), "Targeting", List.of());

        GameData copy = gd.simulationCopy();

        assertThat(copy.pendingEffectResolutionEntry).isNotSameAs(gd.pendingEffectResolutionEntry);
        assertThat(copy.resolvedMayTargetingEntry).isNotSameAs(gd.resolvedMayTargetingEntry);
        assertThat(copy.resolvedMayTargetingEntry).isNotSameAs(copy.pendingEffectResolutionEntry);
    }

    @Test
    @DisplayName("Deep copy preserves the unified delayed-action queue independently and in order")
    void deepCopyPreservesDelayedActions() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        gd.queueDelayedAction(new DelayedPermanentAction(a, DelayedPermanentActionKind.EXILE_TOKEN_AT_END_STEP));
        gd.queueDelayedAction(new DelayedPermanentAction(b, DelayedPermanentActionKind.DESTROY_AT_END_STEP));
        gd.addDelayedPlusOneCounters(a, 4);

        GameData copy = gd.simulationCopy();

        // Same values, same insertion order (records are immutable, shallow copy).
        assertThat(copy.delayedActions).containsExactly(
                new DelayedPermanentAction(a, DelayedPermanentActionKind.EXILE_TOKEN_AT_END_STEP),
                new DelayedPermanentAction(b, DelayedPermanentActionKind.DESTROY_AT_END_STEP),
                new DelayedPlusOneCounters(a, 4));

        // Independent list — mutating the copy leaves the original untouched.
        copy.delayedActions.clear();
        assertThat(gd.delayedActions).hasSize(3);
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .containsExactly(
                        new DelayedPermanentAction(a, DelayedPermanentActionKind.EXILE_TOKEN_AT_END_STEP),
                        new DelayedPermanentAction(b, DelayedPermanentActionKind.DESTROY_AT_END_STEP));
    }

    @Test
    @DisplayName("Deep copy preserves turn-scoped counters independently")
    void deepCopyPreservesTurnScopedCounters() {
        UUID p1 = player1.getId();
        gd.lifeLostThisTurn.put(p1, 6);
        gd.skipNextCombatPhaseCount.put(p1, 2);
        gd.lastClashWonByController.put(p1, true);
        gd.permanentTypesCastFromGraveyardThisTurn.put(p1, new HashSet<>(Set.of(CardType.CREATURE)));

        GameData copy = gd.simulationCopy();

        // These persist until cleanup, so a copy taken mid-turn must carry them.
        assertThat(copy.lifeLostThisTurn).containsEntry(p1, 6);
        assertThat(copy.skipNextCombatPhaseCount).containsEntry(p1, 2);
        assertThat(copy.lastClashWonByController).containsEntry(p1, true);
        assertThat(copy.permanentTypesCastFromGraveyardThisTurn.get(p1)).containsExactly(CardType.CREATURE);

        // Independent — simulating a turn must not write back into the real game.
        copy.lifeLostThisTurn.put(p1, 99);
        copy.permanentTypesCastFromGraveyardThisTurn.get(p1).add(CardType.LAND);
        assertThat(gd.lifeLostThisTurn).containsEntry(p1, 6);
        assertThat(gd.permanentTypesCastFromGraveyardThisTurn.get(p1)).containsExactly(CardType.CREATURE);
    }

    @Test
    @DisplayName("Deep copy preserves simultaneous-death granted-trigger snapshots independently")
    void deepCopyPreservesSimultaneousDeathGrantedTriggerSnapshots() {
        UUID permanentId = UUID.randomUUID();
        PutCountersOnSelfEffect effect = new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE);
        gd.simultaneousDyingGrantedCreatureDeathEffects.put(permanentId, List.of(effect));

        GameData copy = gd.simulationCopy();

        assertThat(copy.simultaneousDyingGrantedCreatureDeathEffects.get(permanentId))
                .containsExactly(effect);
        copy.simultaneousDyingGrantedCreatureDeathEffects.clear();
        assertThat(gd.simultaneousDyingGrantedCreatureDeathEffects).containsKey(permanentId);
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
        gd.cardsGrantedFlashbackUntilEndOfTurn.add(cardId);
        gd.mayTapLandsForSpellsUntilEndOfTurn.add(player1.getId());
        gd.playersWithFlashUntilEndOfTurn.add(player1.getId());
        gd.mayCastTopInstantOrSorceryFromGraveyardUntilEndOfTurn.add(player1.getId());

        GameData copy = gd.simulationCopy();

        assertThat(copy.cardsGrantedFlashbackUntilEndOfTurn).contains(cardId);
        assertThat(copy.mayTapLandsForSpellsUntilEndOfTurn).contains(player1.getId());
        assertThat(copy.playersWithFlashUntilEndOfTurn).contains(player1.getId());
        assertThat(copy.mayCastTopInstantOrSorceryFromGraveyardUntilEndOfTurn).contains(player1.getId());

        copy.cardsGrantedFlashbackUntilEndOfTurn.clear();
        copy.playersWithFlashUntilEndOfTurn.clear();
        copy.mayCastTopInstantOrSorceryFromGraveyardUntilEndOfTurn.clear();
        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).contains(cardId);
        assertThat(gd.playersWithFlashUntilEndOfTurn).contains(player1.getId());
        assertThat(gd.mayCastTopInstantOrSorceryFromGraveyardUntilEndOfTurn).contains(player1.getId());
    }

    @Test
    @DisplayName("Deep copy preserves next-spell flash grants independently")
    void deepCopyPreservesNextSpellFlashGrants() {
        gd.addNextSpellFlashGrant(player1.getId(), CardType.SORCERY);

        GameData copy = gd.simulationCopy();

        assertThat(copy.nextSpellFlashGrantsThisTurn.get(player1.getId())).containsExactly(CardType.SORCERY);

        copy.nextSpellFlashGrantsThisTurn.get(player1.getId()).clear();
        assertThat(gd.nextSpellFlashGrantsThisTurn.get(player1.getId())).containsExactly(CardType.SORCERY);
    }

    @Test
    @DisplayName("Deep copy preserves next-creature-spell empowerments independently")
    void deepCopyPreservesNextCreatureSpellEmpowerments() {
        CreatureSpellEmpowerment empowerment = new CreatureSpellEmpowerment(true, 1);
        gd.addNextCreatureSpellEmpowerment(player1.getId(), empowerment);
        UUID spellId = UUID.randomUUID();
        gd.spellAdditionalEnterCounters.put(spellId, 2);

        GameData copy = gd.simulationCopy();

        assertThat(copy.nextCreatureSpellEmpowermentsThisTurn.get(player1.getId())).containsExactly(empowerment);
        assertThat(copy.spellAdditionalEnterCounters).containsEntry(spellId, 2);

        copy.nextCreatureSpellEmpowermentsThisTurn.get(player1.getId()).clear();
        copy.spellAdditionalEnterCounters.clear();
        assertThat(gd.nextCreatureSpellEmpowermentsThisTurn.get(player1.getId())).containsExactly(empowerment);
        assertThat(gd.spellAdditionalEnterCounters).containsEntry(spellId, 2);
    }
}
