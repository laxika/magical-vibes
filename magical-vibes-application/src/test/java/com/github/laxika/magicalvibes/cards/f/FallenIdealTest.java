package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FallenIdeal.class, GrizzlyBears.class, FountainOfYouth.class})
class FallenIdealTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has flying")
    void grantsFlying() {
        Permanent bears = attachAuraToBears();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature can sacrifice a creature to get +2/+1")
    void grantedAbilityBoostsEnchantedCreature() {
        Permanent bears = attachAuraToBears();
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The granted pump wears off at end of turn")
    void grantedPumpWearsOffAtEndOfTurn() {
        Permanent bears = attachAuraToBears();
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Fallen Ideal returns to its owner's hand from the graveyard")
    void returnsToOwnersHandWhenPutIntoGraveyard() {
        attachAuraToBears();
        Permanent aura = findPermanent(player1, "Fallen Ideal");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Fallen Ideal");
        harness.assertNotInGraveyard(player1, "Fallen Ideal");
        harness.assertNotOnBattlefield(player1, "Fallen Ideal");
    }

    @Test
    @DisplayName("Fallen Ideal cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1,
                new FountainOfYouth());
        harness.setHand(player1, List.of(new FallenIdeal()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent attachAuraToBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1,
                new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FallenIdeal());
        aura.setAttachedTo(bears.getId());
        return bears;
    }
}
