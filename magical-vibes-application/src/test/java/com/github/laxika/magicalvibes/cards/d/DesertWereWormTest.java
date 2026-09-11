package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DesertWereWorm.class, AirElemental.class, HillGiant.class, Mountain.class})
class DesertWereWormTest extends BaseCardTest {

    @Test
    @DisplayName("Desert Were-Worm gets +2/+0 for each Mountain you control")
    void getsPowerForEachMountain() {
        Permanent worm = addCreatureReady(player1, new DesertWereWorm());

        assertThat(gqs.getEffectivePower(gd, worm)).isZero();

        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());

        assertThat(gqs.getEffectivePower(gd, worm)).isEqualTo(4);
    }

    @Test
    @DisplayName("Attacking with total power 12 or greater untaps attackers and adds a combat")
    void qualifyingAttackUntapsAndAddsCombat() {
        Permanent worm = addCreatureReady(player1, new DesertWereWorm());
        Permanent elemental = addCreatureReady(player1, new AirElemental());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        addMountains(3);

        assertThat(gqs.getEffectivePower(gd, worm)).isEqualTo(6);
        gd.combatPhasesThisTurn = 1;
        declareAttackers(List.of(0, 1, 2));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Desert Were-Worm"));
        assertThat(worm.isTapped()).isTrue();
        assertThat(elemental.isTapped()).isTrue();
        assertThat(giant.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(worm.isTapped()).isFalse();
        assertThat(elemental.isTapped()).isFalse();
        assertThat(giant.isTapped()).isFalse();
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack condition counts only the creatures in the attack")
    void nonattackingPowerDoesNotQualify() {
        addCreatureReady(player1, new DesertWereWorm());
        addCreatureReady(player1, new AirElemental());
        addCreatureReady(player1, new HillGiant());
        addMountains(3);

        declareAttackers(List.of(0, 1));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Desert Were-Worm"));
    }

    @Test
    @DisplayName("The qualifying attack trigger fires only once each turn")
    void qualifyingAttackTriggersOnlyOnceEachTurn() {
        addCreatureReady(player1, new DesertWereWorm());
        addCreatureReady(player1, new AirElemental());
        addCreatureReady(player1, new HillGiant());
        addMountains(3);

        gd.combatPhasesThisTurn = 1;
        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();

        declareAttackers(List.of(0, 1, 2));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Desert Were-Worm"));
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    private void addMountains(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new Mountain());
        }
    }
}
