package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OversoldCemetery.class, ElvishWarrior.class, Naturalize.class})
class OversoldCemeteryTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers at upkeep with four creature cards in the graveyard")
    void triggersWithFourCreatureCards() {
        harness.addToBattlefield(player1, new OversoldCemetery());
        harness.setGraveyard(player1, List.of(
                new ElvishWarrior(), new ElvishWarrior(), new ElvishWarrior(), new ElvishWarrior()));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
    }

    @Test
    @DisplayName("Requires exactly one creature target when the ability triggers")
    void requiresOneTarget() {
        harness.addToBattlefield(player1, new OversoldCemetery());
        List<Card> creatures = List.of(
                new ElvishWarrior(), new ElvishWarrior(), new ElvishWarrior(), new ElvishWarrior());
        harness.setGraveyard(player1, creatures);

        advanceToUpkeep(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds())
                .containsExactlyElementsOf(creatures.stream().map(Card::getId).toList());
    }

    @Test
    @DisplayName("Returns the chosen creature card to hand")
    void returnsChosenCreatureToHand() {
        harness.addToBattlefield(player1, new OversoldCemetery());
        Card target = new ElvishWarrior();
        harness.setGraveyard(player1, List.of(
                new Naturalize(), new ElvishWarrior(), new ElvishWarrior(), new ElvishWarrior(), target));

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Elvish Warrior");
        assertThat(gd.playerHands.get(player1.getId())).contains(target);
        harness.assertInGraveyard(player1, "Naturalize");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Does not trigger with fewer than four creature cards")
    void doesNotTriggerBelowThreshold() {
        harness.addToBattlefield(player1, new OversoldCemetery());
        harness.setGraveyard(player1, List.of(
                new ElvishWarrior(), new ElvishWarrior(), new ElvishWarrior(), new Naturalize()));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Does not use an opponent's graveyard for the threshold")
    void doesNotTriggerFromOpponentsGraveyard() {
        harness.addToBattlefield(player1, new OversoldCemetery());
        harness.setGraveyard(player2, List.of(
                new ElvishWarrior(), new ElvishWarrior(), new ElvishWarrior(), new ElvishWarrior()));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Can decline returning the chosen creature when the ability resolves")
    void canDeclineOnResolution() {
        harness.addToBattlefield(player1, new OversoldCemetery());
        Card target = new ElvishWarrior();
        harness.setGraveyard(player1, List.of(
                target, new ElvishWarrior(), new ElvishWarrior(), new ElvishWarrior()));

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        harness.assertNotInHand(player1, "Elvish Warrior");
    }

    @Test
    @DisplayName("Only creature cards are legal graveyard targets")
    void onlyCreatureCardsAreTargets() {
        harness.addToBattlefield(player1, new OversoldCemetery());
        harness.setGraveyard(player1, List.of(
                new Naturalize(), new ElvishWarrior(), new ElvishWarrior(), new ElvishWarrior(), new ElvishWarrior()));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isInstanceOf(
                PendingInteraction.MultiGraveyardChoice.class);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.cards()).extracting(Card::getName).containsOnly("Elvish Warrior");
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new OversoldCemetery());
        harness.setGraveyard(player1, List.of(
                new ElvishWarrior(), new ElvishWarrior(), new ElvishWarrior(), new ElvishWarrior()));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Does nothing if the creature threshold is lost before resolution")
    void rechecksThresholdOnResolution() {
        harness.addToBattlefield(player1, new OversoldCemetery());
        Card target = new ElvishWarrior();
        Card removed = new ElvishWarrior();
        harness.setGraveyard(player1, List.of(
                target, removed, new ElvishWarrior(), new ElvishWarrior()));

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(removed);
        harness.setExile(player1, List.of(removed));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target).hasSize(3);
        harness.assertNotInHand(player1, "Elvish Warrior");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not return a target that leaves the graveyard before resolution")
    void targetMustRemainInGraveyard() {
        harness.addToBattlefield(player1, new OversoldCemetery());
        Card target = new ElvishWarrior();
        harness.setGraveyard(player1, List.of(
                target, new ElvishWarrior(), new ElvishWarrior(), new ElvishWarrior(), new ElvishWarrior()));

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(target);
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
        harness.assertNotInHand(player1, "Elvish Warrior");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
