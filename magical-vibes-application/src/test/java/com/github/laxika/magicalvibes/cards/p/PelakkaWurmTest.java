package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PelakkaWurm.class, Forest.class, WrathOfGod.class, Disperse.class})
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

        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
    }

    @Test
    @DisplayName("Each Wurm dying simultaneously draws exactly one card for its own controller")
    void simultaneousDeathsDrawForEachController() {
        harness.addToBattlefield(player1, new PelakkaWurm());
        harness.addToBattlefield(player2, new PelakkaWurm());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.setHand(player2, List.of());
        Forest firstDraw = new Forest();
        Forest secondDraw = new Forest();
        harness.setLibrary(player1, List.of(firstDraw, new Forest()));
        harness.setLibrary(player2, List.of(secondDraw, new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Returning Pelakka Wurm to hand does not trigger a draw")
    void returningToHandDoesNotDraw() {
        PelakkaWurm wurm = new PelakkaWurm();
        var permanent = harness.addToBattlefieldAndReturn(player1, wurm);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Disperse()));
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player2, 0, permanent.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(wurm);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }


    @Test
    @DisplayName("The enter trigger still gains life after Pelakka Wurm leaves the battlefield")
    void enterTriggerResolvesAfterWurmLeaves() {
        PelakkaWurm wurm = new PelakkaWurm();
        harness.setHand(player1, List.of(wurm));
        harness.setHand(player2, List.of(new Disperse()));
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        var permanent = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.castAndResolveInstant(player2, 0, permanent.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(wurm);
        harness.assertLife(player1, 10);

        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 10);
        assertThat(gd.stack).isEmpty();
    }

}
