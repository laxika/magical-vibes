package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(WormwoodDryad.class)
class WormwoodDryadTest extends BaseCardTest {

    @Test
    @DisplayName("The green ability grants forestwalk and deals 1 damage to its controller")
    void greenAbilityGrantsForestwalkAndDealsDamage() {
        Permanent dryad = addReadyDryad();
        harness.addMana(player1, ManaColor.GREEN, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dryad, Keyword.FORESTWALK)).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("The black ability grants swampwalk and deals 1 damage to its controller")
    void blackAbilityGrantsSwampwalkAndDealsDamage() {
        Permanent dryad = addReadyDryad();
        harness.addMana(player1, ManaColor.BLACK, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dryad, Keyword.SWAMPWALK)).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("The granted landwalk ability wears off at end of turn")
    void grantedLandwalkWearsOffAtEndOfTurn() {
        Permanent dryad = addReadyDryad();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, dryad, Keyword.FORESTWALK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dryad, Keyword.FORESTWALK)).isFalse();
    }

    @ParameterizedTest
    @CsvSource({"0, GREEN, FORESTWALK", "1, BLACK, SWAMPWALK"})
    void abilityCanBeActivatedWhileSummoningSickAndTapped(int abilityIndex, ManaColor color, Keyword keyword) {
        Permanent dryad = harness.addToBattlefieldAndReturn(player1, new WormwoodDryad());
        dryad.setSummoningSick(true);
        dryad.tap();
        harness.addMana(player1, color, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, abilityIndex, null, null);

        assertThat(gqs.hasKeyword(gd, dryad, keyword)).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dryad, keyword)).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(dryad.isTapped()).isTrue();
    }

    @ParameterizedTest
    @CsvSource({"0, GREEN", "1, BLACK"})
    void damageStillHappensWhenSourceLeavesBeforeResolution(int abilityIndex, ManaColor color) {
        Permanent dryad = addReadyDryad();
        harness.addMana(player1, color, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, abilityIndex, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(dryad);
        gd.playerGraveyards.get(player1.getId()).add(dryad.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({"0, GREEN, FORESTWALK", "1, BLACK, SWAMPWALK"})
    void repeatedActivationDealsDamageAgainAndExpiresAtCleanup(int abilityIndex, ManaColor color, Keyword keyword) {
        Permanent dryad = addReadyDryad();
        harness.addMana(player1, color, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, abilityIndex, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, abilityIndex, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dryad, keyword)).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dryad, keyword)).isFalse();
    }

    private Permanent addReadyDryad() {
        return addCreatureReady(player1, new WormwoodDryad());
    }
}
