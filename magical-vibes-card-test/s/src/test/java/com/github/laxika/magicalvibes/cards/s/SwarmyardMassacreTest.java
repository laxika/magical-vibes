package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwarmyardMassacre.class, ColossalDreadmaw.class, GiantSpider.class, GrizzlyBears.class})
class SwarmyardMassacreTest extends BaseCardTest {

    @Test
    @DisplayName("Creates Squirrels and weakens only non-Insect, non-Rat, non-Spider, non-Squirrel creatures")
    void createsSquirrelsAndScalesDebuffFromControlledTypes() {
        Permanent ownSpider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        Permanent opposingSpider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent opposingDreadmaw = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSwarmyardMassacre();

        assertThat(findPermanents(player1, "Squirrel")).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, ownSpider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownSpider)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingSpider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingSpider)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingDreadmaw)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingDreadmaw)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(opposingBears.getId()));
    }

    @Test
    @DisplayName("The scalable debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent opposingDreadmaw = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());

        castSwarmyardMassacre();

        assertThat(gqs.getEffectivePower(gd, opposingDreadmaw)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opposingDreadmaw)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opposingDreadmaw)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, opposingDreadmaw)).isEqualTo(6);
    }

    private void castSwarmyardMassacre() {
        harness.setHand(player1, List.of(new SwarmyardMassacre()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
