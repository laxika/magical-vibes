package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DundoolinWeaver.class, GrizzlyBears.class, FugitiveWizard.class, HolyDay.class, Forest.class})
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

    @Test
    @DisplayName("ETB can return a land card and returns only the chosen permanent")
    void etbReturnsOnlyChosenLandCard() {
        Card target = new Forest();
        Card other = new DundoolinWeaver();
        harness.setGraveyard(player1, List.of(target, other));
        harness.addToBattlefield(player1, new DundoolinWeaver());
        harness.addToBattlefield(player1, new DundoolinWeaver());

        castDundoolinWeaver();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Dundoolin Weaver");
    }

    @Test
    @DisplayName("Opposing creatures do not count toward the ETB condition")
    void opposingCreaturesDoNotCount() {
        Card target = new DundoolinWeaver();
        harness.setGraveyard(player1, List.of(target));
        harness.addToBattlefield(player1, new DundoolinWeaver());
        harness.addToBattlefield(player2, new DundoolinWeaver());
        harness.addToBattlefield(player2, new DundoolinWeaver());

        castDundoolinWeaver();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Dundoolin Weaver");
    }

    @Test
    @DisplayName("ETB cannot return a permanent from an opponent's graveyard")
    void cannotReturnOpponentGraveyardCard() {
        harness.setGraveyard(player2, List.of(new DundoolinWeaver()));
        harness.addToBattlefield(player1, new DundoolinWeaver());
        harness.addToBattlefield(player1, new DundoolinWeaver());

        castDundoolinWeaver();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Dundoolin Weaver");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB does not return a target that leaves the graveyard before resolution")
    void targetLeavingGraveyardIsNotReturned() {
        Card target = new DundoolinWeaver();
        harness.setGraveyard(player1, List.of(target));
        harness.addToBattlefield(player1, new DundoolinWeaver());
        harness.addToBattlefield(player1, new DundoolinWeaver());

        castDundoolinWeaver();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(target.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castDundoolinWeaver() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new DundoolinWeaver(), "{1}{G}");
    }
}
