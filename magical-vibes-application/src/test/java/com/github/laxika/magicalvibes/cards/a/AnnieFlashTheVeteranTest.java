package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnnieFlashTheVeteran.class, GrizzlyBears.class, HolyDay.class, SerraAngel.class})
class AnnieFlashTheVeteranTest extends BaseCardTest {

    @Test
    @DisplayName("When cast, returns a permanent with mana value three or less tapped")
    void castReturnsCheapPermanentTapped() {
        Card valid = new GrizzlyBears();
        Card nonPermanent = new HolyDay();
        Card expensive = new SerraAngel();
        harness.setGraveyard(player1, List.of(valid, nonPermanent, expensive));

        castAnnieFlash();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(valid.getId());

        harness.handleMultipleCardsChosen(player1, List.of(valid.getId()));
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(valid.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Entering the battlefield without being cast does not return a graveyard card")
    void uncastEntryDoesNotReturnCard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new AnnieFlashTheVeteran());

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Becoming tapped exiles the top two cards with play permission this turn")
    void becomingTappedExilesTopTwoCards() {
        Permanent annie = harness.addToBattlefieldAndReturn(player1, new AnnieFlashTheVeteran());
        annie.setSummoningSick(false);
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.exilePlayPermissions).containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn)
                .contains(first.getId(), second.getId());
    }

    private void castAnnieFlash() {
        harness.setHand(player1, List.of(new AnnieFlashTheVeteran()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
