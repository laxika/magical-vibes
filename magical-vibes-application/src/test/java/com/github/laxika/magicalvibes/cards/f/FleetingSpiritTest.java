package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FleetingSpirit.class, Forest.class})
class FleetingSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling three cards from the graveyard grants first strike until end of turn")
    void exilingThreeCardsGrantsFirstStrikeUntilEndOfTurn() {
        Permanent spirit = addSpirit(player1);
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
        assertThat(gqs.hasKeyword(gd, spirit, Keyword.FIRST_STRIKE)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, spirit, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gqs.hasKeyword(gd, spirit, Keyword.FIRST_STRIKE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, spirit, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The first ability cannot be activated with fewer than three cards in the graveyard")
    void firstAbilityRequiresThreeGraveyardCards() {
        addSpirit(player1);
        harness.setGraveyard(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Discarding a card exiles Fleeting Spirit and returns it at the next end step")
    void discardExilesAndReturnsAtNextEndStep() {
        addSpirit(player1);
        harness.setHand(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fleeting Spirit");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Fleeting Spirit"));

        harness.passUntil(TurnStep.END_STEP);
        harness.assertNotOnBattlefield(player1, "Fleeting Spirit");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fleeting Spirit");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Fleeting Spirit"));
    }

    private Permanent addSpirit(Player player) {
        return harness.addToBattlefieldAndReturn(player, new FleetingSpirit());
    }

    @Test
    @DisplayName("The controller chooses exactly three cards when more are available")
    void choosesThreeCardsFromLargerGraveyard() {
        Permanent spirit = addSpirit(player1);
        Forest retained = new Forest();
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        harness.setGraveyard(player1, List.of(retained, first, second, third));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(retained);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second, third);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, spirit, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Flickering in response to the first strike ability does not grant it to the returned spirit")
    void firstStrikeDoesNotFollowSpiritThroughExile() {
        addSpirit(player1);
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Fleeting Spirit");
        harness.passBothPriorities();

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Fleeting Spirit");
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The first ability requires white mana and cannot exile an opponent's graveyard")
    void firstAbilityRequiresItsControllersCosts() {
        addSpirit(player1);
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);

        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The discard ability cannot be activated with an empty hand")
    void discardAbilityRequiresACardInHand() {
        addSpirit(player1);
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Fleeting Spirit");
    }

    @Test
    @DisplayName("Activating during the end step waits for the following turn's end step")
    void endStepActivationWaitsForNextEndStep() {
        addSpirit(player1);
        harness.setHand(player1, List.of(new Forest()));
        harness.passUntil(TurnStep.END_STEP);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fleeting Spirit");
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.assertNotOnBattlefield(player1, "Fleeting Spirit");
        harness.passUntil(player2, TurnStep.END_STEP);
        harness.assertNotOnBattlefield(player1, "Fleeting Spirit");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Fleeting Spirit");
    }

    @Test
    @DisplayName("A spirit controlled by another player returns untapped under its owner's control")
    void stolenSpiritReturnsToOwnerAsANewPermanent() {
        Permanent spirit = addSpirit(player1);
        spirit.setTapped(true);
        gd.playerBattlefields.get(player1.getId()).remove(spirit);
        gd.playerBattlefields.get(player2.getId()).add(spirit);
        gd.stolenCreatures.put(spirit.getId(), player1.getId());
        harness.setHand(player2, List.of(new Forest()));

        harness.activateAbility(player2, 0, 1, null, null);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fleeting Spirit");
        harness.assertInGraveyard(player2, "Forest");
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fleeting Spirit");
        harness.assertNotOnBattlefield(player2, "Fleeting Spirit");
        Permanent returned = findPermanent(player1, "Fleeting Spirit");
        assertThat(returned.getId()).isNotEqualTo(spirit.getId());
        assertThat(returned.isTapped()).isFalse();
    }
}
