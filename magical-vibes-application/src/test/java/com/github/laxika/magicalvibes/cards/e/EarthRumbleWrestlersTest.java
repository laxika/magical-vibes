package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TreetopVillage;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EarthRumbleWrestlers.class, Forest.class, TreetopVillage.class})
class EarthRumbleWrestlersTest extends BaseCardTest {

    @Test
    @DisplayName("Has base power and toughness without the condition")
    void hasBaseStatsWithoutCondition() {
        Permanent wrestlers = harness.addToBattlefieldAndReturn(player1, new EarthRumbleWrestlers());

        assertThat(wrestlers.getEffectivePower()).isEqualTo(3);
        assertThat(wrestlers.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, wrestlers, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+0 and trample after a land enters under your control")
    void getsBoostAfterLandEntersUnderYourControl() {
        Permanent wrestlers = harness.addToBattlefieldAndReturn(player1, new EarthRumbleWrestlers());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gqs.getEffectivePower(gd, wrestlers)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, wrestlers)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, wrestlers, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The landfall condition expires at the end of the turn")
    void landfallConditionExpiresAtEndOfTurn() {
        Permanent wrestlers = harness.addToBattlefieldAndReturn(player1, new EarthRumbleWrestlers());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wrestlers.getEffectivePower()).isEqualTo(3);
        assertThat(wrestlers.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, wrestlers, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A land creature also enables the boost")
    void getsBoostWhileControllingLandCreature() {
        Permanent village = addReadyVillage(player1);
        Permanent wrestlers = harness.addToBattlefieldAndReturn(player1, new EarthRumbleWrestlers());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(village.getEffectivePower()).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, wrestlers)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, wrestlers)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, wrestlers, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's land does not enable the boost")
    void opponentLandDoesNotEnableBoost() {
        Permanent wrestlers = harness.addToBattlefieldAndReturn(player1, new EarthRumbleWrestlers());
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);

        harness.playLand(player2, 0);

        assertThat(wrestlers.getEffectivePower()).isEqualTo(3);
        assertThat(wrestlers.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, wrestlers, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addReadyVillage(Player player) {
        Permanent village = new Permanent(new TreetopVillage());
        village.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(village);
        return village;
    }
}
