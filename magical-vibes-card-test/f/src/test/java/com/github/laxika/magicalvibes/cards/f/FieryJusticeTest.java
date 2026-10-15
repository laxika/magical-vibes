package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BalduvianBarbarians;
import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FieryJustice.class, BalduvianBarbarians.class, BalduvianBears.class, GarrukWildspeaker.class})
class FieryJusticeTest extends BaseCardTest {

    @Test
    void dealsAll5DamageToOneCreatureAndOpponentGains5Life() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FieryJustice()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player1, ManaColor.WHITE, 5);

        Permanent giant = harness.addToBattlefieldAndReturn(player2, new BalduvianBarbarians());
        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, player2.getId(), Map.of(giant.getId(), 5));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Balduvian Barbarians");
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore + 5);
    }

    @Test
    void splitsDamageAmongCreatureAndPlayer() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FieryJustice()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player1, ManaColor.WHITE, 5);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, player2.getId(), Map.of(bears.getId(), 2, player2.getId(), 3));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Balduvian Bears");
        // 3 damage to the opponent, then the same opponent gains 5 life.
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3 + 5);
    }

    @Test
    void damageAssignmentsMustSumTo5() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FieryJustice()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player1, ManaColor.WHITE, 5);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());

        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, player2.getId(), Map.of(bears.getId(), 3))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageAssignmentsMustBePositive() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FieryJustice()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player1, ManaColor.WHITE, 5);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());

        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, player2.getId(), Map.of(bears.getId(), 0, player2.getId(), 5))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void lifeGainTargetMustBeAnOpponent() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FieryJustice()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player1, ManaColor.WHITE, 5);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());

        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, player1.getId(), Map.of(bears.getId(), 5))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void keepsTheOriginalDivisionWhenOneDamageTargetBecomesIllegal() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FieryJustice()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player1, ManaColor.WHITE, 5);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, player2.getId(), Map.of(bears.getId(), 4, player2.getId(), 1));
        gd.playerBattlefields.get(player2.getId()).remove(bears);
        harness.passBothPriorities();

        // The missing four damage is not reassigned to the still-legal player target.
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1 + 5);
    }

    @Test
    void lifeGainStillHappensWhenEveryDamageTargetBecomesIllegal() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FieryJustice()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player1, ManaColor.WHITE, 5);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, player2.getId(), Map.of(bears.getId(), 5));
        gd.playerBattlefields.get(player2.getId()).remove(bears);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore + 5);
    }

    @Test
    void canAssignDamageToAPlaneswalker() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FieryJustice()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player1, ManaColor.WHITE, 5);

        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);

        harness.castSorcery(player1, 0, player2.getId(), Map.of(planeswalker.getId(), 2, player2.getId(), 3));
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(22);
    }

    @Test
    void canDealDamageToItsControllerWhileOpponentGainsLife() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FieryJustice()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, player2.getId(), Map.of(player1.getId(), 5));
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 25);
    }

    @Test
    void opponentSurvivesTemporarilyLethalDamageBeforeGainingLife() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FieryJustice()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player2, 3);

        harness.castSorcery(player1, 0, player2.getId(), Map.of(player2.getId(), 5));
        harness.passBothPriorities();

        harness.assertLife(player2, 3);
        assertThat(gd.gameResult).isNull();
    }
}
