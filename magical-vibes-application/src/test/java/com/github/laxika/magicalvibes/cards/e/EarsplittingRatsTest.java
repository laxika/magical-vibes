package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EarsplittingRats.class, GrizzlyBears.class, Shock.class})
class EarsplittingRatsTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, each player discards a card")
    void eachPlayerDiscardsOnEnter() {
        harness.setHand(player1, List.of(new EarsplittingRats(), new EarsplittingRats()));
        harness.setHand(player2, List.of(new EarsplittingRats()));

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Earsplitting Rats");
        harness.assertInGraveyard(player2, "Earsplitting Rats");
    }

    @Test
    @DisplayName("An empty hand does not create a discard choice")
    void emptyHandDoesNotCreateDiscardChoice() {
        harness.setHand(player1, List.of(new EarsplittingRats(), new EarsplittingRats()));
        harness.setHand(player2, List.of());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Earsplitting Rats");
    }

    @Test
    @DisplayName("Discarding a card grants a regeneration shield")
    void discardingCardGrantsRegenerationShield() {
        Permanent rats = addCreatureReady(player1, new EarsplittingRats());
        harness.setHand(player1, List.of(new EarsplittingRats(), new EarsplittingRats()));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardCostChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(rats.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Earsplitting Rats");
    }

    @Test
    @DisplayName("The regeneration shield saves it from lethal combat damage")
    void regenerationShieldSavesFromLethalCombatDamage() {
        Permanent rats = addCreatureReady(player1, new EarsplittingRats());
        harness.setHand(player1, List.of(new EarsplittingRats()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        rats.setBlocking(true);
        rats.addBlockingTarget(0);
        Permanent attacker = addCreatureReady(player2, new EarsplittingRats());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Earsplitting Rats");
        assertThat(rats.isTapped()).isTrue();
        assertThat(rats.isBlocking()).isFalse();
        assertThat(rats.getMarkedDamage()).isZero();
        assertThat(rats.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("The regeneration ability cannot be activated without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addCreatureReady(player1, new EarsplittingRats());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("discard a card");
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscardJudReview() {
        Permanent rats = harness.addToBattlefieldAndReturn(player1, new EarsplittingRats());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(rats.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Regeneration shield saves it from lethal damage")
    void regenerationShieldSavesFromLethalDamage() {
        Permanent rats = harness.addToBattlefieldAndReturn(player1, new EarsplittingRats());
        harness.setHand(player1, List.of(new GrizzlyBears(), new Shock()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, rats.getId());

        harness.assertOnBattlefield(player1, "Earsplitting Rats");
        assertThat(rats.getRegenerationShield()).isZero();
        assertThat(rats.isTapped()).isTrue();
        assertThat(rats.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Shock");
    }
}
