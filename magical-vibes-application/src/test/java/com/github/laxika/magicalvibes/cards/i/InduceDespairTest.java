package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InduceDespairTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a creature -X/-X based on the revealed creature card's mana value")
    void usesRevealedCreatureManaValue() {
        Permanent target = addCreatureReady(player2, new HillGiant());
        InduceDespair spell = new InduceDespair();
        GrizzlyBears revealed = new GrizzlyBears();
        harness.setHand(player1, List.of(spell, revealed));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealed);
    }

    @Test
    @DisplayName("The -X/-X effect wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new InduceDespair(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("A creature with mana value three dies to a revealed three-mana creature")
    void usesThreeManaValue() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new InduceDespair(), new HillGiant()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot be cast without a creature card to reveal")
    void requiresCreatureCardToReveal() {
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new InduceDespair(), new Swamp()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, target.getId(), 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
    }

    @Test
    @DisplayName("Rejects a non-creature target")
    void rejectsNonCreatureTarget() {
        harness.setHand(player1, List.of(new InduceDespair(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, player2.getId(), 1))
                .isInstanceOf(IllegalStateException.class);
    }
}
