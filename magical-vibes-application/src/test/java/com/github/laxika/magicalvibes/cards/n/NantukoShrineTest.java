package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NantukoShrineTest extends BaseCardTest {

    @Test
    @DisplayName("The spell's caster creates Squirrels for matching cards in all graveyards")
    void casterCreatesSquirrelsForSameNameCardsInAllGraveyards() {
        harness.addToBattlefield(player1, new NantukoShrine());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new Ornithopter()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Squirrel")).isEqualTo(2);
        assertThat(countPermanents(player1, "Squirrel")).isZero();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Nonmatching graveyard cards do not create Squirrels")
    void nonmatchingCardsDoNotCreateSquirrels() {
        harness.addToBattlefield(player1, new NantukoShrine());
        harness.setGraveyard(player1, List.of(new Ornithopter()));
        harness.setGraveyard(player2, List.of(new Ornithopter()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Squirrel")).isZero();
        assertThat(countPermanents(player1, "Squirrel")).isZero();
    }
}
