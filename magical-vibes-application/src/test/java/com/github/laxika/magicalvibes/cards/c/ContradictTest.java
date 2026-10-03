package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.d.DragonlordDromoka;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Contradict.class, GrizzlyBears.class, Millstone.class, DragonlordDromoka.class})
class ContradictTest extends BaseCardTest {

    @Test
    void countersTargetSpellAndDrawsACard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player1, bears, "{1}{G}");

        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new Contradict()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Contradict");
    }

    @Test
    void cannotTargetAPermanent() {
        Millstone millstone = new Millstone();
        harness.addToBattlefield(player1, millstone);

        harness.setHand(player2, List.of(new Contradict()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, millstone.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotDrawWhenTargetWasAlreadyCountered() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player1, bears, "{1}{G}");
        harness.setHand(player2, List.of(new Contradict(), new Contradict()));
        GrizzlyBears firstDraw = new GrizzlyBears();
        GrizzlyBears secondDraw = new GrizzlyBears();
        harness.setLibrary(player2, List.of(firstDraw, secondDraw));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.addMana(player2, ManaColor.COLORLESS, 6);

        harness.castInstant(player2, 0, bears.getId());
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(firstDraw);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(firstDraw);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(secondDraw);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canCounterAnotherContradictAndDrawOnlyForTheResolvingSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player1, bears, "{1}{G}");
        Contradict firstCounter = new Contradict();
        harness.setHand(player2, List.of(firstCounter));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.castInstant(player2, 0, bears.getId());

        harness.setHand(player1, List.of(new Contradict()));
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, firstCounter.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Contradict");
        harness.assertInGraveyard(player2, "Contradict");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void drawsEvenWhenTargetSpellCannotBeCountered() {
        DragonlordDromoka dromoka = new DragonlordDromoka();
        harness.castFromHand(player1, dromoka, "{4}{G}{W}");
        harness.setHand(player1, List.of(new Contradict()));
        DragonlordDromoka drawn = new DragonlordDromoka();
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, dromoka.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.stack).hasSize(1);
        harness.assertNotInGraveyard(player1, "Dragonlord Dromoka");
        harness.assertInGraveyard(player1, "Contradict");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Dragonlord Dromoka");
    }
}
