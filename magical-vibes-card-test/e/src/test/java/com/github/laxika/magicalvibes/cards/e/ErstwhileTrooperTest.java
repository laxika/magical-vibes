package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DeadWeight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ErstwhileTrooper.class, DeadWeight.class})
class ErstwhileTrooperTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a creature card gives Erstwhile Trooper +2/+2 and trample")
    void discardCreatureBoostsAndGrantsTrample() {
        Permanent trooper = harness.addToBattlefieldAndReturn(player1, new ErstwhileTrooper());
        int basePower = gqs.getEffectivePower(gd, trooper);
        int baseToughness = gqs.getEffectiveToughness(gd, trooper);
        harness.setHand(player1, List.of(new ErstwhileTrooper()));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, trooper)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, trooper)).isEqualTo(baseToughness + 2);
        assertThat(trooper.hasKeyword(Keyword.TRAMPLE)).isTrue();
        harness.assertInGraveyard(player1, "Erstwhile Trooper");
    }

    @Test
    @DisplayName("The temporary boost and trample wear off at end of turn")
    void effectWearsOffAtEndOfTurn() {
        Permanent trooper = harness.addToBattlefieldAndReturn(player1, new ErstwhileTrooper());
        int basePower = gqs.getEffectivePower(gd, trooper);
        int baseToughness = gqs.getEffectiveToughness(gd, trooper);
        harness.setHand(player1, List.of(new ErstwhileTrooper()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, trooper)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, trooper)).isEqualTo(baseToughness);
        assertThat(trooper.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The ability only allows a creature card to be discarded")
    void abilityRequiresCreatureCard() {
        harness.addToBattlefield(player1, new ErstwhileTrooper());
        harness.setHand(player1, List.of(new DeadWeight()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a creature card");
    }

    @Test
    @DisplayName("The ability can be activated only once each turn")
    void abilityCanBeActivatedOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new ErstwhileTrooper());
        harness.setHand(player1, List.of(new ErstwhileTrooper(), new ErstwhileTrooper()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Discard is paid before resolution and the boost waits for resolution")
    void discardIsPaidBeforeAbilityResolves() {
        Permanent trooper = harness.addToBattlefieldAndReturn(player1, new ErstwhileTrooper());
        int basePower = gqs.getEffectivePower(gd, trooper);
        int baseToughness = gqs.getEffectiveToughness(gd, trooper);
        harness.setHand(player1, List.of(new ErstwhileTrooper(), new ErstwhileTrooper()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Erstwhile Trooper");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, trooper)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, trooper)).isEqualTo(baseToughness);
        assertThat(trooper.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, trooper)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, trooper)).isEqualTo(baseToughness + 2);
        assertThat(trooper.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The activation limit resets on the opponent's turn")
    void canActivateAgainOnOpponentsTurn() {
        Permanent trooper = harness.addToBattlefieldAndReturn(player1, new ErstwhileTrooper());
        int basePower = gqs.getEffectivePower(gd, trooper);
        int baseToughness = gqs.getEffectiveToughness(gd, trooper);
        harness.setHand(player1, List.of(new ErstwhileTrooper(), new ErstwhileTrooper()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, trooper)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, trooper)).isEqualTo(baseToughness + 2);
        assertThat(trooper.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A noncreature cannot be chosen from a hand containing a creature")
    void mixedHandOnlyAllowsCreatureDiscard() {
        Permanent trooper = harness.addToBattlefieldAndReturn(player1, new ErstwhileTrooper());
        int basePower = gqs.getEffectivePower(gd, trooper);
        harness.setHand(player1, List.of(new DeadWeight(), new ErstwhileTrooper()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
        harness.assertNotInGraveyard(player1, "Dead Weight");

        harness.handleCardChosen(player1, 1);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Dead Weight");
        harness.assertInGraveyard(player1, "Erstwhile Trooper");
        assertThat(gqs.getEffectivePower(gd, trooper)).isEqualTo(basePower + 2);
        assertThat(trooper.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Each Erstwhile Trooper has its own activation limit")
    void activationLimitIsPerPermanent() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ErstwhileTrooper());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ErstwhileTrooper());
        int basePower = gqs.getEffectivePower(gd, first);
        harness.setHand(player1, List.of(new ErstwhileTrooper(), new ErstwhileTrooper()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(basePower);
        assertThat(second.hasKeyword(Keyword.TRAMPLE)).isFalse();

        harness.activateAbility(player1, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(basePower + 2);
        assertThat(first.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(second.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }
}
