package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DundoolinWeaverTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a targeted permanent card from your graveyard to your hand with three creatures")
    void etbReturnsPermanentCardToHandWithThreeCreatures() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FugitiveWizard());

        castDundoolinWeaver();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("ETB does not trigger with fewer than three creatures")
    void etbDoesNotTriggerWithFewerThanThreeCreatures() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addToBattlefield(player1, new GrizzlyBears());

        castDundoolinWeaver();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB cannot target a nonpermanent card")
    void etbCannotTargetNonpermanentCard() {
        Card target = new HolyDay();
        harness.setGraveyard(player1, List.of(target));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FugitiveWizard());

        castDundoolinWeaver();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Holy Day");
    }

    @Test
    @DisplayName("ETB does nothing if the creature count falls below three before resolution")
    void etbDoesNothingIfCreatureCountFallsBelowThree() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FugitiveWizard());

        castDundoolinWeaver();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard().getName().equals("Fugitive Wizard"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void castDundoolinWeaver() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DundoolinWeaver()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
    }
}
