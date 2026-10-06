package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RelentlessHunter.class})
class RelentlessHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability gives Relentless Hunter +1/+1 and trample")
    void activatedAbilityBoostsSelfAndGrantsTrample() {
        Permanent hunter = addReadyHunter(player1);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hunter)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hunter)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, hunter, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The activated ability's boost and trample wear off at end of turn")
    void activatedAbilityWearsOffAtEndOfTurn() {
        Permanent hunter = addReadyHunter(player1);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hunter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hunter)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, hunter, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Repeated activations stack their boosts until end of turn")
    void repeatedActivationsStack() {
        Permanent hunter = addReadyHunter(player1);
        addActivationMana(player1);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, hunter)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, hunter)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, hunter, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Hunter can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent hunter = addReadyHunter(player1);
        hunter.setSummoningSick(true);
        hunter.setTapped(true);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hunter)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hunter)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, hunter, Keyword.TRAMPLE)).isTrue();
        assertThat(hunter.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability affects only its source, including among Hunters with the same name")
    void affectsOnlyActivatingHunter() {
        Permanent hunter = addReadyHunter(player1);
        Permanent otherHunter = harness.addToBattlefieldAndReturn(player1, new RelentlessHunter());
        Permanent opposingHunter = harness.addToBattlefieldAndReturn(player2, new RelentlessHunter());
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hunter)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hunter)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, hunter, Keyword.TRAMPLE)).isTrue();
        for (Permanent unaffected : new Permanent[]{otherHunter, opposingHunter}) {
            assertThat(gqs.getEffectivePower(gd, unaffected)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, unaffected)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, unaffected, Keyword.TRAMPLE)).isFalse();
        }
    }

    private void addActivationMana(Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }

    private Permanent addReadyHunter(Player player) {
        Permanent hunter = addCreatureReady(player, new RelentlessHunter());
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return hunter;
    }
}
