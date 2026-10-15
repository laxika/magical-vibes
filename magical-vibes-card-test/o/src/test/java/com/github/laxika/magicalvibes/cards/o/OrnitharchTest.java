package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BileBlight;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Ornitharch.class, BileBlight.class})
class OrnitharchTest extends BaseCardTest {

    @Test
    @DisplayName("The opponent pays tribute and Ornitharch enters with two +1/+1 counters")
    void opponentPaysTribute() {
        castOrnitharch();

        assertThatThrownBy(() -> harness.handleMayAbilityChosen(player1, true))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMayAbilityChosen(player2, true);

        Permanent ornitharch = findPermanent(player1, "Ornitharch");
        assertThat(ornitharch.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanents(player1, "Bird")).isEmpty();
    }

    @Test
    @DisplayName("Declining tribute creates two 1/1 white Bird tokens with flying")
    void opponentDeclinesTribute() {
        castOrnitharch();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Bird")).hasSize(2);
        assertThat(findPermanent(player1, "Ornitharch")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bird creation still resolves after Ornitharch dies in response")
    void createsBirdsAfterSourceDies() {
        harness.setHand(player2, java.util.List.of(new BileBlight()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        castOrnitharch();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(findPermanents(player1, "Bird")).isEmpty();
        harness.castInstant(player2, 0, findPermanent(player1, "Ornitharch").getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Ornitharch")).isEmpty();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bird")).hasSize(2).allSatisfy(bird -> {
            assertThat(bird.getCard().isToken()).isTrue();
            assertThat(bird.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(bird.getCard().getSubtypes()).containsExactly(CardSubtype.BIRD);
            assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, bird, Keyword.FLYING)).isTrue();
        });
        assertThat(findPermanents(player2, "Bird")).isEmpty();
    }

    @Test
    @DisplayName("The other controller's opponent chooses tribute and Birds belong to that controller")
    void opponentControlledOrnitharchCreatesBirdsForItsController() {
        harness.setHand(player2, java.util.List.of(new Ornitharch()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMayAbilityChosen(player2, false))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Bird")).hasSize(2);
        assertThat(findPermanents(player1, "Bird")).isEmpty();
        assertThat(findPermanent(player2, "Ornitharch")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castOrnitharch() {
        harness.setHand(player1, java.util.List.of(new Ornitharch()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
