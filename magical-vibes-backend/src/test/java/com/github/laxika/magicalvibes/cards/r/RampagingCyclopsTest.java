package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BlockedByMinCreaturesConditionalEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RampagingCyclopsTest extends BaseCardTest {

    @Test
    @DisplayName("Rampaging Cyclops has BlockedByMinCreaturesConditionalEffect with minBlockers=2")
    void hasCorrectEffect() {
        RampagingCyclops card = new RampagingCyclops();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .singleElement()
                .isInstanceOfSatisfying(BlockedByMinCreaturesConditionalEffect.class,
                        e -> assertThat(e.minBlockers()).isEqualTo(2));
    }

    @Test
    @DisplayName("Rampaging Cyclops has full power when not blocked")
    void fullPowerWhenNotBlocked() {
        Permanent cyclops = addToBattlefield(player1, new RampagingCyclops());

        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cyclops)).isEqualTo(4);
    }

    @Test
    @DisplayName("Rampaging Cyclops has full power when blocked by one creature")
    void fullPowerWhenBlockedByOne() {
        Permanent cyclops = addToBattlefield(player1, new RampagingCyclops());
        cyclops.setSummoningSick(false);
        cyclops.setAttacking(true);

        Permanent blocker = addToBattlefield(player2, new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(gd.playerBattlefields.get(player1.getId()).indexOf(cyclops));
        blocker.addBlockingTargetId(cyclops.getId());

        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cyclops)).isEqualTo(4);
    }

    @Test
    @DisplayName("Rampaging Cyclops gets -2/-0 when blocked by two creatures")
    void reducedPowerWhenBlockedByTwo() {
        Permanent cyclops = addToBattlefield(player1, new RampagingCyclops());
        cyclops.setSummoningSick(false);
        cyclops.setAttacking(true);

        int cyclopsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(cyclops);

        Permanent blocker1 = addToBattlefield(player2, new GrizzlyBears());
        blocker1.setSummoningSick(false);
        blocker1.setBlocking(true);
        blocker1.addBlockingTarget(cyclopsIndex);
        blocker1.addBlockingTargetId(cyclops.getId());

        Permanent blocker2 = addToBattlefield(player2, new GrizzlyBears());
        blocker2.setSummoningSick(false);
        blocker2.setBlocking(true);
        blocker2.addBlockingTarget(cyclopsIndex);
        blocker2.addBlockingTargetId(cyclops.getId());

        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cyclops)).isEqualTo(4);
    }

    @Test
    @DisplayName("Rampaging Cyclops gets -2/-0 when blocked by three creatures")
    void reducedPowerWhenBlockedByThree() {
        Permanent cyclops = addToBattlefield(player1, new RampagingCyclops());
        cyclops.setSummoningSick(false);
        cyclops.setAttacking(true);

        int cyclopsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(cyclops);

        for (int i = 0; i < 3; i++) {
            Permanent blocker = addToBattlefield(player2, new GrizzlyBears());
            blocker.setSummoningSick(false);
            blocker.setBlocking(true);
            blocker.addBlockingTarget(cyclopsIndex);
            blocker.addBlockingTargetId(cyclops.getId());
        }

        // Still -2/-0, not -6/-0 — the penalty is a flat -2, not per-blocker
        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cyclops)).isEqualTo(4);
    }

    private Permanent addToBattlefield(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
