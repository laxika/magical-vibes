package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrownOfFury.class, GrizzlyBears.class, RagingGoblin.class, SuntailHawk.class})
class CrownOfFuryTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+0 and has first strike")
    void enchantedCreatureGetsBoostAndFirstStrike() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachCrown(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Sacrificing the Aura boosts and grants first strike to sharing creatures")
    void sacrificeAffectsEnchantedAndSharingCreatures() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherBears = addCreatureReady(player2, new GrizzlyBears());
        Permanent goblin = addCreatureReady(player1, new RagingGoblin());
        Permanent hawk = addCreatureReady(player2, new SuntailHawk());
        Permanent crown = attachCrown(bears);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(crown), null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, hawk, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(crown);
    }

    @Test
    @DisplayName("Sacrifice boost and first strike wear off at end of turn")
    void sacrificeEffectsWearOffAtEndOfTurn() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherBears = addCreatureReady(player2, new GrizzlyBears());
        Permanent crown = attachCrown(bears);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(crown), null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.FIRST_STRIKE)).isFalse();
    }

    private Permanent attachCrown(Permanent host) {
        Permanent crown = new Permanent(new CrownOfFury());
        crown.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player1.getId()).add(crown);
        return crown;
    }
}
