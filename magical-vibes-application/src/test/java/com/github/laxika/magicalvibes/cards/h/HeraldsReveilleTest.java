package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IxallisDiviner;
import com.github.laxika.magicalvibes.cards.m.MerfolkOfThePearlTrident;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeraldsReveille.class, Forest.class, IxallisDiviner.class, MerfolkOfThePearlTrident.class})
class HeraldsReveilleTest extends BaseCardTest {

    @Test
    void drawsNormallyWhenNoPermanentExplored() {
        Card drawn = new Forest();
        harness.setHand(player1, List.of(new HeraldsReveille()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void seeksAMerfolkInsteadAfterAPermanentExplored() {
        Forest exploredLand = new Forest();
        Card sought = new MerfolkOfThePearlTrident();
        harness.setLibrary(player1, List.of(exploredLand, sought));
        harness.setHand(player1, List.of(new IxallisDiviner(), new HeraldsReveille()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(exploredLand.getId(), sought.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
