package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
    @DisplayName("With no creature card in hand, the ETB ability exiles Body Snatcher automatically")
    void noCreatureInHandExilesAutomatically() {
        Card bodySnatcher = new BodySnatcher();
        harness.setHand(player1, List.of(bodySnatcher));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0);
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

    private Card castBodySnatcherWithCreatureInHand() {
        Card bodySnatcher = new BodySnatcher();
        harness.setHand(player1, List.of(bodySnatcher));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        return bodySnatcher;
    }

    private void castWrathOfGod() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
    }
}
