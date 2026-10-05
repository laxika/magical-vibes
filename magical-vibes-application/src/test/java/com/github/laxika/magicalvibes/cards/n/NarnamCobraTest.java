package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NarnamCobra.class})
class NarnamCobraTest extends BaseCardTest {

    @Test
    @DisplayName("{G}: this creature gains deathtouch until end of turn")
    void gainsDeathtouch() {
        Permanent cobra = addCreatureReady(player1, new NarnamCobra());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cobra, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Deathtouch wears off at end of turn")
    void deathtouchWearsOff() {
        Permanent cobra = addCreatureReady(player1, new NarnamCobra());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, cobra, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Cobra can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent cobra = addCreatureReady(player1, new NarnamCobra());
        cobra.setSummoningSick(true);
        cobra.setTapped(true);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cobra, Keyword.DEATHTOUCH)).isTrue();
        assertThat(cobra.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Deathtouch is granted on resolution only to the activating Cobra")
    void grantsOnlyToSourceOnResolution() {
        Permanent cobra = addCreatureReady(player1, new NarnamCobra());
        Permanent otherCobra = addCreatureReady(player1, new NarnamCobra());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, cobra, Keyword.DEATHTOUCH)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cobra, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherCobra, Keyword.DEATHTOUCH)).isFalse();
    }
}
