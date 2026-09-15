package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ToweringBaloth.class)
class ToweringBalothTest extends BaseCardTest {

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        Permanent baloth = castFaceDown();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        int balothIndex = gd.playerBattlefields.get(player1.getId()).indexOf(baloth);
        harness.turnFaceUp(player1, balothIndex);
        harness.passBothPriorities();

        assertThat(baloth.isFaceDown()).isFalse();
    }

    @Test
    void faceDownCastRequiresThreeMana() {
        harness.setHand(player1, List.of(new ToweringBaloth()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithMorph(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        harness.assertInHand(player1, "Towering Baloth");
    }

    @Test
    void turningFaceUpRequiresSixGenericMana() {
        Permanent baloth = castFaceDown();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(baloth)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(baloth.isFaceDown()).isTrue();
    }

    @Test
    void turningFaceUpRequiresGreenMana() {
        Permanent baloth = castFaceDown();
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(baloth)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(baloth.isFaceDown()).isTrue();
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new ToweringBaloth()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent baloth = findPermanent(player1, "Towering Baloth");
        assertThat(baloth.isFaceDown()).isTrue();
        return baloth;
    }
}
