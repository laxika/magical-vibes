package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ErstwhileTrooperTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a creature card gives Erstwhile Trooper +2/+2 and trample")
    void discardCreatureBoostsAndGrantsTrample() {
        Permanent trooper = harness.addToBattlefieldAndReturn(player1, new ErstwhileTrooper());
        int basePower = gqs.getEffectivePower(gd, trooper);
        int baseToughness = gqs.getEffectiveToughness(gd, trooper);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, trooper)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, trooper)).isEqualTo(baseToughness + 2);
        assertThat(trooper.hasKeyword(Keyword.TRAMPLE)).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The temporary boost and trample wear off at end of turn")
    void effectWearsOffAtEndOfTurn() {
        Permanent trooper = harness.addToBattlefieldAndReturn(player1, new ErstwhileTrooper());
        int basePower = gqs.getEffectivePower(gd, trooper);
        int baseToughness = gqs.getEffectiveToughness(gd, trooper);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, trooper)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, trooper)).isEqualTo(baseToughness);
        assertThat(trooper.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The ability only allows a creature card to be discarded")
    void abilityRequiresCreatureCard() {
        harness.addToBattlefield(player1, new ErstwhileTrooper());
        harness.setHand(player1, List.of(new Mountain()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a creature card");
    }

    @Test
    @DisplayName("The ability can be activated only once each turn")
    void abilityCanBeActivatedOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new ErstwhileTrooper());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
