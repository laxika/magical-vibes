package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OathswornVampireTest extends BaseCardTest {

    private void prepareGraveyardCast() {
        harness.setGraveyard(player1, List.of(new OathswornVampire()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Cannot be cast from the graveyard unless you gained life this turn")
    void cannotCastFromGraveyardWithoutLifeGain() {
        prepareGraveyardCast();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card cannot be cast from graveyard");
    }

    @Test
    @DisplayName("Can be cast from the graveyard after gaining life this turn")
    void canCastFromGraveyardAfterGainingLife() {
        prepareGraveyardCast();
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        Permanent vampire = findPermanent(player1, "Oathsworn Vampire");
        assertThat(vampire).isNotNull();
        assertThat(vampire.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's life gain does not enable the graveyard cast")
    void opponentLifeGainDoesNotEnableGraveyardCast() {
        prepareGraveyardCast();
        gd.lifeGainedThisTurn.put(player2.getId(), 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card cannot be cast from graveyard");
    }
}
