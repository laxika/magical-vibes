package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RampantFrogantua.class, Forest.class, GrizzlyBears.class})
class RampantFrogantuaTest extends BaseCardTest {

    @Test
    @DisplayName("gets +10/+10 for each player who has lost the game")
    void getsBonusForPlayersWhoLostTheGame() {
        Permanent frogantua = harness.addToBattlefieldAndReturn(player1, new RampantFrogantua());

        assertThat(gqs.getEffectivePower(gd, frogantua)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, frogantua)).isEqualTo(3);

        gd.playersWhoLostGameThisMatch.add(UUID.randomUUID());
        gd.playersWhoLostGameThisMatch.add(UUID.randomUUID());

        assertThat(gqs.getEffectivePower(gd, frogantua)).isEqualTo(23);
        assertThat(gqs.getEffectiveToughness(gd, frogantua)).isEqualTo(23);
    }

    @Test
    @DisplayName("may mill combat damage and put any number of milled lands onto the battlefield tapped")
    void millsCombatDamageAndPutsChosenMilledLandsTapped() {
        addAttackingFrogantua();
        Card firstLand = new Forest();
        Card nonland = new GrizzlyBears();
        Card secondLand = new Forest();
        harness.setLibrary(player1, List.of(firstLand, nonland, secondLand));

        resolveCombat();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(firstLand.getId(), secondLand.getId());
        harness.handleMultipleCardsChosen(player1, List.of(firstLand.getId()));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Forest")).singleElement().satisfies(land ->
                assertThat(land.isTapped()).isTrue());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(nonland, secondLand);
    }

    @Test
    @DisplayName("may decline to mill")
    void mayDeclineToMill() {
        addAttackingFrogantua();
        Card topCard = new GrizzlyBears();
        List<Card> library = List.of(topCard, new RampantFrogantua(), new RampantFrogantua());
        harness.setLibrary(player1, library);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("counts players who lost before entering the battlefield")
    void countsLossesBeforeEntering() {
        gd.playersWhoLostGameThisMatch.add(UUID.randomUUID());
        Permanent frogantua = harness.addToBattlefieldAndReturn(player1, new RampantFrogantua());

        assertThat(gqs.getEffectivePower(gd, frogantua)).isEqualTo(13);
        assertThat(gqs.getEffectiveToughness(gd, frogantua)).isEqualTo(13);
    }

    @Test
    @DisplayName("may choose zero lands after milling")
    void mayChooseZeroMilledLands() {
        addAttackingFrogantua();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land, new RampantFrogantua(), new RampantFrogantua()));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3).contains(land);
        assertThat(findPermanents(player1, "Forest")).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("may return all milled lands but cannot return older graveyard lands")
    void returnsAllMilledLandsAndExcludesOlderLands() {
        addAttackingFrogantua();
        Card oldLand = new Forest();
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        Card thirdLand = new Forest();
        Card fourthLand = new Forest();
        harness.setGraveyard(player1, List.of(oldLand));
        harness.setLibrary(player1, List.of(firstLand, secondLand, thirdLand, fourthLand));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(
                firstLand.getId(), secondLand.getId(), thirdLand.getId());
        harness.handleMultipleCardsChosen(player1,
                List.of(firstLand.getId(), secondLand.getId(), thirdLand.getId()));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Forest")).hasSize(3)
                .allSatisfy(land -> assertThat(land.isTapped()).isTrue());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(oldLand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourthLand);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(findPermanents(player2, "Forest")).isEmpty();
    }

    @Test
    @DisplayName("finishes the ability without a land choice when no lands are milled")
    void finishesWhenNoLandsAreMilled() {
        addAttackingFrogantua();
        Card nonland = new RampantFrogantua();
        harness.setLibrary(player1, List.of(nonland, new RampantFrogantua(), new RampantFrogantua()));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3).contains(nonland);
        assertThat(findPermanents(player1, "Forest")).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("cannot choose to mill when the library has fewer cards than damage dealt")
    void cannotChooseToMillWithInsufficientLibrary() {
        addAttackingFrogantua();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));

        resolveCombat();
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            assertThatThrownBy(() -> harness.handleMayAbilityChosen(player1, true))
                    .isInstanceOf(IllegalStateException.class);
            harness.handleMayAbilityChosen(player1, false);
        }

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Forest")).isEmpty();
    }

    @Test
    @DisplayName("mills only damage dealt to the player when trampling over a blocker")
    void millsOnlyTrampleDamageDealtToPlayer() {
        addCreatureReady(player1, new RampantFrogantua());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        harness.setLibrary(player1, List.of(firstLand, secondLand));

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(firstLand.getId());
        harness.handleMultipleCardsChosen(player1, List.of(firstLand.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondLand);
        assertThat(findPermanents(player1, "Forest")).singleElement()
                .satisfies(land -> assertThat(land.isTapped()).isTrue());
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void addAttackingFrogantua() {
        Permanent frogantua = addCreatureReady(player1, new RampantFrogantua());
        frogantua.setAttacking(true);
    }
}
