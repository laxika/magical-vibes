package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.e.Endbringer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PsychicIntrusion.class, Divination.class, GrizzlyBears.class, Swamp.class, Endbringer.class})
class PsychicIntrusionTest extends BaseCardTest {

    @Test
    void choosesANonlandFromTheRevealedHandOrGraveyard() {
        Card land = new Swamp();
        Card handCard = new GrizzlyBears();
        Card graveyardCard = new Divination();
        harness.setHand(player2, List.of(land, handCard));
        harness.setGraveyard(player2, List.of(graveyardCard));

        harness.setHand(player1, List.of(new PsychicIntrusion()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileNonlandCardFromTargetHandOrGraveyardChoice.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(land.getId())))
                .hasMessageContaining("valid nonland card");

        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(graveyardCard.getId());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land, handCard);
    }

    @Test
    void grantsPersistentAnyColorManaCastPermission() {
        Card exiledCard = new Divination();
        harness.setHand(player2, List.of(new Swamp()));
        harness.setGraveyard(player2, List.of(exiledCard));

        harness.setHand(player1, List.of(new PsychicIntrusion()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(exiledCard.getId()));

        assertThat(gd.exilePlayPermissions.get(exiledCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).contains(exiledCard.getId());

        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castFromExile(player1, exiledCard.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Divination");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getId().equals(exiledCard.getId()));
    }

    @Test
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new PsychicIntrusion()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .hasMessageContaining("opponent");
    }

    @Test
    void exilesAHandCardAndCastsItUnderTheChoosersControl() {
        Card chosenCard = new GrizzlyBears();
        Card graveyardCard = new Divination();
        harness.setHand(player2, List.of(chosenCard));
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.setHand(player1, List.of(new PsychicIntrusion()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .hasMessageContaining("exactly one");
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(chosenCard.getId(), graveyardCard.getId())))
                .hasMessageContaining("exactly one");
        harness.handleMultipleCardsChosen(player1, List.of(chosenCard.getId()));

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosenCard);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castFromExile(player1, chosenCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void resolvesWithoutAChoiceWhenBothZonesContainOnlyLands() {
        Card handLand = new Swamp();
        Card graveyardLand = new Swamp();
        harness.setHand(player2, List.of(handLand));
        harness.setGraveyard(player2, List.of(graveyardLand));
        harness.setHand(player1, List.of(new PsychicIntrusion()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handLand);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardLand);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Psychic Intrusion");
    }

    @Test
    void anyColorPermissionDoesNotPayARequiredColorlessManaSymbol() {
        Card chosenCard = new Endbringer();
        harness.setHand(player2, List.of(chosenCard));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new PsychicIntrusion()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(chosenCard.getId()));

        harness.addMana(player1, ManaColor.GREEN, 6);
        assertThatThrownBy(() -> harness.castFromExile(player1, chosenCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosenCard);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, chosenCard.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Endbringer");
    }

    @Test
    void permissionPersistsAcrossTurnsButRespectsTimingAndDoesNotBelongToTheOwner() {
        Card chosenCard = new GrizzlyBears();
        harness.setHand(player2, List.of(chosenCard));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new PsychicIntrusion()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(chosenCard.getId()));

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.GREEN, 2);
        assertThatThrownBy(() -> harness.castFromExile(player2, chosenCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.BLUE, 2);
        assertThatThrownBy(() -> harness.castFromExile(player1, chosenCard.getId()))
                .hasMessageContaining("sorcery-speed");

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, chosenCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosenCard);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, chosenCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }
}
