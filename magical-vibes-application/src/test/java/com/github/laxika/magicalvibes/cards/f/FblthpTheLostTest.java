package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.Wargate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FblthpTheLost.class, GrizzlyBears.class, Shock.class, Wargate.class})
class FblthpTheLostTest extends BaseCardTest {

    @Test
    @DisplayName("Entering from hand draws one card")
    void enteringFromHandDrawsOneCard() {
        Shock drawn = new Shock();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new FblthpTheLost()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Entering from the library draws two cards")
    void enteringFromLibraryDrawsTwoCards() {
        FblthpTheLost fblthp = new FblthpTheLost();
        GrizzlyBears drawn1 = new GrizzlyBears();
        GrizzlyBears drawn2 = new GrizzlyBears();
        GrizzlyBears remaining = new GrizzlyBears();
        harness.setLibrary(player1, List.of(fblthp, drawn1, drawn2, remaining));
        harness.setHand(player1, List.of(new Wargate()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(search.params().cards().indexOf(fblthp)));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(2)
                .allMatch(card -> card == drawn1 || card == drawn2 || card == remaining);
    }

    @Test
    @DisplayName("Becoming the target of a spell shuffles Fblthp into its owner's library")
    void becomingTargetOfSpellShufflesIntoOwnersLibrary() {
        Permanent fblthp = harness.addToBattlefieldAndReturn(player1, new FblthpTheLost());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, fblthp.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getId().equals(fblthp.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).anyMatch(
                card -> card.getId().equals(fblthp.getCard().getId()));
    }
}
