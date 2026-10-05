package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AgateBladeAssassin;
import com.github.laxika.magicalvibes.cards.f.FallOfTheFirstCivilization;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LaylaHassan.class, LeoninScimitar.class, GrizzlyBears.class, AgateBladeAssassin.class,
        FallOfTheFirstCivilization.class})
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

    @Test
    @DisplayName("ETB can return a legendary nonartifact creature")
    void etbReturnsLegendaryCreature() {
        assertEtbReturns(new LaylaHassan(), "Layla Hassan");
    }

    @Test
    @DisplayName("ETB can return a nonlegendary Saga")
    void etbReturnsSaga() {
        assertEtbReturns(new FallOfTheFirstCivilization(), "Fall of the First Civilization");
    }

    @Test
    @DisplayName("ETB cannot target a historic card in the opponent's graveyard")
    void etbDoesNotReturnOpponentsCard() {
        harness.setGraveyard(player2, List.of(new LaylaHassan()));

        castLayla();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Layla Hassan");
        harness.assertNotInHand(player1, "Layla Hassan");
    }

    @Test
    @DisplayName("Layla's own first-strike combat damage triggers her ability")
    void ownCombatDamageReturnsHistoricCard() {
        attacking(new LaylaHassan());
        Card historic = new FallOfTheFirstCivilization();
        harness.setGraveyard(player1, List.of(historic));

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(historic.getId());
        harness.handleMultipleCardsChosen(player1, List.of(historic.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Fall of the First Civilization");
    }

    @Test
    @DisplayName("Simultaneous Assassin damage returns only one card even when more targets remain")
    void simultaneousDamageDoesNotCreateExtraTrigger() {
        addCreatureReady(player1, new LaylaHassan());
        attacking(new AgateBladeAssassin());
        attacking(new AgateBladeAssassin());
        Card chosen = new LaylaHassan();
        Card remaining = new FallOfTheFirstCivilization();
        harness.setGraveyard(player1, List.of(chosen, remaining));

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(chosen.getId(), remaining.getId());
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Layla Hassan");
        harness.assertInGraveyard(player1, "Fall of the First Civilization");
    }

    @Test
    @DisplayName("An opposing Assassin dealing combat damage does not trigger Layla")
    void opposingAssassinDamageDoesNotTrigger() {
        addCreatureReady(player1, new LaylaHassan());
        Permanent attacker = addCreatureReady(player2, new LaylaHassan());
        attacker.setAttacking(true);
        harness.setGraveyard(player1, List.of(new FallOfTheFirstCivilization()));

        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Fall of the First Civilization");
        harness.assertNotInHand(player1, "Fall of the First Civilization");
    }

    @Test
    @DisplayName("A historic target that leaves the graveyard is not replaced by another target")
    void removedTargetIsNotReturnedOrReplaced() {
        Card chosen = new LaylaHassan();
        Card remaining = new FallOfTheFirstCivilization();
        harness.setGraveyard(player1, List.of(chosen, remaining));
        castLayla();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        assertThat(gd.stack).isNotEmpty();
        harness.setGraveyard(player1, List.of(remaining));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Layla Hassan");
        harness.assertNotInHand(player1, "Fall of the First Civilization");
        harness.assertInGraveyard(player1, "Fall of the First Civilization");
    }

    private void assertEtbReturns(Card historic, String name) {
        harness.setGraveyard(player1, List.of(historic));
        castLayla();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(historic.getId());
        harness.handleMultipleCardsChosen(player1, List.of(historic.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, name);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void castLayla() {
        harness.castFromHand(player1, new LaylaHassan(), "{3}{W}");
    }

    private Permanent attacking(Card card) {
        Permanent permanent = addCreatureReady(player1, card);
        permanent.setAttacking(true);
        return permanent;
    }
}
