package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DisdainfulStrokeTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell with mana value 4")
    void countersManaValue4Spell() {
        GiantSpider spider = new GiantSpider();
        harness.setHand(player1, List.of(spider));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new DisdainfulStroke()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spider.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Giant Spider");
        harness.assertInGraveyard(player2, "Disdainful Stroke");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Counters a spell with mana value greater than 4")
    void countersManaValue5Spell() {
        SerraAngel angel = new SerraAngel();
        harness.setHand(player1, List.of(angel));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.setHand(player2, List.of(new DisdainfulStroke()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, angel.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Serra Angel");
        harness.assertInGraveyard(player2, "Disdainful Stroke");
    }

    @Test
    @DisplayName("Cannot target a spell with mana value 3")
    void cannotTargetManaValue3Spell() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.setHand(player2, List.of(new DisdainfulStroke()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the target spell already left the stack")
    void fizzlesIfTargetGone() {
        GiantSpider spider = new GiantSpider();
        harness.setHand(player1, List.of(spider));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new DisdainfulStroke()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spider.getId());

        GameData gameData = harness.getGameData();
        gameData.stack.removeIf(stackEntry -> stackEntry.getCard().getName().equals("Giant Spider"));

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Disdainful Stroke");
        assertThat(gameData.stack).isEmpty();
    }
}
