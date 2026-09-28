package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ValeriaRichardsPrecocious.class, MindStone.class, GrizzlyBears.class, SerraAngel.class})
class ValeriaRichardsPrecociousTest extends BaseCardTest {

    @Test
    @DisplayName("Noncreature spells you cast cost {1} less")
    void reducesNoncreatureSpellCosts() {
        harness.addToBattlefield(player1, new ValeriaRichardsPrecocious());
        harness.setHand(player1, List.of(new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).isNotEmpty();
    }

    @Test
    @DisplayName("The cost reduction does not apply to creature spells")
    void doesNotReduceCreatureSpellCosts() {
        harness.addToBattlefield(player1, new ValeriaRichardsPrecocious());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Draws a card for the first noncreature spell each turn")
    void drawsForFirstNoncreatureSpellOnly() {
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new SerraAngel();
        harness.addToBattlefield(player1, new ValeriaRichardsPrecocious());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new MindStone(), new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).contains(firstDraw);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(secondDraw);
    }
}
