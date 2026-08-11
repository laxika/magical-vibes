package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmbodimentOfAgoniesTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one counter for each different nonland mana cost")
    void entersWithCountersForDistinctManaCosts() {
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player1.getId()).add(new Shock());
        gd.playerGraveyards.get(player1.getId()).add(new GiantGrowth());
        gd.playerGraveyards.get(player1.getId()).add(new Forest());
        Card cardWithoutManaCost = new Card();
        cardWithoutManaCost.setType(CardType.SORCERY);
        gd.playerGraveyards.get(player1.getId()).add(cardWithoutManaCost);

        harness.setHand(player1, List.of(new EmbodimentOfAgonies()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent embodiment = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Embodiment of Agonies"))
                .findFirst()
                .orElseThrow();
        assertThat(embodiment.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counts only the controller's graveyard")
    void countsOnlyControllersGraveyard() {
        gd.playerGraveyards.get(player2.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player2.getId()).add(new Shock());

        harness.setHand(player1, List.of(new EmbodimentOfAgonies()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Embodiment of Agonies"));
    }
}
