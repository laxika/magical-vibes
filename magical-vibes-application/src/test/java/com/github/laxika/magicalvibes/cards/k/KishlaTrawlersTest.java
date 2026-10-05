package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FrontlineRush;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KishlaTrawlers.class, FrontlineRush.class, KnockoutManeuver.class})
class KishlaTrawlersTest extends BaseCardTest {

    private void castKishlaTrawlers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new KishlaTrawlers(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Exiles a creature and returns a chosen instant or sorcery")
    void exilesCreatureAndReturnsChosenSpell() {
        KishlaTrawlers creature = new KishlaTrawlers();
        FrontlineRush instant = new FrontlineRush();
        KnockoutManeuver sorcery = new KnockoutManeuver();
        harness.setGraveyard(player1, List.of(creature, instant, sorcery));

        castKishlaTrawlers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class))
                .isNotNull();
        harness.handleGraveyardCardChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(card -> card.getName())
                .contains("Kishla Trawlers");
        harness.assertInHand(player1, "Knockout Maneuver");
        harness.assertInGraveyard(player1, "Frontline Rush");
        harness.assertNotInGraveyard(player1, "Knockout Maneuver");
    }

    @Test
    @DisplayName("The optional exile can be declined")
    void exileCanBeDeclined() {
        KishlaTrawlers creature = new KishlaTrawlers();
        FrontlineRush instant = new FrontlineRush();
        harness.setGraveyard(player1, List.of(creature, instant));

        castKishlaTrawlers();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Kishla Trawlers");
        harness.assertInGraveyard(player1, "Frontline Rush");
        harness.assertNotInHand(player1, "Frontline Rush");
    }

    @Test
    @DisplayName("Only creature cards can be exiled and only instants or sorceries can be returned")
    void filtersBothChoices() {
        FrontlineRush instant = new FrontlineRush();
        harness.setGraveyard(player1, List.of(instant));

        castKishlaTrawlers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Frontline Rush");
    }

    @Test
    @DisplayName("Can exile itself if it dies before its enter trigger resolves")
    void canExileItselfAfterDyingInResponse() {
        KishlaTrawlers trawlers = new KishlaTrawlers();
        FrontlineRush spell = new FrontlineRush();
        harness.setGraveyard(player1, List.of(spell));
        Permanent permanent = harness.enterBattlefieldAndReturn(player1, trawlers);
        permanent.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Kishla Trawlers");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(trawlers.getId());
        harness.handleMultipleCardsChosen(player1, List.of(trawlers.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(trawlers);
        harness.assertInHand(player1, "Frontline Rush");
    }

    @Test
    @DisplayName("May exile a creature even when no instant or sorcery is available")
    void canExileWithoutAReturnTarget() {
        KishlaTrawlers creature = new KishlaTrawlers();
        harness.setGraveyard(player1, List.of(creature));

        castKishlaTrawlers();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The reflexive return waits for priority and fails if its target leaves the graveyard")
    void returnTargetCanBeRemovedInResponse() {
        KishlaTrawlers creature = new KishlaTrawlers();
        FrontlineRush spell = new FrontlineRush();
        harness.setGraveyard(player1, List.of(creature, spell));

        castKishlaTrawlers();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        harness.assertInGraveyard(player1, "Frontline Rush");
        harness.assertNotInHand(player1, "Frontline Rush");
        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(creature, spell));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Frontline Rush");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature, spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returns a sole instant target but never returns a creature or an opponent's spell")
    void returnsOnlyOwnInstantOrSorcery() {
        KishlaTrawlers exiledCreature = new KishlaTrawlers();
        KishlaTrawlers remainingCreature = new KishlaTrawlers();
        FrontlineRush instant = new FrontlineRush();
        KnockoutManeuver opposingSpell = new KnockoutManeuver();
        harness.setGraveyard(player1, List.of(exiledCreature, remainingCreature, instant));
        harness.setGraveyard(player2, List.of(opposingSpell));

        castKishlaTrawlers();
        harness.handleMultipleCardsChosen(player1, List.of(exiledCreature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Frontline Rush");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(remainingCreature);
        harness.assertInGraveyard(player2, "Knockout Maneuver");
        harness.assertNotInHand(player1, "Knockout Maneuver");
    }
}
