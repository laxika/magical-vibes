package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.UkkimaStalkingShadow;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CazurRuthlessStalker.class, GrizzlyBears.class, UkkimaStalkingShadow.class})
class CazurRuthlessStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Partner with lets the target player search for Ukkima")
    void partnerWithSearchesForUkkima() {
        UkkimaStalkingShadow ukkima = new UkkimaStalkingShadow();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(ukkima));

        harness.enterBattlefieldAndReturn(player1, new CazurRuthlessStalker());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.playerId()).isEqualTo(player1.getId());
        assertThat(targetChoice.validPlayerIds()).contains(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(ukkima);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each creature you control gets a counter when it deals combat damage")
    void combatDamagePutsCountersOnDealingCreatures() {
        Permanent cazur = addCreatureReady(player1, new CazurRuthlessStalker());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        cazur.setAttacking(true);
        bears.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(cazur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void targetPlayerMayDeclinePartnerSearch() {
        UkkimaStalkingShadow ukkima = new UkkimaStalkingShadow();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(ukkima));
        harness.enterBattlefieldAndReturn(player1, new CazurRuthlessStalker());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(ukkima);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void partnerSearchCanTargetControllerAndFindNoPartner() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(bears));
        harness.enterBattlefieldAndReturn(player1, new CazurRuthlessStalker());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPlayerIds()).contains(player1.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("library is shuffled")).isTrue();
    }

    @Test
    void opponentCombatDamageDoesNotGiveCounters() {
        Permanent cazur = addCreatureReady(player1, new CazurRuthlessStalker());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        assertThat(cazur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void countersArePlacedOnlyAfterCombatDamageTriggerResolves() {
        Permanent cazur = addCreatureReady(player1, new CazurRuthlessStalker());
        cazur.setAttacking(true);

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> resolveCombat());

        harness.assertLife(player2, 17);
        assertThat(cazur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        resolveAllTriggers();
        assertThat(cazur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void leavingBeforeResolutionDoesNotMoveCounterToAnotherCreature() {
        Permanent cazur = addCreatureReady(player1, new CazurRuthlessStalker());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        cazur.setAttacking(true);
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> resolveCombat());
        assertThat(cazur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, cazur));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Cazur, Ruthless Stalker");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void counterTriggerResolvesAfterCazurLeaves() {
        Permanent cazur = addCreatureReady(player1, new CazurRuthlessStalker());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> resolveCombat());
        harness.assertLife(player2, 18);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, cazur));
        resolveAllTriggers();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void noncombatDamageDoesNotGiveCounters() {
        Permanent cazur = addCreatureReady(player1, new CazurRuthlessStalker());
        Permanent ukkima = addCreatureReady(player1, new UkkimaStalkingShadow());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, ukkima));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(cazur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
