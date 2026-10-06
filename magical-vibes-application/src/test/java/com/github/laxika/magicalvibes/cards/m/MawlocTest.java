package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Toxicrene;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Mawloc.class, GrizzlyBears.class, Toxicrene.class})
class MawlocTest extends BaseCardTest {

    @Test
    @DisplayName("Ravenous enters with X counters and draws at X=5")
    void ravenousAtFive() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        castMawloc(5, List.of());

        Permanent mawloc = findPermanent(player1, "Mawloc");
        assertThat(mawloc.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Ravenous does not draw below X=5")
    void ravenousBelowFiveDoesNotDraw() {
        castMawloc(4, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(findPermanent(player1, "Mawloc")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Terror from the Deep fights an opposing creature and exiles it if it dies")
    void fightsAndExilesOpposingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castMawloc(1, List.of(target.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("Terror from the Deep may choose no target")
    void fightTargetIsOptional() {
        castMawloc(1, List.of());

        harness.assertOnBattlefield(player1, "Mawloc");
    }

    @Test
    @DisplayName("Terror from the Deep cannot target a creature its controller controls")
    void fightTargetMustBeControlledByOpponent() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> castMawloc(1, List.of(ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Terror from the Deep exiles a surviving creature that dies later this turn")
    void exilesSurvivingCreatureThatDiesLater() {
        GrizzlyBears targetCard = new GrizzlyBears();
        targetCard.setPower(0);
        targetCard.setToughness(5);
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);

        castMawloc(1, List.of(target.getId()));

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        target.setMarkedDamage(5);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("Ravenous at X=0 enters without counters and does not draw")
    void ravenousAtZero() {
        harness.setLibrary(player1, List.of(new Mawloc()));

        castMawloc(0, List.of());

        assertThat(findPermanent(player1, "Mawloc")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The chosen creature is exiled if it dies even when fight damage was prevented")
    void exilesTargetWhenFightDamageWasPrevented() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setDamagePreventionShield(3);

        castMawloc(1, List.of(target.getId()));

        assertThat(target.getMarkedDamage()).isZero();
        target.setMarkedDamage(2);
        harness.runStateBasedActions();

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("The chosen creature is still exiled after Mawloc dies in the fight")
    void exileReplacementSurvivesMawlocLeaving() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Toxicrene());

        castMawloc(1, List.of(target.getId()));

        harness.assertInGraveyard(player1, "Mawloc");
        harness.assertOnBattlefield(player2, "Toxicrene");
        assertThat(target.getMarkedDamage()).isEqualTo(3);
        target.setMarkedDamage(4);
        harness.runStateBasedActions();

        harness.assertNotInGraveyard(player2, "Toxicrene");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("Combat damage does not exile a creature that was never the fight target")
    void combatDamageDoesNotGrantExileReplacement() {
        addCreatureReady(player1, new Mawloc());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Ravenous still draws when the fight target leaves before the triggers resolve")
    void ravenousDrawIsIndependentOfFightTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Mawloc()));
        harness.setHand(player1, List.of(new Mawloc()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        gs.playCard(gd, player1, 0, 5, null, null, List.of(target.getId()), List.of());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Mawloc");
        target.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        resolveAllTriggers();

        harness.assertInHand(player1, "Mawloc");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void castMawloc(int x, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new Mawloc()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, x);

        gs.playCard(gd, player1, 0, x, null, null, targetIds, List.of());
        resolveAllTriggers();
    }
}
