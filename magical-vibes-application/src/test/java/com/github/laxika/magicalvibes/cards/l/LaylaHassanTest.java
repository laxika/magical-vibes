package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AgateBladeAssassin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LaylaHassan.class, LeoninScimitar.class, GrizzlyBears.class, AgateBladeAssassin.class})
class LaylaHassanTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a targeted historic card from the graveyard to hand")
    void etbReturnsHistoricCard() {
        Card nonHistoric = new GrizzlyBears();
        Card historic = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(nonHistoric, historic));

        castLayla();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(historic.getId());

        harness.handleMultipleCardsChosen(player1, List.of(historic.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Leonin Scimitar");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB does not trigger when the graveyard has no historic card")
    void etbDoesNotTriggerWithoutHistoricCard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castLayla();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("One or more Assassins dealing combat damage return one historic card")
    void assassinCombatDamageReturnsHistoricCardOnce() {
        addCreatureReady(player1, new LaylaHassan());
        attacking(new AgateBladeAssassin());
        attacking(new AgateBladeAssassin());
        Card historic = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(historic));

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(historic.getId());
        harness.handleMultipleCardsChosen(player1, List.of(historic.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Combat damage from a non-Assassin does not trigger")
    void nonAssassinCombatDamageDoesNotTrigger() {
        addCreatureReady(player1, new LaylaHassan());
        attacking(new GrizzlyBears());
        Card historic = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(historic));

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Leonin Scimitar");
    }

    private void castLayla() {
        harness.setHand(player1, List.of(new LaylaHassan()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }

    private Permanent attacking(Card card) {
        Permanent permanent = addCreatureReady(player1, card);
        permanent.setAttacking(true);
        return permanent;
    }
}
