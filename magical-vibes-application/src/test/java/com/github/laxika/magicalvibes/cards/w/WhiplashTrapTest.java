package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WhiplashTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Returns two target creatures to their owners' hands")
    void returnsTwoTargetCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WhiplashTrap()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can be cast for {U} after two opponent creatures entered this turn")
    void castsForAlternateCostAfterTwoOpponentCreaturesEntered() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.permanentsEnteredBattlefieldThisTurn.put(player2.getId(),
                List.of(firstCreature.getCard(), secondCreature.getCard()));
        harness.setHand(player1, List.of(new WhiplashTrap()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(firstCreature.getId(), secondCreature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Whiplash Trap");
    }

    @Test
    @DisplayName("Alternate cost requires two creatures to have entered under an opponent's control")
    void alternateCostRequiresTwoOpponentCreaturesToHaveEntered() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.permanentsEnteredBattlefieldThisTurn.put(player2.getId(), List.of(firstCreature.getCard()));
        gd.permanentsEnteredBattlefieldThisTurn.put(player1.getId(), List.of(secondCreature.getCard()));
        harness.setHand(player1, List.of(new WhiplashTrap()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
