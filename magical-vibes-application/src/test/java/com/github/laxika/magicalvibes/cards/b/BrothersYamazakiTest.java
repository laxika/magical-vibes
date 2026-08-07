package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BrothersYamazakiTest extends BaseCardTest {

    @Test
    @DisplayName("A lone copy boosts nothing and keeps its printed stats")
    void loneCopyIsUnboosted() {
        Permanent brother = harness.addToBattlefieldAndReturn(player1, new BrothersYamazaki());

        assertThat(gqs.getEffectivePower(gd, brother)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, brother)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each other Brothers Yamazaki gets +2/+2 and haste")
    void eachOtherCopyGetsBoostAndHaste() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BrothersYamazaki());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BrothersYamazaki());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, first, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The boost also reaches a copy an opponent controls")
    void boostsOpponentCopy() {
        harness.addToBattlefieldAndReturn(player1, new BrothersYamazaki());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new BrothersYamazaki());

        assertThat(gqs.getEffectivePower(gd, theirs)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, theirs, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Exactly two under one controller survive the legend rule")
    void exactlyTwoSurviveLegendRule() {
        harness.addToBattlefieldAndReturn(player1, new BrothersYamazaki());
        harness.addToBattlefieldAndReturn(player1, new BrothersYamazaki());

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.permanentChoiceContext()).isNull();
    }

    @Test
    @DisplayName("A third copy on the battlefield brings the legend rule back")
    void thirdCopyRestoresLegendRule() {
        harness.addToBattlefieldAndReturn(player1, new BrothersYamazaki());
        harness.addToBattlefieldAndReturn(player1, new BrothersYamazaki());
        harness.addToBattlefieldAndReturn(player2, new BrothersYamazaki());

        harness.runStateBasedActions();

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.LegendRule.class);
    }
}
