package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LumithreadField.class, BlindPhantasm.class})
class LumithreadFieldTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control get +0/+1")
    void buffsCreaturesYouControl() {
        harness.addToBattlefield(player1, new LumithreadField());
        Permanent phantasm = harness.addToBattlefieldAndReturn(player1, new BlindPhantasm());

        assertThat(gqs.getEffectivePower(gd, phantasm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, phantasm)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not buff creatures controlled by an opponent")
    void doesNotBuffOpponentsCreatures() {
        harness.addToBattlefield(player1, new LumithreadField());
        Permanent phantasm = harness.addToBattlefieldAndReturn(player2, new BlindPhantasm());

        assertThat(gqs.getEffectivePower(gd, phantasm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, phantasm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Can be cast face down and turned face up for its morph cost")
    void morphsFaceDownAndTurnsFaceUp() {
        Permanent phantasm = harness.addToBattlefieldAndReturn(player1, new BlindPhantasm());
        harness.setHand(player1, List.of(new LumithreadField()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent field = findPermanent(player1, "Lumithread Field");
        assertThat(field.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int fieldIndex = gd.playerBattlefields.get(player1.getId()).indexOf(field);
        harness.turnFaceUp(player1, fieldIndex);
        harness.passBothPriorities();

        assertThat(field.isFaceDown()).isFalse();
        assertThat(gqs.getEffectivePower(gd, phantasm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, phantasm)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Face-down Lumithread Field does not grant its static ability")
    void faceDownFieldDoesNotGrantStaticBonus() {
        Permanent phantasm = harness.addToBattlefieldAndReturn(player1, new BlindPhantasm());
        harness.setHand(player1, List.of(new LumithreadField()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Lumithread Field").isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, phantasm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, phantasm)).isEqualTo(3);
    }
}
