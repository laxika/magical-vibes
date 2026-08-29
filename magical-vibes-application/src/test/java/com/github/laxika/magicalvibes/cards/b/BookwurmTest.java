package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookwurmTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains 3 life and draws a card")
    void etbGainsLifeAndDrawsCard() {
        Card drawCard = new Forest();
        harness.setHand(player1, List.of(new Bookwurm()));
        harness.setLibrary(player1, List.of(drawCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(drawCard.getId());
    }

    @Test
    @DisplayName("Graveyard ability puts Bookwurm third from the top of its owner's library")
    void graveyardAbilityPutsBookwurmThirdFromTop() {
        Card bookwurm = new Bookwurm();
        Card top = new Plains();
        Card second = new Island();
        Card third = new Mountain();
        Card fourth = new Forest();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(bookwurm));
        harness.setLibrary(player1, List.of(top, second, third, fourth));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(top.getId(), second.getId(), bookwurm.getId(), third.getId(), fourth.getId());
    }

    @Test
    @DisplayName("Graveyard ability puts Bookwurm on the bottom when the library has fewer than three cards")
    void graveyardAbilityClampsToLibraryBottom() {
        Card bookwurm = new Bookwurm();
        Card top = new Plains();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(bookwurm));
        harness.setLibrary(player1, List.of(top));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(top.getId(), bookwurm.getId());
    }
}
