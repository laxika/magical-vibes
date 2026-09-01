package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(JodahsAvenger.class)
class JodahsAvengerTest extends BaseCardTest {

    @Test
    void canChooseEachAbilityMode() {
        Permanent avenger = addAvengerReady(player1);

        activate(avenger, "Double strike");
        assertThat(gqs.hasKeyword(gd, avenger, Keyword.DOUBLE_STRIKE)).isTrue();

        activate(avenger, "Protection from red");
        assertThat(gqs.hasProtectionFrom(gd, avenger, CardColor.RED)).isTrue();

        activate(avenger, "Vigilance");
        assertThat(gqs.hasKeyword(gd, avenger, Keyword.VIGILANCE)).isTrue();

        activate(avenger, "Shadow");
        assertThat(gqs.hasKeyword(gd, avenger, Keyword.SHADOW)).isTrue();
    }

    @Test
    void getsMinusOneMinusOneAndChosenAbilityUntilEndOfTurn() {
        Permanent avenger = addAvengerReady(player1);
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

    private Permanent addAvengerReady(Player player) {
        Permanent permanent = new Permanent(new JodahsAvenger());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void activate(Permanent avenger, String mode) {
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(avenger), 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
    }
}
