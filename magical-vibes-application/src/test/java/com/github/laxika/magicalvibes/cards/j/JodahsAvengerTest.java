package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JodahsAvenger.class})
class JodahsAvengerTest extends BaseCardTest {

    @Test
    void canChooseEachAbilityMode() {
        Permanent doubleStrikeAvenger = addCreatureReady(player1, new JodahsAvenger());

        activate(doubleStrikeAvenger, "Double strike");
        assertThat(gqs.hasKeyword(gd, doubleStrikeAvenger, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, doubleStrikeAvenger, CardColor.RED)).isFalse();
        assertThat(gqs.hasKeyword(gd, doubleStrikeAvenger, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, doubleStrikeAvenger, Keyword.SHADOW)).isFalse();

        Permanent protectionAvenger = addCreatureReady(player1, new JodahsAvenger());

        activate(protectionAvenger, "Protection from red");
        assertThat(gqs.hasProtectionFrom(gd, protectionAvenger, CardColor.RED)).isTrue();
        assertThat(gqs.hasKeyword(gd, protectionAvenger, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, protectionAvenger, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, protectionAvenger, Keyword.SHADOW)).isFalse();

        Permanent vigilanceAvenger = addCreatureReady(player1, new JodahsAvenger());

        activate(vigilanceAvenger, "Vigilance");
        assertThat(gqs.hasKeyword(gd, vigilanceAvenger, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, vigilanceAvenger, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, vigilanceAvenger, CardColor.RED)).isFalse();
        assertThat(gqs.hasKeyword(gd, vigilanceAvenger, Keyword.SHADOW)).isFalse();

        Permanent shadowAvenger = addCreatureReady(player1, new JodahsAvenger());

        activate(shadowAvenger, "Shadow");
        assertThat(gqs.hasKeyword(gd, shadowAvenger, Keyword.SHADOW)).isTrue();
        assertThat(gqs.hasKeyword(gd, shadowAvenger, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, shadowAvenger, CardColor.RED)).isFalse();
        assertThat(gqs.hasKeyword(gd, shadowAvenger, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void getsMinusOneMinusOneAndChosenAbilityUntilEndOfTurn() {
        Permanent avenger = addCreatureReady(player1, new JodahsAvenger());
        int power = gqs.getEffectivePower(gd, avenger);
        int toughness = gqs.getEffectiveToughness(gd, avenger);

        activate(avenger, "Vigilance");

        assertThat(gqs.getEffectivePower(gd, avenger)).isEqualTo(power - 1);
        assertThat(gqs.getEffectiveToughness(gd, avenger)).isEqualTo(toughness - 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, avenger)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, avenger)).isEqualTo(toughness);
        assertThat(gqs.hasKeyword(gd, avenger, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void protectionFromRedWearsOffAtEndOfTurn() {
        Permanent avenger = addCreatureReady(player1, new JodahsAvenger());
        int power = gqs.getEffectivePower(gd, avenger);
        int toughness = gqs.getEffectiveToughness(gd, avenger);

        activate(avenger, "Protection from red");

        assertThat(gqs.getEffectivePower(gd, avenger)).isEqualTo(power - 1);
        assertThat(gqs.getEffectiveToughness(gd, avenger)).isEqualTo(toughness - 1);
        assertThat(gqs.hasProtectionFrom(gd, avenger, CardColor.RED)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, avenger)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, avenger)).isEqualTo(toughness);
        assertThat(gqs.hasProtectionFrom(gd, avenger, CardColor.RED)).isFalse();
    }

    private void activate(Permanent avenger, String mode) {
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(avenger), 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
    }
}
