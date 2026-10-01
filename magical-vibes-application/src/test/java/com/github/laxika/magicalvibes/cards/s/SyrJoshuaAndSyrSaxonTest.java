package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SyrJoshuaAndSyrSaxon.class, GrizzlyBears.class})
class SyrJoshuaAndSyrSaxonTest extends BaseCardTest {

    @Test
    @DisplayName("A lone copy does not grant Battle Cry to itself")
    void loneCopyDoesNotGrantBattleCry() {
        Permanent syr = harness.addToBattlefieldAndReturn(player1, new SyrJoshuaAndSyrSaxon());

        assertThat(gqs.hasKeyword(gd, syr, Keyword.BATTLE_CRY)).isFalse();
    }

    @Test
    @DisplayName("Each other copy under the same controller has Battle Cry")
    void otherCopyGainsBattleCry() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SyrJoshuaAndSyrSaxon());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SyrJoshuaAndSyrSaxon());

        assertThat(gqs.hasKeyword(gd, first, Keyword.BATTLE_CRY)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.BATTLE_CRY)).isTrue();
    }

    @Test
    @DisplayName("A copy controlled by an opponent does not grant Battle Cry across controllers")
    void doesNotGrantBattleCryAcrossControllers() {
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new SyrJoshuaAndSyrSaxon());
        Permanent ours = harness.addToBattlefieldAndReturn(player1, new SyrJoshuaAndSyrSaxon());

        assertThat(gqs.hasKeyword(gd, theirs, Keyword.BATTLE_CRY)).isFalse();
        assertThat(gqs.hasKeyword(gd, ours, Keyword.BATTLE_CRY)).isFalse();
    }

    @Test
    @DisplayName("Exactly two copies under one controller survive the legend rule")
    void exactlyTwoCopiesSurviveLegendRule() {
        harness.addToBattlefieldAndReturn(player1, new SyrJoshuaAndSyrSaxon());
        harness.addToBattlefieldAndReturn(player1, new SyrJoshuaAndSyrSaxon());

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.permanentChoiceContext()).isNull();
    }

    @Test
    @DisplayName("A third copy under one controller restores the legend rule")
    void thirdCopyRestoresLegendRule() {
        harness.addToBattlefieldAndReturn(player1, new SyrJoshuaAndSyrSaxon());
        harness.addToBattlefieldAndReturn(player1, new SyrJoshuaAndSyrSaxon());
        harness.addToBattlefieldAndReturn(player1, new SyrJoshuaAndSyrSaxon());

        harness.runStateBasedActions();

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.LegendRule.class);
    }
}
