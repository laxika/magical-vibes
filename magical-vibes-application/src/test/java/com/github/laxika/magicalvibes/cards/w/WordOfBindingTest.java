package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WordOfBindingTest extends BaseCardTest {

    @Test
    @DisplayName("X=2 taps both target creatures")
    void tapsAllTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WordOfBinding()));
        harness.addMana(player1, ManaColor.BLACK, 4); // X=2: {2}{B}{B}

        harness.castSorcery(player1, 0, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-creature")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new WordOfBinding()));
        harness.addMana(player1, ManaColor.BLACK, 3); // X=1: {1}{B}{B}

        UUID forestId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(forestId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
