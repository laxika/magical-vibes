package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PelakkaWurmTest extends BaseCardTest {

    @Test
    @DisplayName("When Pelakka Wurm enters, its controller gains 7 life")
    void gainsLifeWhenItEnters() {
        harness.setHand(player1, List.of(new PelakkaWurm()));
        harness.addMana(player1, ManaColor.GREEN, 9);
        harness.setLife(player1, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("When Pelakka Wurm dies, its controller draws a card")
    void drawsCardWhenItDies() {
        harness.addToBattlefield(player1, new PelakkaWurm());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        gd.playerDecks.get(player1.getId()).clear();
        Forest forest = new Forest();
        gd.playerDecks.get(player1.getId()).add(forest);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
    }
}
