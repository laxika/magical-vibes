package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrownOfSuspicion.class, GrizzlyBears.class, RagingGoblin.class, SuntailHawk.class})
class CrownOfSuspicionTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/-1")
    void enchantedCreatureGetsBoost() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachCrown(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing the Aura boosts the enchanted creature and creatures sharing its type")
    void sacrificeBoostsEnchantedAndSharingCreatures() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherBears = addCreatureReady(player2, new GrizzlyBears());
        Permanent goblin = addCreatureReady(player1, new RagingGoblin());
        Permanent hawk = addCreatureReady(player2, new SuntailHawk());
        Permanent crown = attachCrown(bears);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(crown), null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, otherBears)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(crown);
    }

    @Test
    @DisplayName("Sacrifice boost wears off at end of turn")
    void sacrificeBoostWearsOffAtEndOfTurn() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherBears = addCreatureReady(player2, new GrizzlyBears());
        Permanent crown = attachCrown(bears);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(crown), null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherBears)).isEqualTo(2);
    }

    private Permanent attachCrown(Permanent host) {
        Permanent crown = new Permanent(new CrownOfSuspicion());
        crown.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player1.getId()).add(crown);
        return crown;
    }
}
