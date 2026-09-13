package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExplosiveSingularity.class, GrizzlyBears.class, Spellbook.class})
class ExplosiveSingularityTest extends BaseCardTest {

    @Test
    void tapsCreaturesToReduceTheGenericCost() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ExplosiveSingularity()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorceryWithSacrifices(player1, 0, player2.getId(),
                List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
    }

    @Test
    void canBeCastWithoutTappingCreatures() {
        harness.setHand(player1, List.of(new ExplosiveSingularity()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castSorceryWithSacrifices(player1, 0, player2.getId(), List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
    }

    @Test
    void cannotTapNonCreatureToReduceTheCost() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setHand(player1, List.of(new ExplosiveSingularity()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(
                player1, 0, player2.getId(), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(artifact.isTapped()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }
}
