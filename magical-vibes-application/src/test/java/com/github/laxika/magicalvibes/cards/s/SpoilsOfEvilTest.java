package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpoilsOfEvilTest extends BaseCardTest {

    private void castSpoilsOfEvil() {
        harness.setHand(player1, List.of(new SpoilsOfEvil()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private int colorlessInPool() {
        return gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS);
    }

    @Test
    @DisplayName("Adds {C} and gains 1 life per artifact or creature card in the target opponent's graveyard")
    void addsManaAndLifePerMatchingCard() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new HillGiant(), new SisaysRing()));

        castSpoilsOfEvil();

        assertThat(colorlessInPool()).isEqualTo(3);
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Cards that are neither artifacts nor creatures are ignored")
    void ignoresOtherCardTypes() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new Mountain(), new Mountain()));

        castSpoilsOfEvil();

        assertThat(colorlessInPool()).isEqualTo(1);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("An artifact creature card counts only once")
    void artifactCreatureCountsOnce() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player2, List.of(new Ornithopter()));

        castSpoilsOfEvil();

        assertThat(colorlessInPool()).isEqualTo(1);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Only the target opponent's graveyard is counted, not the caster's")
    void ignoresCastersGraveyard() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new HillGiant()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        castSpoilsOfEvil();

        assertThat(colorlessInPool()).isEqualTo(1);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Adds no mana and no life when the opponent's graveyard has no matching cards")
    void addsNothingWithoutMatchingCards() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player2, List.of(new Mountain()));

        castSpoilsOfEvil();

        assertThat(colorlessInPool()).isEqualTo(0);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new SpoilsOfEvil()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }
}
