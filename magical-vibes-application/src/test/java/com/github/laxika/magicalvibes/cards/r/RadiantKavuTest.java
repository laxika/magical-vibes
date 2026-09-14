package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CavernHarpy;
import com.github.laxika.magicalvibes.cards.m.MaggotCarrier;
import com.github.laxika.magicalvibes.cards.p.PygmyKavu;
import com.github.laxika.magicalvibes.cards.s.StormscapeFamiliar;
import com.github.laxika.magicalvibes.cards.s.Stratadon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RadiantKavu.class, CavernHarpy.class, MaggotCarrier.class, PygmyKavu.class,
        StormscapeFamiliar.class, Stratadon.class})
class RadiantKavuTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability prevents blue and black creatures from dealing combat damage")
    void preventsBlueAndBlackCombatDamage() {
        addCreatureReady(player1, new RadiantKavu());
        Permanent blue = addCreatureReady(player2, new StormscapeFamiliar());
        Permanent black = addCreatureReady(player2, new MaggotCarrier());
        Permanent blueAndBlack = addCreatureReady(player2, new CavernHarpy());

        activateAbility();

        assertThat(gqs.isPreventedFromDealingDamage(gd, blue, true)).isTrue();
        assertThat(gqs.isPreventedFromDealingDamage(gd, black, true)).isTrue();
        assertThat(gqs.isPreventedFromDealingDamage(gd, blueAndBlack, true)).isTrue();
    }

    @Test
    @DisplayName("Activated ability does not prevent other creatures from dealing combat damage")
    void allowsOtherCreatureColorsToDealCombatDamage() {
        addCreatureReady(player1, new RadiantKavu());
        Permanent green = addCreatureReady(player2, new PygmyKavu());
        Permanent colorless = addCreatureReady(player2, new Stratadon());

        activateAbility();

        assertThat(gqs.isPreventedFromDealingDamage(gd, green, true)).isFalse();
        assertThat(gqs.isPreventedFromDealingDamage(gd, colorless, true)).isFalse();
    }

    @Test
    @DisplayName("Activated ability only prevents combat damage")
    void doesNotPreventNoncombatDamage() {
        addCreatureReady(player1, new RadiantKavu());
        Permanent black = addCreatureReady(player2, new MaggotCarrier());

        activateAbility();

        assertThat(gqs.isPreventedFromDealingDamage(gd, black, false)).isFalse();
    }

    @Test
    @DisplayName("Prevention expires at the end of the turn")
    void preventionExpiresAtEndOfTurn() {
        addCreatureReady(player1, new RadiantKavu());
        Permanent black = addCreatureReady(player2, new MaggotCarrier());

        activateAbility();
        assertThat(gqs.isPreventedFromDealingDamage(gd, black, true)).isTrue();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isPreventedFromDealingDamage(gd, black, true)).isFalse();
    }

    private void activateAbility() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
    }

}
