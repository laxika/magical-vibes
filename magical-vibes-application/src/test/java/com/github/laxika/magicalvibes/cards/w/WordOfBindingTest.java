package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CityOfShadows;
import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WordOfBinding.class, Squire.class, CityOfShadows.class})
class WordOfBindingTest extends BaseCardTest {

    @Test
    @DisplayName("X=2 taps both target creatures")
    void tapsAllTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Squire());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Squire());
        harness.setHand(player1, List.of(new WordOfBinding()));
        harness.addMana(player1, ManaColor.BLACK, 4); // X=2: {2}{B}{B}

        harness.castSorcery(player1, 0, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Requires exactly X targets")
    void requiresExactlyXTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Squire());
        harness.setHand(player1, List.of(new WordOfBinding()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-creature")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new CityOfShadows());
        harness.setHand(player1, List.of(new WordOfBinding()));
        harness.addMana(player1, ManaColor.BLACK, 3); // X=1: {1}{B}{B}

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
