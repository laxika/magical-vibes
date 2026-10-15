package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AjaniSteadfast;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CovenantOfBlood.class, RuneclawBear.class, ChildOfNight.class, AjaniSteadfast.class})
class CovenantOfBloodTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to target player and controller gains 4 life")
    void dealsDamageAndGainsLife() {
        harness.setHand(player1, List.of(new CovenantOfBlood()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Convoke taps creatures and reduces the mana needed to cast the spell")
    void castsWithConvoke() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new CovenantOfBlood()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        gs.playCard(gd, player1, 0, 0, player2.getId(), null, List.of(),
                List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    void killsCreatureAndGainsFourLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new CovenantOfBlood()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
    }

    @Test
    void gainsNoLifeWhenOnlyTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new CovenantOfBlood()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Covenant of Blood");
    }

    @Test
    void canTargetController() {
        harness.setHand(player1, List.of(new CovenantOfBlood()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Covenant of Blood");
    }

    @Test
    void convokesEntireCostIncludingBlackWithSummoningSickCreatures() {
        List<UUID> creatures = new ArrayList<>();
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player1, new ChildOfNight());
        blackCreature.setSummoningSick(true);
        creatures.add(blackCreature.getId());
        for (int i = 0; i < 6; i++) {
            Permanent creature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
            creature.setSummoningSick(true);
            creatures.add(creature.getId());
        }
        harness.setHand(player1, List.of(new CovenantOfBlood()));
        gs.playCard(gd, player1, 0, 0, player2.getId(), null, List.of(), creatures);
        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(Permanent::isTapped);
        harness.passBothPriorities();
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }

    @Test
    void damagesPlaneswalkerAndGainsFourLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AjaniSteadfast());
        target.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new CovenantOfBlood()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Ajani Steadfast");
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
    }
}
