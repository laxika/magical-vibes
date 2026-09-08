package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LumithreadField.class, GrizzlyBears.class})
class LumithreadFieldTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control get +0/+1")
    void buffsCreaturesYouControl() {
        harness.addToBattlefield(player1, new LumithreadField());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff creatures controlled by an opponent")
    void doesNotBuffOpponentsCreatures() {
        harness.addToBattlefield(player1, new LumithreadField());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can be cast face down and turned face up for its morph cost")
    void morphsFaceDownAndTurnsFaceUp() {
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
    }
}
