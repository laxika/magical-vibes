package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ConsulateDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkyCycle.class, ConsulateDreadnought.class, GrizzlyBears.class})
class SkyCycleTest extends BaseCardTest {

    @Test
    void entersAndDealsTwiceTheNumberOfControlledVehicles() {
        Permanent target = addOpponentBear();
        harness.addToBattlefield(player1, new ConsulateDreadnought());

        castSkyCycle(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void canEnterWithoutChoosingATarget() {
        harness.setHand(player1, List.of(new SkyCycle()));
        addCastingMana();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotTargetANonCreaturePermanent() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player2, new ConsulateDreadnought());
        harness.setHand(player1, List.of(new SkyCycle()));
        addCastingMana();

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, vehicle.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void crewAnimatesSkyCycleAndTapsCrew() {
        Permanent skyCycle = harness.addToBattlefieldAndReturn(player1, new SkyCycle());
        Permanent crew = addCreatureReady();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, skyCycle)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void countsItselfButNotOpponentsVehicles() {
        Permanent target = addOpponentBear();
        harness.addToBattlefield(player2, new SkyCycle());

        castSkyCycle(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void canTargetAControlledCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castSkyCycle(target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target.getCard());
    }

    @Test
    void countsVehiclesAddedAfterTheTriggerIsPutOnTheStack() {
        Permanent target = addOpponentBear();
        harness.setHand(player1, List.of(new SkyCycle()));
        addCastingMana();
        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new SkyCycle());

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void doesNotCountTheSourceAfterItLeavesTheBattlefield() {
        Permanent target = addOpponentBear();
        harness.setHand(player1, List.of(new SkyCycle()));
        addCastingMana();
        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void summoningSickCreatureCanCrew() {
        Permanent skyCycle = harness.addToBattlefieldAndReturn(player1, new SkyCycle());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, skyCycle)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void cannotCrewWithoutEnoughUntappedPower() {
        harness.addToBattlefield(player1, new SkyCycle());
        Permanent crew = addCreatureReady();
        crew.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addOpponentBear() {
        GrizzlyBears bear = new GrizzlyBears();
        bear.setToughness(8);
        return harness.addToBattlefieldAndReturn(player2, bear);
    }

    private Permanent addCreatureReady() {
        return addCreatureReady(player1, new GrizzlyBears());
    }

    private void castSkyCycle(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new SkyCycle()));
        addCastingMana();
        harness.castArtifact(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
