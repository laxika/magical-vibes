package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KolaghanForerunners.class})
class KolaghanForerunnersTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of creatures its controller controls")
    void powerEqualsControlledCreatures() {
        Permanent forerunners = addForerunners(player1);

        assertThat(gqs.getEffectivePower(gd, forerunners)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, forerunners)).isEqualTo(3);

        Permanent otherForerunners = addForerunners(player1);
        addForerunners(player2);

        assertThat(gqs.getEffectivePower(gd, forerunners)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, otherForerunners)).isEqualTo(2);
    }

    @Test
    @DisplayName("Power updates when creatures leave its controller's battlefield")
    void powerUpdatesWhenCreaturesChange() {
        Permanent forerunners = addForerunners(player1);
        addForerunners(player1);

        assertThat(gqs.getEffectivePower(gd, forerunners)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent != forerunners);

        assertThat(gqs.getEffectivePower(gd, forerunners)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power follows the current controller's creature count after control changes")
    void powerUpdatesAfterControlChanges() {
        Permanent forerunners = addForerunners(player1);
        addForerunners(player2);
        addForerunners(player2);
        assertThat(gqs.getEffectivePower(gd, forerunners)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(forerunners);
        gd.playerBattlefields.get(player2.getId()).add(forerunners);

        assertThat(gqs.getEffectivePower(gd, forerunners)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, forerunners)).isEqualTo(3);
    }

    @Test
    @DisplayName("Dash grants haste and returns the creature to its owner's hand at end step")
    void dashGrantsHasteAndReturnsAtEndStep() {
        harness.setHand(player1, List.of(new KolaghanForerunners()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent forerunners = findPermanent(player1, "Kolaghan Forerunners");
        assertThat(forerunners.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Kolaghan Forerunners");
        harness.assertNotOnBattlefield(player1, "Kolaghan Forerunners");
    }

    @Test
    @DisplayName("Paying dash does not create an enter-the-battlefield trigger")
    void dashDoesNotTriggerOnEntry() {
        harness.setHand(player1, List.of(new KolaghanForerunners()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kolaghan Forerunners");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Dash returns the creature only when its end-step trigger resolves")
    void dashReturnUsesTheStackAtEndStep() {
        harness.setHand(player1, List.of(new KolaghanForerunners()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Kolaghan Forerunners");
        harness.assertNotInHand(player1, "Kolaghan Forerunners");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kolaghan Forerunners");
        harness.assertInHand(player1, "Kolaghan Forerunners");
    }

    @Test
    @DisplayName("A normally cast Forerunners has no dash haste or end-step return")
    void normalCastDoesNotApplyDash() {
        harness.setHand(player1, List.of(new KolaghanForerunners()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent forerunners = findPermanent(player1, "Kolaghan Forerunners");
        assertThat(gqs.hasKeyword(gd, forerunners, Keyword.HASTE)).isFalse();
        assertThat(forerunners.isSummoningSick()).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Kolaghan Forerunners");
        harness.assertNotInHand(player1, "Kolaghan Forerunners");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addForerunners(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new KolaghanForerunners());
    }
}
