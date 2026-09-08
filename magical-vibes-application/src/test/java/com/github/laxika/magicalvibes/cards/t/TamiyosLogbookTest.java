package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TamiyosLogbookTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card with its activation cost reduced by other artifacts")
    void drawsCardWithReducedCost() {
        Permanent logbook = harness.addToBattlefieldAndReturn(player1, new TamiyosLogbook());
        harness.addToBattlefield(player1, new Millstone());
        harness.addToBattlefield(player1, new Millstone());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(logbook.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not count Tamiyo's Logbook when reducing its own activation cost")
    void doesNotCountItself() {
        harness.addToBattlefield(player1, new TamiyosLogbook());
        harness.addToBattlefield(player1, new Millstone());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
