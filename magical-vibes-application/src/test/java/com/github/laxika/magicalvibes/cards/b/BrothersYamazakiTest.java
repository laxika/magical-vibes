package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrothersYamazaki.class, WanderingOnes.class})
class BrothersYamazakiTest extends BaseCardTest {

    @Test
    @DisplayName("A lone copy boosts nothing and keeps its printed stats")
    void loneCopyIsUnboosted() {
        Permanent brother = harness.addToBattlefieldAndReturn(player1, new BrothersYamazaki());

        assertThat(gqs.getEffectivePower(gd, brother)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, brother)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, brother, Keyword.HASTE)).isFalse();
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
    @DisplayName("The static ability does not affect an unrelated creature")
    void doesNotBoostUnrelatedCreature() {
        addCreatureReady(player1, new BrothersYamazaki());
        Permanent unrelated = addCreatureReady(player1, new WanderingOnes());

        assertThat(gqs.getEffectivePower(gd, unrelated)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, unrelated)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, unrelated, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Bushido 1 gives each brother +1/+1 when one blocks the other")
    void bothCopiesGetBushidoWhenTheyClash() {
        Permanent attacker = addCreatureReady(player1, new BrothersYamazaki());
        Permanent blocker = addCreatureReady(player2, new BrothersYamazaki());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(4);
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
