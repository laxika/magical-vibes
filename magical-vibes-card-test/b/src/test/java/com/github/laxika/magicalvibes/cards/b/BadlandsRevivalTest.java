package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DroverGrizzly;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BadlandsRevival.class, Forest.class, DroverGrizzly.class})
class BadlandsRevivalTest extends BaseCardTest {

    @Test
    void returnsACreatureToTheBattlefieldAndAPermanentToHand() {
        Card creature = new DroverGrizzly();
        Card land = new Forest();
        prepareSpell(List.of(creature, land));

        castAndBeginTargeting();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Drover Grizzly");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void targetGroupsCanBeDeclinedIndependently() {
        Card creature = new DroverGrizzly();
        prepareSpell(List.of(creature));

        castAndBeginTargeting();
        harness.handleMultipleCardsChosen(player1, List.of());
        PendingInteraction.MultiGraveyardChoice handChoice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(handChoice.validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Drover Grizzly");
        harness.assertNotOnBattlefield(player1, "Drover Grizzly");
    }

    @Test
    void choosingTheSameCardForBothGroupsOnlyReturnsItToTheBattlefield() {
        Card creature = new DroverGrizzly();
        prepareSpell(List.of(creature));

        castAndBeginTargeting();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Drover Grizzly");
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card.getId().equals(creature.getId()));
    }

    @Test
    void canReturnOnlyTheCreatureToTheBattlefield() {
        Card creature = new DroverGrizzly();
        prepareSpell(List.of(creature));

        castAndBeginTargeting();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Drover Grizzly");
        harness.assertNotInHand(player1, "Drover Grizzly");
        harness.assertNotInGraveyard(player1, "Drover Grizzly");
        assertThat(findPermanent(player1, "Drover Grizzly").isTapped()).isFalse();
        assertThat(findPermanent(player1, "Drover Grizzly").isSummoningSick()).isTrue();
    }

    @Test
    void canDeclineBothTargetsEvenWhenLegalCardsExist() {
        Card creature = new DroverGrizzly();
        Card land = new Forest();
        prepareSpell(List.of(creature, land));

        castAndBeginTargeting();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Drover Grizzly");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Badlands Revival");
        harness.assertNotOnBattlefield(player1, "Drover Grizzly");
        harness.assertNotInHand(player1, "Forest");
    }

    @Test
    void skipsTheCreatureGroupWhenOnlyALandIsAvailable() {
        Card land = new Forest();
        prepareSpell(List.of(land));

        castAndBeginTargeting();
        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(land.getId());
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    void targetGroupsExcludeOpponentsCardsAndNonpermanents() {
        Card creature = new DroverGrizzly();
        Card land = new Forest();
        Card nonpermanent = new BadlandsRevival();
        Card opposingCreature = new DroverGrizzly();
        Card opposingLand = new Forest();
        prepareSpell(List.of(creature, land, nonpermanent));
        harness.setGraveyard(player2, List.of(opposingCreature, opposingLand));

        castAndBeginTargeting();
        PendingInteraction.MultiGraveyardChoice creatureChoice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(creatureChoice.validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of());
        PendingInteraction.MultiGraveyardChoice permanentChoice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(permanentChoice.validCardIds()).containsExactlyInAnyOrder(creature.getId(), land.getId());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Drover Grizzly");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    void canResolveWithoutAnyCardsInTheGraveyard() {
        prepareSpell(List.of());

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Badlands Revival");
    }

    @Test
    void returnsThePermanentWhenTheCreatureTargetLeavesTheGraveyard() {
        Card creature = new DroverGrizzly();
        Card land = new Forest();
        prepareSpell(List.of(creature, land));

        castAndBeginTargeting();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.setGraveyard(player1, List.of(land));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Drover Grizzly");
        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    void returnsTheCreatureWhenThePermanentTargetLeavesTheGraveyard() {
        Card creature = new DroverGrizzly();
        Card land = new Forest();
        prepareSpell(List.of(creature, land));

        castAndBeginTargeting();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.setGraveyard(player1, List.of(creature));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Drover Grizzly");
        harness.assertNotInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Drover Grizzly");
    }

    @Test
    void returnsTwoDifferentCreaturesToTheirRespectiveDestinations() {
        Card battlefieldCreature = new DroverGrizzly();
        Card handCreature = new DroverGrizzly();
        prepareSpell(List.of(battlefieldCreature, handCreature));

        castAndBeginTargeting();
        harness.handleMultipleCardsChosen(player1, List.of(battlefieldCreature.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(handCreature.getId()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Drover Grizzly").getCard().getId())
                .isEqualTo(battlefieldCreature.getId());
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .contains(handCreature.getId()).doesNotContain(battlefieldCreature.getId());
        harness.assertNotInGraveyard(player1, "Drover Grizzly");
    }

    @Test
    void returnsNothingWhenBothTargetsLeaveTheGraveyard() {
        Card creature = new DroverGrizzly();
        Card land = new Forest();
        prepareSpell(List.of(creature, land));

        castAndBeginTargeting();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Drover Grizzly");
        harness.assertNotInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Badlands Revival");
        assertThat(gd.stack).isEmpty();
    }

    private void prepareSpell(List<Card> graveyard) {
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new BadlandsRevival()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void castAndBeginTargeting() {
        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
    }
}
