package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EarnestFellowship;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Thaumatog.class, Forest.class, EarnestFellowship.class})
class ThaumatogTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a land gives Thaumatog +1/+1 until end of turn")
    void sacrificingLandBoostsSelf() {
        Permanent thaumatog = addCreatureReady(player1, new Thaumatog());
        harness.addToBattlefieldAndReturn(player1, new Forest());
        int powerBefore = gqs.getEffectivePower(gd, thaumatog);
        int toughnessBefore = gqs.getEffectiveToughness(gd, thaumatog);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, thaumatog)).isEqualTo(powerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, thaumatog)).isEqualTo(toughnessBefore + 1);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Sacrificing an enchantment gives Thaumatog +1/+1 until end of turn")
    void sacrificingEnchantmentBoostsSelf() {
        Permanent thaumatog = addCreatureReady(player1, new Thaumatog());
        harness.addToBattlefieldAndReturn(player1, new EarnestFellowship());
        int powerBefore = gqs.getEffectivePower(gd, thaumatog);
        int toughnessBefore = gqs.getEffectiveToughness(gd, thaumatog);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, thaumatog)).isEqualTo(powerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, thaumatog)).isEqualTo(toughnessBefore + 1);
        harness.assertInGraveyard(player1, "Earnest Fellowship");
    }

    @Test
    @DisplayName("Each ability requires its matching permanent type")
    void requiresMatchingPermanentType() {
        addCreatureReady(player1, new Thaumatog());
        harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The boost wears off at cleanup")
    void boostsWearOffAtCleanup() {
        Permanent thaumatog = addCreatureReady(player1, new Thaumatog());
        harness.addToBattlefieldAndReturn(player1, new Forest());
        int powerBefore = gqs.getEffectivePower(gd, thaumatog);
        int toughnessBefore = gqs.getEffectiveToughness(gd, thaumatog);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, thaumatog)).isEqualTo(powerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, thaumatog)).isEqualTo(toughnessBefore + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, thaumatog)).isEqualTo(powerBefore);
        assertThat(gqs.getEffectiveToughness(gd, thaumatog)).isEqualTo(toughnessBefore);
    }

    @Test
    @DisplayName("Each ability lets you choose among matching permanents")
    void choosesMatchingPermanents() {
        Permanent thaumatog = addCreatureReady(player1, new Thaumatog());
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent firstEnchantment = harness.addToBattlefieldAndReturn(player1, new EarnestFellowship());
        Permanent secondEnchantment = harness.addToBattlefieldAndReturn(player1, new EarnestFellowship());
        int powerBefore = gqs.getEffectivePower(gd, thaumatog);
        int toughnessBefore = gqs.getEffectiveToughness(gd, thaumatog);

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.PermanentChoice landChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(landChoice).isNotNull();
        assertThat(landChoice.validIds()).containsExactlyInAnyOrder(firstLand.getId(), secondLand.getId());

        harness.handlePermanentChosen(player1, secondLand.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, null, null);

        PendingInteraction.PermanentChoice enchantmentChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(enchantmentChoice).isNotNull();
        assertThat(enchantmentChoice.validIds())
                .containsExactlyInAnyOrder(firstEnchantment.getId(), secondEnchantment.getId());

        harness.handlePermanentChosen(player1, secondEnchantment.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(firstLand, firstEnchantment)
                .doesNotContain(secondLand, secondEnchantment);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(secondLand.getCard(), secondEnchantment.getCard());
        assertThat(gqs.getEffectivePower(gd, thaumatog)).isEqualTo(powerBefore + 2);
        assertThat(gqs.getEffectiveToughness(gd, thaumatog)).isEqualTo(toughnessBefore + 2);
    }

    @Test
    @DisplayName("Neither ability can sacrifice a permanent controlled by an opponent")
    void cannotSacrificeOpponentsPermanents() {
        addCreatureReady(player1, new Thaumatog());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new EarnestFellowship());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player2, "Earnest Fellowship");
    }
}
