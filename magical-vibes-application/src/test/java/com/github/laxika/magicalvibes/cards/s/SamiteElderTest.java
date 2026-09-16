package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlphaKavu;
import com.github.laxika.magicalvibes.cards.c.CavernHarpy;
import com.github.laxika.magicalvibes.cards.l.LashknifeBarrier;
import com.github.laxika.magicalvibes.cards.m.ManaCylix;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SamiteElder.class, AlphaKavu.class, CavernHarpy.class, LashknifeBarrier.class, ManaCylix.class})
class SamiteElderTest extends BaseCardTest {

    @Test
    @DisplayName("Grants your creatures protection from the target permanent's colors")
    void grantsProtectionFromTargetPermanentColors() {
        Permanent elder = addCreatureReady(player1, new SamiteElder());
        Permanent kavu = addCreatureReady(player1, new AlphaKavu());
        Permanent opponentKavu = addCreatureReady(player2, new AlphaKavu());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(elder), null, kavu.getId());
        harness.passBothPriorities();

        Permanent lateKavu = addCreatureReady(player1, new AlphaKavu());

        assertThat(gqs.hasProtectionFrom(gd, elder, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, kavu, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, kavu, CardColor.RED)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, opponentKavu, CardColor.GREEN)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, lateKavu, CardColor.GREEN)).isFalse();
    }

    @Test
    @DisplayName("Grants protection from every color of a multicolored target permanent")
    void grantsProtectionFromAllColorsOfMulticoloredTarget() {
        Permanent elder = addCreatureReady(player1, new SamiteElder());
        Permanent kavu = addCreatureReady(player1, new AlphaKavu());
        Permanent harpy = addCreatureReady(player1, new CavernHarpy());
        Permanent opponentKavu = addCreatureReady(player2, new AlphaKavu());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(elder), null, harpy.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, elder, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, elder, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, elder, CardColor.WHITE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, kavu, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, kavu, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, harpy, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, opponentKavu, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("Can target a noncreature permanent but grants protection only to creatures")
    void grantsProtectionOnlyToCreatures() {
        Permanent elder = addCreatureReady(player1, new SamiteElder());
        Permanent kavu = addCreatureReady(player1, new AlphaKavu());
        Permanent barrier = harness.addToBattlefieldAndReturn(player1, new LashknifeBarrier());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(elder), null, barrier.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, elder, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, kavu, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, barrier, CardColor.WHITE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant protection when the target permanent has no colors")
    void grantsNoProtectionFromColorlessTarget() {
        Permanent elder = addCreatureReady(player1, new SamiteElder());
        Permanent kavu = addCreatureReady(player1, new AlphaKavu());
        Permanent cylix = harness.addToBattlefieldAndReturn(player1, new ManaCylix());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(elder), null, cylix.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, elder, CardColor.WHITE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, elder, CardColor.BLUE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, kavu, CardColor.WHITE)).isFalse();
    }

    @Test
    @DisplayName("Can target only a permanent you control")
    void cannotTargetOpponentPermanent() {
        Permanent elder = addCreatureReady(player1, new SamiteElder());
        Permanent opponentKavu = addCreatureReady(player2, new AlphaKavu());

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(elder),
                null,
                opponentKavu.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a permanent you control");
    }

    @Test
    @DisplayName("Protection expires at end of turn")
    void protectionExpiresAtEndOfTurn() {
        Permanent elder = addCreatureReady(player1, new SamiteElder());
        Permanent kavu = addCreatureReady(player1, new AlphaKavu());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(elder), null, kavu.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasProtectionFrom(gd, elder, CardColor.GREEN)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, elder, CardColor.GREEN)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, kavu, CardColor.GREEN)).isFalse();
    }
}
