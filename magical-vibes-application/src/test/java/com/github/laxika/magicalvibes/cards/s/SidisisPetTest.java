package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SidisisPetTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUpForBlack() {
        harness.setHand(player1, List.of(new SidisisPet()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent pet = findPermanent(player1, "Sidisi's Pet");
        assertThat(pet.isFaceDown()).isTrue();
        assertThat(pet.getEffectivePower()).isEqualTo(2);
        assertThat(pet.getEffectiveToughness()).isEqualTo(2);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(pet));
        harness.passBothPriorities();

        assertThat(pet.isFaceDown()).isFalse();
        assertThat(pet.getEffectivePower()).isEqualTo(1);
        assertThat(pet.getEffectiveToughness()).isEqualTo(4);
    }
}
