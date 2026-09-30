package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JukaiLiberator.class, Forest.class, GrizzlyBears.class, Shock.class})
class JukaiLiberatorTest extends BaseCardTest {

    @Test
    void seeksAChosenLand() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(forest, bears, shock));
        addCreatureReady(player1, new JukaiLiberator());

        dealCombatDamageAndChoose("Land");

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(bears, shock);
    }

    @Test
    void seeksAChosenNonlandPermanent() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(forest, bears, shock));
        addCreatureReady(player1, new JukaiLiberator());

        dealCombatDamageAndChoose("Nonland");

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, shock);
    }

    private void dealCombatDamageAndChoose(String choice) {
        declareAttackers(List.of(0));
        resolveCombat();
        harness.handleListChoice(player1, choice);
    }
}
