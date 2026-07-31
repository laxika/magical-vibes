package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DuskborneSkymarcher;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EdgarMarkovTest extends BaseCardTest {

    @Test
    @DisplayName("Eminence: casting a Vampire spell from the command zone creates a 1/1 black Vampire token")
    void commandZoneEminenceCreatesToken() {
        addToCommandZone(player1, new EdgarMarkov());

        harness.setHand(player1, List.of(new DuskborneSkymarcher()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Vampire")).hasSize(1);
    }

    @Test
    @DisplayName("Eminence: casting a Vampire spell from the battlefield creates a 1/1 black Vampire token")
    void battlefieldEminenceCreatesToken() {
        addCreatureReady(player1, new EdgarMarkov());

        harness.setHand(player1, List.of(new DuskborneSkymarcher()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Vampire")).hasSize(1);
    }

    @Test
    @DisplayName("Eminence: a non-Vampire spell does not create a token")
    void nonVampireSpellDoesNotCreateToken() {
        addToCommandZone(player1, new EdgarMarkov());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Vampire")).isEmpty();
    }

    @Test
    @DisplayName("Attacking puts a +1/+1 counter on each Vampire you control")
    void attackPutsCounterOnEachVampire() {
        Permanent edgar = addCreatureReady(player1, new EdgarMarkov());
        Permanent skymarcher = addCreatureReady(player1, new DuskborneSkymarcher());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(edgar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(skymarcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void addToCommandZone(Player player, Card card) {
        gd.playerCommandZones.get(player.getId()).add(card);
    }
}
