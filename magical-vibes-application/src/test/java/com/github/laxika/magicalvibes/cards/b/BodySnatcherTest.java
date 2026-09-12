package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BodySnatcher.class, Forest.class, GrizzlyBears.class, Juggernaut.class, WrathOfGod.class})
class BodySnatcherTest extends BaseCardTest {

    @Test
    @DisplayName("The ETB ability prompts to discard a creature card")
    void etbPromptsForCreatureDiscard() {
        castBodySnatcherWithCreatureInHand();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Discarding a creature card keeps Body Snatcher on the battlefield")
    void discardingCreatureKeepsBodySnatcher() {
        Card bodySnatcher = castBodySnatcherWithCreatureInHand();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(bodySnatcher.getId()));
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the discard exiles Body Snatcher")
    void decliningDiscardExilesBodySnatcher() {
        Card bodySnatcher = castBodySnatcherWithCreatureInHand();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(bodySnatcher.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(bodySnatcher.getId()));
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(bodySnatcher.getId()));
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An artifact creature card satisfies the creature discard requirement")
    void artifactCreatureCanBeDiscarded() {
        Card bodySnatcher = new BodySnatcher();
        harness.castFromHand(player1, bodySnatcher, "{2}{B}{B}");
        harness.setHand(player1, List.of(new Juggernaut()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(bodySnatcher.getId()));
        harness.assertInGraveyard(player1, "Juggernaut");
    }

    @Test
    @DisplayName("With no creature card in hand, the ETB ability exiles Body Snatcher automatically")
    void noCreatureInHandExilesAutomatically() {
        Card bodySnatcher = new BodySnatcher();
        harness.castFromHand(player1, bodySnatcher, "{2}{B}{B}");
        harness.setHand(player1, List.of(new Forest()));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(bodySnatcher.getId()));
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("When Body Snatcher dies, it exiles itself and returns a targeted creature")
    void deathTriggerExilesSourceAndReturnsTarget() {
        Card bodySnatcher = new BodySnatcher();
        Card target = new GrizzlyBears();
        addCreatureReady(player1, bodySnatcher);
        harness.setGraveyard(player1, List.of(target));
        castWrathOfGod();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .contains(target.getId());

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(bodySnatcher.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("The death trigger can target Body Snatcher, but it is exiled before the return")
    void deathTriggerCannotReturnItsExiledSource() {
        Card bodySnatcher = new BodySnatcher();
        addCreatureReady(player1, bodySnatcher);
        castWrathOfGod();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .contains(bodySnatcher.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bodySnatcher.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(bodySnatcher.getId()));
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(bodySnatcher.getId()));
    }

    @Test
    @DisplayName("The death trigger only targets creature cards in its controller's graveyard")
    void deathTriggerFiltersToOwnCreatureCards() {
        Card bodySnatcher = new BodySnatcher();
        addCreatureReady(player1, bodySnatcher);
        harness.setGraveyard(player1, List.of(new Forest()));
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(opponentCreature));

        castWrathOfGod();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(bodySnatcher.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bodySnatcher.getId()));
        harness.passBothPriorities();

        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(bodySnatcher.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(opponentCreature.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCreature);
    }

    private Card castBodySnatcherWithCreatureInHand() {
        Card bodySnatcher = new BodySnatcher();
        harness.castFromHand(player1, bodySnatcher, "{2}{B}{B}");
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        return bodySnatcher;
    }

    private void castWrathOfGod() {
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
    }
}
