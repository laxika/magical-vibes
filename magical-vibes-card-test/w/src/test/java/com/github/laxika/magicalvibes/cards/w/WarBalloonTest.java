package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.EnchantedEvening;
import com.github.laxika.magicalvibes.cards.o.OtterPenguin;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarBalloon.class, OtterPenguin.class, EnchantedEvening.class})
class WarBalloonTest extends BaseCardTest {

    @Test
    @DisplayName("The fire-counter ability costs one mana and adds a fire counter")
    void putsOnFireCounter() {
        Permanent balloon = addWarBalloonReady();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(balloon.getCounterCount(CounterType.FIRE)).isEqualTo(1);
        assertThat(gqs.isCreature(gd, balloon)).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Three fire counters make War Balloon an artifact creature")
    void becomesCreatureAtThreeFireCounters() {
        Permanent balloon = addWarBalloonReady();

        balloon.setCounterCount(CounterType.FIRE, 2);
        assertThat(gqs.isCreature(gd, balloon)).isFalse();

        balloon.setCounterCount(CounterType.FIRE, 3);
        assertThat(gqs.isCreature(gd, balloon)).isTrue();
        assertThat(gqs.isArtifact(balloon)).isTrue();
        assertThat(gqs.getEffectivePower(gd, balloon)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, balloon)).isEqualTo(3);

        balloon.setCounterCount(CounterType.FIRE, 2);
        assertThat(gqs.isCreature(gd, balloon)).isFalse();
    }

    @Test
    @DisplayName("Crew 3 animates War Balloon until end of turn")
    void crewAnimatesUntilEndOfTurn() {
        Permanent balloon = addWarBalloonReady();
        Permanent firstCrewMember = addCreatureReady(player1, new OtterPenguin());
        Permanent secondCrewMember = addCreatureReady(player1, new OtterPenguin());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, balloon)).isTrue();
        assertThat(firstCrewMember.isTapped()).isTrue();
        assertThat(secondCrewMember.isTapped()).isTrue();
        assertThat(balloon.isTapped()).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, balloon)).isFalse();
    }

    @Test
    void repeatedActivationsAnimateTappedVehicleOnlyOnResolution() {
        Permanent balloon = addWarBalloonReady();
        balloon.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        for (int counter = 1; counter <= 3; counter++) {
            harness.activateAbility(player1, 0, 0, null, null);
            assertThat(balloon.getCounterCount(CounterType.FIRE)).isEqualTo(counter - 1);
            assertThat(gqs.isCreature(gd, balloon)).isFalse();
            harness.passBothPriorities();
            assertThat(balloon.getCounterCount(CounterType.FIRE)).isEqualTo(counter);
            assertThat(gqs.isCreature(gd, balloon)).isEqualTo(counter >= 3);
        }

        assertThat(balloon.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void fireCounterAnimationPersistsAfterCrewExpires() {
        Permanent balloon = addWarBalloonReady();
        balloon.setCounterCount(CounterType.FIRE, 4);
        addCreatureReady(player1, new OtterPenguin());
        addCreatureReady(player1, new OtterPenguin());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, balloon)).isTrue();
        assertThat(balloon.getCounterCount(CounterType.FIRE)).isEqualTo(4);
        balloon.setCounterCount(CounterType.FIRE, 2);
        assertThat(gqs.isCreature(gd, balloon)).isFalse();
    }

    @Test
    void crewKeepsVehicleAnimatedWhenFireCountersFallBelowThreshold() {
        Permanent balloon = addWarBalloonReady();
        balloon.setCounterCount(CounterType.FIRE, 3);
        Permanent firstCrewMember = addCreatureReady(player1, new OtterPenguin());
        Permanent secondCrewMember = addCreatureReady(player1, new OtterPenguin());
        firstCrewMember.setSummoningSick(true);
        secondCrewMember.setSummoningSick(true);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        balloon.setCounterCount(CounterType.FIRE, 0);

        assertThat(gqs.isCreature(gd, balloon)).isTrue();
        assertThat(firstCrewMember.isTapped()).isTrue();
        assertThat(secondCrewMember.isTapped()).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.isCreature(gd, balloon)).isFalse();
    }

    @Test
    void crewRejectsInsufficientPowerWithoutTappingCreatures() {
        Permanent balloon = addWarBalloonReady();
        Permanent crewMember = addCreatureReady(player1, new OtterPenguin());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(crewMember.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, balloon)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({WarBalloon.class, EnchantedEvening.class})
    void fireCounterAnimationRetainsExistingEnchantmentType() {
        Permanent evening = harness.addToBattlefieldAndReturn(player1, new EnchantedEvening());
        evening.setTimestamp(1);
        Permanent balloon = addWarBalloonReady();
        balloon.setTimestamp(2);
        balloon.setCounterCount(CounterType.FIRE, 2);
        assertThat(gqs.isEnchantment(gd, balloon)).isTrue();

        balloon.setCounterCount(CounterType.FIRE, 3);

        assertThat(gqs.isCreature(gd, balloon)).isTrue();
        assertThat(gqs.isArtifact(gd, balloon)).isTrue();
        assertThat(gqs.isEnchantment(gd, balloon)).isTrue();
    }

    private Permanent addWarBalloonReady() {
        return addCreatureReady(player1, new WarBalloon());
    }
}
