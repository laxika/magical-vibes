package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({Mawloc.class, GrizzlyBears.class})
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

    private void castMawloc(int x, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new Mawloc()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, x);

        gs.playCard(gd, player1, 0, x, null, null, targetIds, List.of());
        resolveAllTriggers();
    }
}
