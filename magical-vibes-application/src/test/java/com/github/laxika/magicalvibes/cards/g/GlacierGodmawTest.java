package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlacierGodmaw.class, Forest.class, GrizzlyBears.class})
class GlacierGodmawTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Lander token when Glacier Godmaw enters")
    void createsLanderOnEnter() {
        harness.enterBattlefieldAndReturn(player1, new GlacierGodmaw());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Lander")).hasSize(1);
    }

    @Test
    @DisplayName("Landfall boosts all creatures you control and grants vigilance and haste until end of turn")
    void landfallBoostsOwnCreaturesUntilEndOfTurn() {
        Permanent godmaw = harness.addToBattlefieldAndReturn(player1, new GlacierGodmaw());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, godmaw)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, godmaw)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, godmaw, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, godmaw, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, godmaw)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, godmaw)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, godmaw, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, godmaw, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isFalse();
    }
}
