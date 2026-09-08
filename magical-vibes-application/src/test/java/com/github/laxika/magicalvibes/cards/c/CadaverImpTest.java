package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CadaverImpTest extends BaseCardTest {

    private void castCadaverImp() {
        harness.setHand(player1, List.of(new CadaverImp()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns a chosen creature card from its controller's graveyard to hand")
    void returnsChosenCreatureFromOwnGraveyard() {
        Card card = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(card));

        castCadaverImp();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(card.getId());

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the optional return leaves the creature card in the graveyard")
    void decliningReturnsNothing() {
        Card card = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(card));

        castCadaverImp();

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB does not target noncreature cards")
    void nonCreatureIsNotTargetable() {
        Card card = new LightningBolt();
        harness.setGraveyard(player1, List.of(card));

        castCadaverImp();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Lightning Bolt");
    }

    @Test
    @DisplayName("ETB does not target cards in an opponent's graveyard")
    void onlyTargetsOwnGraveyard() {
        Card card = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(card));

        castCadaverImp();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
