package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WoollyLoxodon.class})
class WoollyLoxodonTest extends BaseCardTest {

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new WoollyLoxodon()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent loxodon = findPermanent(player1, "Woolly Loxodon");
        assertThat(loxodon.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        int loxodonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(loxodon);
        harness.turnFaceUp(player1, loxodonIndex);
        harness.passBothPriorities();

        assertThat(loxodon.isFaceDown()).isFalse();
    }

    @Test
    void cannotTurnFaceUpWithoutGreenMana() {
        harness.setHand(player1, List.of(new WoollyLoxodon()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent loxodon = findPermanent(player1, "Woolly Loxodon");
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(loxodon.isFaceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTurnFaceUpWithOnlyFiveMana() {
        harness.setHand(player1, List.of(new WoollyLoxodon()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent loxodon = findPermanent(player1, "Woolly Loxodon");
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(loxodon.isFaceDown()).isTrue();
    }

    @Test
    void turningFaceUpOnOpponentsTurnIsImmediateAndPreservesPermanentState() {
        harness.setHand(player1, List.of(new WoollyLoxodon()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent loxodon = findPermanent(player1, "Woolly Loxodon");
        assertThat(gqs.getEffectivePower(gd, loxodon)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, loxodon)).isEqualTo(2);
        loxodon.setTapped(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.turnFaceUp(player1, 0);

        assertThat(loxodon.isFaceDown()).isFalse();
        assertThat(loxodon.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(loxodon);
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, loxodon)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, loxodon)).isEqualTo(7);
    }
}
