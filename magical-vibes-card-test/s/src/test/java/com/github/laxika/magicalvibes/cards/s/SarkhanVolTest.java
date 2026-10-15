package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.cards.o.ObeliskOfNaya;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SarkhanVol.class, CylianElf.class, ObeliskOfNaya.class,
        MycosynthLattice.class, MarchOfTheMachines.class})
class SarkhanVolTest extends BaseCardTest {

    @Test
    @DisplayName("+1 gives controlled creatures +1/+1 and haste until end of turn")
    void plusOneBoostsAndGrantsHaste() {
        Permanent sarkhan = addReadySarkhan(player1);
        Permanent bear = addCreatureReady(player1, new CylianElf());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isEqualTo(5); // 4 + 1
        assertThat(bear.getEffectivePower()).isEqualTo(3);
        assertThat(bear.getEffectiveToughness()).isEqualTo(3);
        assertThat(bear.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("+1 does not affect opponent's creatures")
    void plusOneDoesNotAffectOpponentCreatures() {
        addReadySarkhan(player1);
        Permanent oppBear = addCreatureReady(player2, new CylianElf());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(oppBear.getEffectivePower()).isEqualTo(2);
        assertThat(oppBear.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("+1 boost and haste wear off at end of turn")
    void plusOneWearsOff() {
        addReadySarkhan(player1);
        Permanent bear = addCreatureReady(player1, new CylianElf());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("-2 steals target creature, untaps it, and grants haste")
    void minusTwoStealsUntapsAndGrantsHaste() {
        Permanent sarkhan = addReadySarkhan(player1);
        Permanent target = addCreatureReady(player2, new CylianElf());
        target.tap();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isEqualTo(2); // 4 - 2
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("-2 control and haste expire at end of turn")
    void minusTwoExpiresAtEndOfTurn() {
        addReadySarkhan(player1);
        Permanent target = addCreatureReady(player2, new CylianElf());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("-2 cannot target a non-creature permanent")
    void minusTwoCannotTargetNonCreature() {
        addReadySarkhan(player1);
        addCreatureReady(player2, new CylianElf()); // valid target exists
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ObeliskOfNaya());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("-6 creates five 4/4 red Dragon tokens with flying")
    void minusSixCreatesFiveDragons() {
        Permanent sarkhan = addReadySarkhan(player1);
        sarkhan.setCounterCount(CounterType.LOYALTY, 6);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        // Sarkhan dies (6 - 6 = 0)
        harness.assertNotOnBattlefield(player1, "Sarkhan Vol");

        List<Permanent> dragons = findPermanents(player1, "Dragon");
        assertThat(dragons).hasSize(5);
        assertThat(dragons).allSatisfy(d -> {
            assertThat(d.getCard().getPower()).isEqualTo(4);
            assertThat(d.getCard().getToughness()).isEqualTo(4);
            assertThat(d.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(d.getCard().getSubtypes()).contains(CardSubtype.DRAGON);
            assertThat(d.hasKeyword(Keyword.FLYING)).isTrue();
        });
    }

    @Test
    @DisplayName("Cannot activate -6 with only 4 loyalty")
    void cannotActivateUltimateWithInsufficientLoyalty() {
        addReadySarkhan(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    @DisplayName("+1 also grants haste to Sarkhan when he is a creature")
    void plusOneAffectsAnimatedSarkhan() {
        Permanent sarkhan = addReadySarkhan(player1);
        harness.addToBattlefield(player1, new MycosynthLattice());
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        sarkhan.setSummoningSick(true);

        assertThat(gqs.isCreature(gd, sarkhan)).isTrue();
        assertThat(gqs.hasKeyword(gd, sarkhan, Keyword.HASTE)).isFalse();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sarkhan)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, sarkhan)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, sarkhan, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("+1 affects creatures present at resolution but not later arrivals")
    void plusOneLocksInCreaturesAtResolution() {
        addReadySarkhan(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new CylianElf());

        assertThat(beforeResolution.getEffectivePower()).isEqualTo(3);
        assertThat(beforeResolution.getEffectiveToughness()).isEqualTo(3);
        assertThat(beforeResolution.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(afterResolution.getEffectivePower()).isEqualTo(2);
        assertThat(afterResolution.getEffectiveToughness()).isEqualTo(2);
        assertThat(afterResolution.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("-2 can untap and grant haste to a creature already controlled")
    void minusTwoCanTargetOwnCreature() {
        addReadySarkhan(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        target.tap();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("-2 resolves and lasts until end of turn even if its cost kills Sarkhan")
    void minusTwoResolvesAfterSarkhanDies() {
        Permanent sarkhan = addReadySarkhan(player1);
        sarkhan.setCounterCount(CounterType.LOYALTY, 2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CylianElf());
        target.tap();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sarkhan Vol");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    private Permanent addReadySarkhan(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SarkhanVol());
        perm.setCounterCount(CounterType.LOYALTY, 4);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
