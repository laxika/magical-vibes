package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.m.MeteorShower;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Thoughtbind.class, GiantSpider.class, GrizzlyBears.class, MeteorShower.class,
        SerraAngel.class, Shock.class})
class ThoughtbindTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell with mana value 4")
    void countersManaValue4Spell() {
        GiantSpider spider = new GiantSpider();
        harness.setHand(player1, List.of(spider));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new Thoughtbind()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spider.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Giant Spider");
        harness.assertNotOnBattlefield(player1, "Giant Spider");
        harness.assertInGraveyard(player2, "Thoughtbind");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Counters a cheap noncreature spell")
    void countersCheapInstant() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.setHand(player2, List.of(new Thoughtbind()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, shock.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        Thoughtbind thoughtbind = new Thoughtbind();
        harness.setHand(player2, List.of(thoughtbind));
        harness.addMana(player2, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spell on the stack");
        assertThat(harness.getGameData().playerHands.get(player2.getId())).containsExactly(thoughtbind);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a double-X spell whose mana value exceeds 4")
    void cannotTargetDoubleXSpellAboveManaValueLimit() {
        MeteorShower meteorShower = new MeteorShower();
        harness.setHand(player1, List.of(meteorShower));
        harness.addMana(player1, ManaColor.RED, 7);

        // Meteor Shower costs {X}{X}{R}; X = 3 gives it mana value 6.
        harness.castSorceryForX(player1, 0, 3, Map.of(player2.getId(), 4));
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Thoughtbind()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, meteorShower.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a spell with mana value 5")
    void cannotTargetManaValue5Spell() {
        SerraAngel angel = new SerraAngel();
        harness.setHand(player1, List.of(angel));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.setHand(player2, List.of(new Thoughtbind()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, angel.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the target spell already left the stack")
    void fizzlesIfTargetGone() {
        GiantSpider spider = new GiantSpider();
        harness.setHand(player1, List.of(spider));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new Thoughtbind()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spider.getId());

        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Giant Spider"));

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Thoughtbind");
        assertThat(gd.stack).isEmpty();
    }
}
