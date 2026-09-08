package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FelhideBrawler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KragmaButcher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RagemongerTest extends BaseCardTest {

    @Test
    void reducesMatchingBlackColoredManaButNotGenericMana() {
        harness.addToBattlefield(player1, new Ragemonger());
        harness.setHand(player1, List.of(new FelhideBrawler()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void reducesMatchingRedColoredManaButLeavesGenericCost() {
        harness.addToBattlefield(player1, new Ragemonger());
        harness.setHand(player1, List.of(new KragmaButcher()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotReduceNonMinotaurSpells() {
        harness.addToBattlefield(player1, new Ragemonger());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotReduceOpponentMinotaurSpells() {
        harness.addToBattlefield(player1, new Ragemonger());
        harness.setHand(player2, List.of(new FelhideBrawler()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
