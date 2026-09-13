package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CavePeople.class, GrizzlyBears.class, HowlingMine.class})
class CavePeopleTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gives Cave People +1/-2 until end of turn")
    void attackingBoostsSelf() {
        Permanent cavePeople = addCreatureReady(player1, new CavePeople());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(cavePeople.getPowerModifier()).isEqualTo(1);
        assertThat(cavePeople.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("The +1/-2 wears off at end of turn")
    void boostWearsOff() {
        Permanent cavePeople = addCreatureReady(player1, new CavePeople());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(cavePeople.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(cavePeople.getPowerModifier()).isEqualTo(0);
        assertThat(cavePeople.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("{1}{R}{R}, {T} ability grants mountainwalk to the target creature")
    void grantsMountainwalkToTarget() {
        Permanent cavePeople = addCreatureReady(player1, new CavePeople());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.MOUNTAINWALK)).isTrue();
        assertThat(cavePeople.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Mountainwalk wears off at end of turn")
    void mountainwalkWearsOff() {
        addCreatureReady(player1, new CavePeople());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.MOUNTAINWALK)).isFalse();
    }

    @Test
    @DisplayName("Ability targeting a non-creature is rejected")
    void illegalTargetRejected() {
        addCreatureReady(player1, new CavePeople());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The ability can target a creature an opponent controls")
    void grantsMountainwalkToOpponentsCreature() {
        addCreatureReady(player1, new CavePeople());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.MOUNTAINWALK)).isTrue();
    }

    @Test
    @DisplayName("The ability cannot be activated without {1}{R}{R}")
    void cannotActivateWithoutEnoughMana() {
        Permanent cavePeople = addCreatureReady(player1, new CavePeople());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(cavePeople.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("The tap ability cannot be activated while Cave People is tapped")
    void cannotActivateAbilityWhileTapped() {
        Permanent cavePeople = addCreatureReady(player1, new CavePeople());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 6);

        harness.activateAbility(player1, 0, 0, null, target.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(cavePeople.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    @CardUsed(Mountain.class)
    @DisplayName("Mountainwalk prevents blocking while the defending player controls a Mountain")
    void mountainwalkPreventsBlockingAgainstMountain() {
        addCreatureReady(player1, new CavePeople());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null, attacker.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.MOUNTAINWALK)).isTrue();

        declareAttackersAndPrepareBlockers(List.of(1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mountainwalk");
    }

    @Test
    @DisplayName("Mountainwalk does not prevent blocking when the defending player controls no Mountain")
    void mountainwalkAllowsBlockingWithoutMountain() {
        addCreatureReady(player1, new CavePeople());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null, attacker.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.MOUNTAINWALK)).isTrue();

        declareAttackers(List.of(1));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
