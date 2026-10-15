package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.cards.d.DreamtailHeron;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WingspanMentor.class, AlmightyBrushwagg.class, DreamtailHeron.class})
class WingspanMentorTest extends BaseCardTest {

    @Test
    void entersWithFlyingCounterOnTargetNonHumanCreature() {
        Permanent target = addCreatureReady(player1, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new WingspanMentor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    void cannotTargetHumanCreature() {
        Permanent target = addCreatureReady(player1, new WingspanMentor());
        harness.setHand(player1, List.of(new WingspanMentor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activatedAbilityPutsCountersOnlyOnCreaturesWithFlying() {
        Permanent mentor = addCreatureReady(player1, new WingspanMentor());
        Permanent flyingCreature = addCreatureReady(player1, new AlmightyBrushwagg());
        flyingCreature.setCounterCount(CounterType.FLYING, 1);
        Permanent ordinaryCreature = addCreatureReady(player1, new AlmightyBrushwagg());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mentor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(flyingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ordinaryCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotTargetOpponentsNonHumanCreature() {
        Permanent target = addCreatureReady(player2, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new WingspanMentor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void entersWithoutAnyLegalTarget() {
        harness.setHand(player1, List.of(new WingspanMentor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Wingspan Mentor");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void activatedAbilityIncludesNaturalFlyingAndHumanFlyersButNotOpponents() {
        Permanent mentor = addCreatureReady(player1, new WingspanMentor());
        mentor.setCounterCount(CounterType.FLYING, 1);
        Permanent naturalFlyer = addCreatureReady(player1, new DreamtailHeron());
        Permanent opposingFlyer = addCreatureReady(player2, new DreamtailHeron());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(mentor.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(mentor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(naturalFlyer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingFlyer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void activatedAbilityChecksFlyingAtResolutionAndSurvivesSourceRemoval() {
        Permanent mentor = addCreatureReady(player1, new WingspanMentor());
        Permanent gainingFlying = addCreatureReady(player1, new AlmightyBrushwagg());
        Permanent losingFlying = addCreatureReady(player1, new AlmightyBrushwagg());
        losingFlying.setCounterCount(CounterType.FLYING, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        gainingFlying.setCounterCount(CounterType.FLYING, 1);
        losingFlying.setCounterCount(CounterType.FLYING, 0);
        gd.playerBattlefields.get(player1.getId()).remove(mentor);
        gd.playerGraveyards.get(player1.getId()).add(mentor.getCard());
        harness.passBothPriorities();

        assertThat(gainingFlying.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(losingFlying.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent mentor = addCreatureReady(player1, new WingspanMentor());
        mentor.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enterTriggerDoesNotPutCounterOnCreatureNoLongerControlled() {
        Permanent target = addCreatureReady(player1, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new WingspanMentor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.FLYING)).isZero();
        harness.assertOnBattlefield(player1, "Wingspan Mentor");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enterTriggerCanTargetCreatureAlreadyWithFlying() {
        Permanent target = addCreatureReady(player1, new DreamtailHeron());
        harness.setHand(player1, List.of(new WingspanMentor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.FLYING)).isEqualTo(1);
    }
}
