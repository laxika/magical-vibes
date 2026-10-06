package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SanctuaryWall.class, Forest.class})
class SanctuaryWallTest extends BaseCardTest {

    @BeforeEach
    void setUp() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void tapsTargetAndAcceptingPutsStunCountersOnBothCreatures() {
        Permanent wall = addCreatureReady(player1, new SanctuaryWall());
        Permanent target = addCreatureReady(player2, new SanctuaryWall());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(wall.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void decliningDoesNotPutStunCountersOnEitherCreature() {
        Permanent wall = addCreatureReady(player1, new SanctuaryWall());
        Permanent target = addCreatureReady(player2, new SanctuaryWall());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
        assertThat(wall.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    void rejectsNonCreatureTarget() {
        addCreatureReady(player1, new SanctuaryWall());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void canPutStunCountersOnAnAlreadyTappedTarget() {
        Permanent wall = addCreatureReady(player1, new SanctuaryWall());
        Permanent target = addCreatureReady(player2, new SanctuaryWall());
        target.setTapped(true);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(wall.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void targetingItselfPutsTwoStunCountersOnTheWall() {
        Permanent wall = addCreatureReady(player1, new SanctuaryWall());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 0, null, wall.getId());
        assertThat(wall.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(wall.getCounterCount(CounterType.STUN)).isEqualTo(2);
        harness.performUntapStep(player1);
        assertThat(wall.isTapped()).isTrue();
        assertThat(wall.getCounterCount(CounterType.STUN)).isEqualTo(1);
        harness.performUntapStep(player1);
        assertThat(wall.isTapped()).isTrue();
        assertThat(wall.getCounterCount(CounterType.STUN)).isZero();
        harness.performUntapStep(player1);
        assertThat(wall.isTapped()).isFalse();
    }

    @Test
    void abilityStillTapsAndStunsTargetAfterSourceLeaves() {
        Permanent wall = addCreatureReady(player1, new SanctuaryWall());
        Permanent target = addCreatureReady(player2, new SanctuaryWall());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(wall);
        gd.playerGraveyards.get(player1.getId()).add(wall.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void illegalTargetPreventsTheWholeAbilityFromResolving() {
        Permanent wall = addCreatureReady(player1, new SanctuaryWall());
        Permanent target = addCreatureReady(player2, new SanctuaryWall());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(wall.isTapped()).isTrue();
        assertThat(wall.getCounterCount(CounterType.STUN)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
