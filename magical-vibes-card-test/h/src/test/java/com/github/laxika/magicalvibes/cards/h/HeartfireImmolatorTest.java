package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LilianaDreadhordeGeneral;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeartfireImmolator.class, Forest.class, GrizzlyBears.class, Shock.class,
        LilianaDreadhordeGeneral.class})
class HeartfireImmolatorTest extends BaseCardTest {

    @Test
    @DisplayName("Prowess gives Heartfire Immolator +1/+1 for a noncreature spell")
    void prowessPumpsForNoncreatureSpell() {
        Permanent immolator = addReadyImmolator();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, immolator)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, immolator)).isEqualTo(3);
    }

    @Test
    @DisplayName("Sacrificing Heartfire Immolator deals damage equal to its power to a creature")
    void sacrificeAbilityDealsPowerDamageToCreature() {
        Permanent immolator = addReadyImmolator();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.assertInGraveyard(player1, "Heartfire Immolator");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(immolator);
    }

    @Test
    @DisplayName("Sacrifice ability can target a planeswalker")
    void sacrificeAbilityDamagesPlaneswalker() {
        addReadyImmolator();
        Permanent planeswalker = addPlaneswalker(5);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Heartfire Immolator");
    }

    @Test
    @DisplayName("Sacrifice ability cannot target a land")
    void sacrificeAbilityCannotTargetLand() {
        addReadyImmolator();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forest.getId()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void prowessBonusesStackAndExpireAtEndOfTurn() {
        Permanent immolator = addReadyImmolator();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, immolator)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, immolator)).isEqualTo(4);
        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gqs.getEffectivePower(gd, immolator)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, immolator)).isEqualTo(2);
    }

    @Test
    void sacrificeUsesPowerIncludingResolvedProwess() {
        addReadyImmolator();
        Permanent planeswalker = addPlaneswalker(6);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void sacrificeBeforeProwessResolvesUsesUnboostedPower() {
        addReadyImmolator();
        Permanent planeswalker = addPlaneswalker(6);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.activateAbility(player1, 0, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Heartfire Immolator");
    }

    @Test
    void creatureSpellsDoNotTriggerProwess() {
        Permanent immolator = addReadyImmolator();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, immolator)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, immolator)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void opponentsSpellsDoNotTriggerProwess() {
        Permanent immolator = addReadyImmolator();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, immolator)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, immolator)).isEqualTo(2);
    }

    @Test
    void sacrificeAbilityWorksWhileSummoningSickAndTapped() {
        Permanent immolator = harness.addToBattlefieldAndReturn(player1, new HeartfireImmolator());
        immolator.setSummoningSick(true);
        immolator.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Heartfire Immolator");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void sacrificeAbilityCannotTargetPlayer() {
        addReadyImmolator();
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(RuntimeException.class);
        harness.assertOnBattlefield(player1, "Heartfire Immolator");
    }

    private Permanent addReadyImmolator() {
        Permanent immolator = harness.addToBattlefieldAndReturn(player1, new HeartfireImmolator());
        immolator.setSummoningSick(false);
        return immolator;
    }

    private Permanent addPlaneswalker(int loyalty) {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new LilianaDreadhordeGeneral());
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        return planeswalker;
    }
}
