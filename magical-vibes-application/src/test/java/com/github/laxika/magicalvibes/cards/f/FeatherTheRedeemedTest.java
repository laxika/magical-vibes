package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FeatherTheRedeemed.class, GiantGrowth.class, GrizzlyBears.class})
class FeatherTheRedeemedTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a targeted instant and returns it at the next end step")
    void exilesAndReturnsTargetedSpell() {
        harness.addToBattlefield(player1, new FeatherTheRedeemed());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GiantGrowth growth = new GiantGrowth();
        harness.setHand(player1, java.util.List.of(growth));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Giant Growth");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(growth);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(growth);
        assertThat(gd.playerHands.get(player1.getId())).contains(growth);
    }

    @Test
    @DisplayName("Does not trigger when the spell targets an opponent's creature")
    void doesNotTriggerForOpponentsCreature() {
        harness.addToBattlefield(player1, new FeatherTheRedeemed());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, opponentBears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Giant Growth");
        assertThat(gd.getPlayerExiledCards(player1.getId())).noneMatch(card -> card.getName().equals("Giant Growth"));
    }

    @Test
    @DisplayName("Does not exile a spell that fizzles")
    void doesNotExileFizzledSpell() {
        harness.addToBattlefield(player1, new FeatherTheRedeemed());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Giant Growth");
        assertThat(gd.getPlayerExiledCards(player1.getId())).noneMatch(card -> card.getName().equals("Giant Growth"));
    }
}
