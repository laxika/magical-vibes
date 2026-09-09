package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HorriblyAwry.class, GiantSpider.class, GrizzlyBears.class, MightOfOaks.class, SerraAngel.class})
class HorriblyAwryTest extends BaseCardTest {

    @Test
    @DisplayName("Counters and exiles a creature spell with mana value 4")
    void countersAndExilesCreatureSpellWithManaValueFour() {
        GiantSpider spider = new GiantSpider();
        harness.setHand(player1, List.of(spider));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new HorriblyAwry()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spider.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Giant Spider");
        harness.assertNotOnBattlefield(player1, "Giant Spider");
        org.assertj.core.api.Assertions.assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Giant Spider"));
    }

    @Test
    @DisplayName("Cannot target a creature spell with mana value 5")
    void cannotTargetCreatureSpellWithManaValueFive() {
        SerraAngel angel = new SerraAngel();
        harness.setHand(player1, List.of(angel));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.setHand(player2, List.of(new HorriblyAwry()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, angel.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature spell")
    void cannotTargetNoncreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new HorriblyAwry()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, might.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
