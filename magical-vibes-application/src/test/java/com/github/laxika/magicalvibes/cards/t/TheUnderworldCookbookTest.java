package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheUnderworldCookbook.class, GrizzlyBears.class, Forest.class})
class TheUnderworldCookbookTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card creates a Food token")
    void discardingACardCreatesFoodToken() {
        Card discarded = new GrizzlyBears();
        harness.addToBattlefield(player1, new TheUnderworldCookbook());
        harness.setHand(player1, List.of(discarded));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Food");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing the Cookbook returns a target creature card from the graveyard")
    void sacrificesAndReturnsTargetCreature() {
        harness.addToBattlefield(player1, new TheUnderworldCookbook());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "The Underworld Cookbook");
        harness.assertNotOnBattlefield(player1, "The Underworld Cookbook");
    }

    @Test
    @DisplayName("The second ability cannot target a noncreature card")
    void secondAbilityRequiresCreatureTarget() {
        harness.addToBattlefield(player1, new TheUnderworldCookbook());
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
