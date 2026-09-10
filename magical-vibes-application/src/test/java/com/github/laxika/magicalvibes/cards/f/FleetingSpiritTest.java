package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.f.Forest;
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
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, spirit, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);

        advanceToEndStep();

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

        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Fleeting Spirit");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Fleeting Spirit"));
    }

    private Permanent addSpirit(Player player) {
        return harness.addToBattlefieldAndReturn(player, new FleetingSpirit());
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
