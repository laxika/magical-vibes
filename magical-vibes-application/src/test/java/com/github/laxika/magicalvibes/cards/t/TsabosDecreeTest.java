package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TsabosDecreeTest extends BaseCardTest {

    @Test
    @DisplayName("Discards matching creature cards and destroys matching creatures target player controls")
    void discardsAndDestroysChosenType() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent targetBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        targetBear.setRegenerationShield(1);
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new HillGiant(), new Forest(), new GrizzlyBears(), new AvianChangeling())));

        castDecree(player2.getId());
        harness.handleListChoice(player1, "BEAR");

        assertThat(ownBear).isIn(gd.playerBattlefields.get(player1.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Hill Giant");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Hill Giant", "Forest");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .contains("Grizzly Bears", "Avian Changeling");
    }

    @Test
    @DisplayName("Does nothing to another type when the chosen type is absent")
    void absentChosenTypeDoesNothing() {
        Permanent targetBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, new ArrayList<>(List.of(new HillGiant(), new Forest())));

        castDecree(player2.getId());
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(targetBear).isIn(gd.playerBattlefields.get(player2.getId()));
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Hill Giant", "Forest");
    }

    @Test
    @DisplayName("Rejects a non-player target")
    void rejectsNonPlayerTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.setHand(player1, List.of(new TsabosDecree()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castDecree(java.util.UUID targetPlayerId) {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.setHand(player1, List.of(new TsabosDecree()));
        harness.castInstant(player1, 0, targetPlayerId);
        harness.passBothPriorities();
    }
}
