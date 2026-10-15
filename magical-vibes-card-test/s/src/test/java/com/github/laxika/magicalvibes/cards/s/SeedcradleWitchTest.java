package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeedcradleWitch.class, GrizzlyBears.class})
class SeedcradleWitchTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +3/+3 and is untapped when the ability resolves")
    void boostsAndUntapsTarget() {
        setupWitch();
        Permanent bear = findPermanent(player1, "Grizzly Bears");
        bear.tap();
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(3);
        assertThat(bear.getToughnessModifier()).isEqualTo(3);
        assertThat(bear.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        setupWitch();
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Witch can activate targeting itself")
    void tappedSummoningSickWitchCanTargetItself() {
        Permanent witch = setupSingleWitch();
        witch.setSummoningSick(true);
        witch.tap();

        harness.activateAbility(player1, 0, null, witch.getId());
        harness.passBothPriorities();

        assertThat(witch.getPowerModifier()).isEqualTo(3);
        assertThat(witch.getToughnessModifier()).isEqualTo(3);
        assertThat(witch.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's creature can be boosted and untapped")
    void targetsOpponentsCreature() {
        Permanent source = setupSingleWitch();
        source.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SeedcradleWitch());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(target.isTapped()).isFalse();
        assertThat(source.isTapped()).isTrue();
        assertThat(source.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Repeated activations boost an already untapped creature cumulatively")
    void repeatedActivationsAccumulate() {
        Permanent witch = setupSingleWitch();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, witch.getId());
        harness.activateAbility(player1, 0, null, witch.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(witch.getPowerModifier()).isEqualTo(6);
        assertThat(witch.getToughnessModifier()).isEqualTo(6);
        assertThat(witch.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability still resolves after its source leaves the battlefield")
    void resolvesWithoutSource() {
        Permanent source = setupSingleWitch();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SeedcradleWitch());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A creature that leaves and returns is not affected by the old target")
    void returnedCreatureIsNotTheOldTarget() {
        setupSingleWitch();
        SeedcradleWitch targetCard = new SeedcradleWitch();
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        Permanent returned = harness.addToBattlefieldAndReturn(player2, targetCard);
        returned.tap();
        harness.passBothPriorities();

        assertThat(returned.getPowerModifier()).isZero();
        assertThat(returned.getToughnessModifier()).isZero();
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent setupSingleWitch() {
        Permanent witch = harness.addToBattlefieldAndReturn(player1, new SeedcradleWitch());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return witch;
    }

    private void setupWitch() {
        setupSingleWitch();
        harness.addToBattlefield(player1, new GrizzlyBears());
    }
}
