package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScornfulAetherLich.class})
class ScornfulAetherLichTest extends BaseCardTest {

    @Test
    @DisplayName("{W}{B} ability grants both fear and vigilance")
    void abilityGrantsFearAndVigilance() {
        Permanent lich = addCreatureReady(player1, new ScornfulAetherLich());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThat(gqs.hasKeyword(gd, lich, Keyword.FEAR)).isFalse();
        assertThat(gqs.hasKeyword(gd, lich, Keyword.VIGILANCE)).isFalse();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, lich, Keyword.FEAR)).isTrue();
        assertThat(gqs.hasKeyword(gd, lich, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Granted fear and vigilance wear off at end of turn")
    void grantedKeywordsWearOff() {
        Permanent lich = addCreatureReady(player1, new ScornfulAetherLich());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, lich, Keyword.FEAR)).isTrue();
        assertThat(gqs.hasKeyword(gd, lich, Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, lich, Keyword.FEAR)).isFalse();
        assertThat(gqs.hasKeyword(gd, lich, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Ability can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent lich = harness.addToBattlefieldAndReturn(player1, new ScornfulAetherLich());
        lich.setSummoningSick(true);
        lich.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, lich, Keyword.FEAR)).isTrue();
        assertThat(gqs.hasKeyword(gd, lich, Keyword.VIGILANCE)).isTrue();
        assertThat(lich.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability grants keywords only to its source after resolution")
    void grantsOnlyToSourceAfterResolution() {
        Permanent lich = addCreatureReady(player1, new ScornfulAetherLich());
        Permanent other = addCreatureReady(player1, new ScornfulAetherLich());
        Permanent opposing = addCreatureReady(player2, new ScornfulAetherLich());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.hasKeyword(gd, lich, Keyword.FEAR)).isFalse();
        assertThat(gqs.hasKeyword(gd, lich, Keyword.VIGILANCE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, lich, Keyword.FEAR)).isTrue();
        assertThat(gqs.hasKeyword(gd, lich, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FEAR)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposing, Keyword.FEAR)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposing, Keyword.VIGILANCE)).isFalse();
    }
}
