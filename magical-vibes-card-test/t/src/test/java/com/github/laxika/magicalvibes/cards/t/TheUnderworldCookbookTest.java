package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.j.JadeAvenger;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheUnderworldCookbook.class, JadeAvenger.class})
class TheUnderworldCookbookTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card creates a Food token")
    void discardingACardCreatesFoodToken() {
        Card discarded = new JadeAvenger();
        harness.addToBattlefield(player1, new TheUnderworldCookbook());
        harness.setHand(player1, List.of(discarded));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Food");
        harness.assertInGraveyard(player1, "Jade Avenger");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing the Cookbook returns a target creature card from the graveyard")
    void sacrificesAndReturnsTargetCreature() {
        harness.addToBattlefield(player1, new TheUnderworldCookbook());
        Card creature = new JadeAvenger();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Jade Avenger");
        harness.assertInGraveyard(player1, "The Underworld Cookbook");
        harness.assertNotOnBattlefield(player1, "The Underworld Cookbook");
    }

    @Test
    @DisplayName("The second ability cannot target a noncreature card")
    void secondAbilityRequiresCreatureTarget() {
        harness.addToBattlefield(player1, new TheUnderworldCookbook());
        Card noncreature = new TheUnderworldCookbook();
        harness.setGraveyard(player1, List.of(noncreature));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(noncreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void foodCanBeSacrificedImmediatelyToGainThreeLife() {
        harness.addToBattlefield(player1, new TheUnderworldCookbook());
        harness.setHand(player1, List.of(new TheUnderworldCookbook()));
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        assertThat(findPermanent(player1, "The Underworld Cookbook").isTapped()).isTrue();
        harness.assertInGraveyard(player1, "The Underworld Cookbook");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Food");
        harness.assertLife(player1, 10);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertLife(player1, 10);
        harness.passBothPriorities();
        harness.assertLife(player1, 13);
        harness.assertLife(player2, 10);
    }

    @Test
    void firstAbilityCannotBeActivatedWithoutACardToDiscard() {
        harness.addToBattlefield(player1, new TheUnderworldCookbook());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanent(player1, "The Underworld Cookbook").isTapped()).isFalse();
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    void tappedCookbookCannotCreateFood() {
        harness.addToBattlefield(player1, new TheUnderworldCookbook());
        findPermanent(player1, "The Underworld Cookbook").tap();
        harness.setHand(player1, List.of(new JadeAvenger()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Jade Avenger");
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    void secondAbilityCannotTargetOpponentsCreatureCard() {
        harness.addToBattlefield(player1, new TheUnderworldCookbook());
        Card creature = new JadeAvenger();
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "The Underworld Cookbook");
        harness.assertInGraveyard(player2, "Jade Avenger");
    }

    @Test
    void secondAbilityRequiresFourMana() {
        harness.addToBattlefield(player1, new TheUnderworldCookbook());
        Card creature = new JadeAvenger();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "The Underworld Cookbook");
        assertThat(findPermanent(player1, "The Underworld Cookbook").isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Jade Avenger");
    }

    @Test
    void secondAbilityCannotBeActivatedWhileTapped() {
        harness.addToBattlefield(player1, new TheUnderworldCookbook());
        findPermanent(player1, "The Underworld Cookbook").tap();
        Card creature = new JadeAvenger();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "The Underworld Cookbook");
        harness.assertInGraveyard(player1, "Jade Avenger");
    }

    @Test
    void removedGraveyardTargetIsNotReturnedAndSacrificeIsNotRefunded() {
        harness.addToBattlefield(player1, new TheUnderworldCookbook());
        Card creature = new JadeAvenger();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(creature.getId()));
        harness.assertNotOnBattlefield(player1, "The Underworld Cookbook");
        harness.assertInGraveyard(player1, "The Underworld Cookbook");
        harness.assertNotInHand(player1, "Jade Avenger");

        gd.playerGraveyards.get(player1.getId()).remove(creature);
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Jade Avenger");
        harness.assertInGraveyard(player1, "The Underworld Cookbook");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
    }

    @Test
    void secondAbilityRequiresATargetEvenWithAnEmptyGraveyard() {
        harness.addToBattlefield(player1, new TheUnderworldCookbook());
        harness.setGraveyard(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "The Underworld Cookbook");
        assertThat(findPermanent(player1, "The Underworld Cookbook").isTapped()).isFalse();
    }
}
