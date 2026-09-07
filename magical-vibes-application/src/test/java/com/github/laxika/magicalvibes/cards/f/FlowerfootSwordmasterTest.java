package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlowerfootSwordmaster.class, GiantGrowth.class, GrizzlyBears.class})
class FlowerfootSwordmasterTest extends BaseCardTest {

    @Test
    void offspringCreatesOneOneTokenCopyWhenPaid() {
        harness.setHand(player1, List.of(new FlowerfootSwordmaster()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getEffectivePower()).isEqualTo(1);
        assertThat(tokens.getFirst().getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void valiantBoostsMiceOnlyOnceWhenTargetedByYourSpellsEachTurn() {
        Permanent swordmaster = harness.addToBattlefieldAndReturn(player1, new FlowerfootSwordmaster());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, swordmaster.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        int powerAfterFirstSpell = swordmaster.getEffectivePower();

        assertThat(powerAfterFirstSpell).isEqualTo(5);
        assertThat(bear.getEffectivePower()).isEqualTo(2);

        harness.castInstant(player1, 0, swordmaster.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(swordmaster.getEffectivePower()).isEqualTo(powerAfterFirstSpell + 3);
    }

    @Test
    void valiantDoesNotTriggerForAnOpponentsSpell() {
        Permanent swordmaster = harness.addToBattlefieldAndReturn(player1, new FlowerfootSwordmaster());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, swordmaster.getId());
        harness.passBothPriorities();

        assertThat(swordmaster.getEffectivePower()).isEqualTo(4);
    }
}
