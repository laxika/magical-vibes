package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(SpinedBasher.class)
class SpinedBasherTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUp() {
        harness.setHand(player1, List.of(new SpinedBasher()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent basher = findPermanent(player1, "Spined Basher");
        assertThat(basher.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int basherIndex = gd.playerBattlefields.get(player1.getId()).indexOf(basher);
        harness.turnFaceUp(player1, basherIndex);
        harness.passBothPriorities();

        assertThat(basher.isFaceDown()).isFalse();
    }

    @Test
    void turningFaceUpRequiresBlackMana() {
        harness.setHand(player1, List.of(new SpinedBasher()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent basher = findPermanent(player1, "Spined Basher");
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int basherIndex = gd.playerBattlefields.get(player1.getId()).indexOf(basher);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, basherIndex))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(basher.isFaceDown()).isTrue();
    }

    @Test
    void turningFaceUpRequiresFullGenericCostAndDoesNotUseTheStack() {
        harness.setHand(player1, List.of(new SpinedBasher()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent basher = findPermanent(player1, "Spined Basher");
        int basherIndex = gd.playerBattlefields.get(player1.getId()).indexOf(basher);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, basherIndex))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(basher.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, basherIndex);

        assertThat(basher.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(basher);
    }

    @Test
    void castingFaceDownRequiresThreeMana() {
        harness.setHand(player1, List.of(new SpinedBasher()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithMorph(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        harness.assertInHand(player1, "Spined Basher");
        harness.assertNotOnBattlefield(player1, "Spined Basher");
        assertThat(gd.stack).isEmpty();
    }
}
