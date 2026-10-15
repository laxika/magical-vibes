package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ChandraPyromaster;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AjaniSteadfast.class, RuneclawBear.class, ChandraPyromaster.class, LightningStrike.class})
class AjaniSteadfastTest extends BaseCardTest {

    @Test
    @DisplayName("+1 boosts one creature and grants the three keywords until end of turn")
    void plusOneBoostsTargetCreature() {
        Permanent ajani = addReadyAjani(player1, 4);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        int ajaniIndex = gd.playerBattlefields.get(player1.getId()).indexOf(ajani);
        harness.activateAbility(player1, ajaniIndex, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.LIFELINK)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("-2 puts counters on controlled creatures and other planeswalkers")
    void minusTwoPutsCountersOnCreaturesAndOtherPlaneswalkers() {
        Permanent ajani = addReadyAjani(player1, 4);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        Permanent chandra = harness.addToBattlefieldAndReturn(player1, new ChandraPyromaster());
        chandra.setCounterCount(CounterType.LOYALTY, 3);
        Permanent opponentChandra = harness.addToBattlefieldAndReturn(player2, new ChandraPyromaster());
        opponentChandra.setCounterCount(CounterType.LOYALTY, 4);

        int ajaniIndex = gd.playerBattlefields.get(player1.getId()).indexOf(ajani);
        harness.activateAbility(player1, ajaniIndex, 1, null, null);
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(ajani.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(opponentChandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("-7 emblem prevents all but 1 damage to its controller")
    void minusSevenEmblemProtectsController() {
        Permanent ajani = addReadyAjani(player1, 7);
        harness.setLife(player1, 20);

        int ajaniIndex = gd.playerBattlefields.get(player1.getId()).indexOf(ajani);
        harness.activateAbility(player1, ajaniIndex, 2, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("-7 emblem prevents all but 1 damage to a planeswalker it protects")
    void minusSevenEmblemProtectsPlaneswalker() {
        Permanent ajani = addReadyAjani(player1, 7);
        Permanent chandra = harness.addToBattlefieldAndReturn(player1, new ChandraPyromaster());
        chandra.setCounterCount(CounterType.LOYALTY, 6);

        int ajaniIndex = gd.playerBattlefields.get(player1.getId()).indexOf(ajani);
        harness.activateAbility(player1, ajaniIndex, 2, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, chandra.getId());

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("+1 can be activated without a target on an empty battlefield")
    void plusOneWithoutTarget() {
        Permanent ajani = addReadyAjani(player1, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("+1 may target an opponent's creature")
    void plusOneCanTargetOpponentsCreature() {
        addReadyAjani(player1, 4);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

        harness.activateAbility(player1, 0, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("The emblem applies separately to successive sources and does not protect creatures or opponents")
    void emblemOnlyProtectsControllerAndPlaneswalkersForEachSource() {
        addReadyAjani(player1, 7);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new LightningStrike(), new LightningStrike(), new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 6);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        harness.castAndResolveInstant(player2, 0, bear.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);

        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    private Permanent addReadyAjani(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new AjaniSteadfast());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
