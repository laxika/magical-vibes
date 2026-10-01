package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CunningAzurescaleDiviningDive.class, Forest.class, Island.class, GrizzlyBears.class})
class CunningAzurescaleDiviningDiveTest extends BaseCardTest {

    @Test
    void creatureFaceSeeksTwoCardsOfChosenKind() {
        Card forest = new Forest();
        Card island = new Island();
        Card nonland = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, island, nonland));
        harness.setHand(player1, List.of(new CunningAzurescaleDiviningDive()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "LAND");

        assertThat(gd.playerHands.get(player1.getId())).contains(forest, island);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(nonland);
    }

    @Test
    void adventureSeeksOneCardOfChosenKindAndExilesTheCard() {
        Card forest = new Forest();
        Card nonland = new GrizzlyBears();
        CunningAzurescaleDiviningDive card = new CunningAzurescaleDiviningDive();
        harness.setLibrary(player1, List.of(forest, nonland));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "NONLAND");

        assertThat(gd.playerHands.get(player1.getId())).contains(nonland);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }
}
