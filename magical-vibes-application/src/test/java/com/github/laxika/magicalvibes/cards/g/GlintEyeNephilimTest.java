package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlintEyeNephilim.class})
class GlintEyeNephilimTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage draws that many cards")
    void combatDamageDrawsEqualToDamageDealt() {
        Card firstDraw = new GlintEyeNephilim();
        Card secondDraw = new GlintEyeNephilim();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        addReadyNephilim();

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Combat damage draws the boosted damage amount")
    void combatDamageDrawsBoostedDamageAmount() {
        Permanent nephilim = addReadyNephilim();
        Card discarded = new GlintEyeNephilim();
        activatePump(discarded);

        Card firstDraw = new GlintEyeNephilim();
        Card secondDraw = new GlintEyeNephilim();
        Card thirdDraw = new GlintEyeNephilim();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, thirdDraw));

        assertThat(gqs.getEffectivePower(gd, nephilim)).isEqualTo(3);
        declareAttackers(List.of(0));
        resolveCombat();
        harness.assertLife(player2, 17);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw, thirdDraw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Discarding a card gives Glint-Eye Nephilim +1/+1 until end of turn")
    void discardAbilityBoostsUntilEndOfTurn() {
        Permanent nephilim = addReadyNephilim();
        Card discarded = new GlintEyeNephilim();
        activatePump(discarded);

        assertThat(gqs.getEffectivePower(gd, nephilim)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, nephilim)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, nephilim)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nephilim)).isEqualTo(2);
    }

    private Permanent addReadyNephilim() {
        return addCreatureReady(player1, new GlintEyeNephilim());
    }

    @Test
    @DisplayName("Combat damage to a creature does not draw cards")
    void blockedCombatDoesNotDrawCards() {
        Card libraryCard = new GlintEyeNephilim();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(libraryCard));
        addReadyNephilim();
        addCreatureReady(player2, new GlintEyeNephilim());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("Discard is paid before the pump resolves, even while summoning sick")
    void discardIsPaidBeforeResolution() {
        Permanent nephilim = harness.addToBattlefieldAndReturn(player1, new GlintEyeNephilim());
        Card discarded = new GlintEyeNephilim();
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gqs.getEffectivePower(gd, nephilim)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nephilim)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, nephilim)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, nephilim)).isEqualTo(3);
    }

    @Test
    @DisplayName("Pumping after combat damage does not change the pending draw amount")
    void drawAmountUsesDamageAlreadyDealt() {
        Permanent nephilim = addReadyNephilim();
        Card discarded = new GlintEyeNephilim();
        Card firstDraw = new GlintEyeNephilim();
        Card secondDraw = new GlintEyeNephilim();
        Card remaining = new GlintEyeNephilim();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, remaining));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.assertLife(player2, 18);
        activatePump(discarded);
        assertThat(gqs.getEffectivePower(gd, nephilim)).isEqualTo(3);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
    }

    private void activatePump(Card discarded) {
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
    }
}
