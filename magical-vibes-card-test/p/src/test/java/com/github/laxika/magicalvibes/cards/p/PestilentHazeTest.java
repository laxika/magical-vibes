package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.cards.d.DrowsingTyrannodon;
import com.github.laxika.magicalvibes.cards.g.GarrukUnleashed;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PestilentHaze.class, AlpineWatchdog.class, DrowsingTyrannodon.class, GarrukUnleashed.class})
class PestilentHazeTest extends BaseCardTest {

    @Test
    @DisplayName("First mode gives -2/-2 to all creatures")
    void debuffsAllCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new DrowsingTyrannodon());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());

        castPestilentHaze(0);

        assertThat(ownCreature.getEffectivePower()).isEqualTo(1);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
    }

    @Test
    @DisplayName("Second mode removes two loyalty counters from each planeswalker")
    void removesLoyaltyCountersFromAllPlaneswalkers() {
        Permanent ownPlaneswalker = addReadyPlaneswalker(player1, 5);
        Permanent opponentPlaneswalker = addReadyPlaneswalker(player2, 4);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DrowsingTyrannodon());

        castPestilentHaze(1);

        assertThat(ownPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(opponentPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(creature.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Second mode removes only the loyalty counters a planeswalker has")
    void removesAtMostAvailableLoyaltyCounters() {
        Permanent planeswalker = addReadyPlaneswalker(player2, 1);

        castPestilentHaze(1);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(planeswalker);
    }

    @Test
    @DisplayName("A planeswalker with exactly two loyalty dies after the second mode")
    void removesPlaneswalkerWithExactlyTwoLoyalty() {
        Permanent planeswalker = addReadyPlaneswalker(player1, 2);

        castPestilentHaze(1);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(planeswalker);
        harness.assertInGraveyard(player1, "Garruk, Unleashed");
    }

    @Test
    @DisplayName("Creature reduction ends during cleanup for both players")
    void creatureReductionExpiresAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new DrowsingTyrannodon());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new DrowsingTyrannodon());

        castPestilentHaze(0);

        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(1);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(ownCreature.getEffectivePower()).isEqualTo(3);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(3);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Creatures entering after resolution are unaffected")
    void doesNotAffectCreaturesEnteringLater() {
        castPestilentHaze(0);

        Permanent newcomer = harness.enterBattlefieldAndReturn(player2, new AlpineWatchdog());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(newcomer);
        assertThat(newcomer.getEffectivePower()).isEqualTo(2);
        assertThat(newcomer.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("First mode leaves planeswalker loyalty unchanged")
    void creatureModeDoesNotRemoveLoyalty() {
        Permanent ownPlaneswalker = addReadyPlaneswalker(player1, 4);
        Permanent opponentPlaneswalker = addReadyPlaneswalker(player2, 4);

        castPestilentHaze(0);

        assertThat(ownPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(opponentPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Loyalty mode leaves other counters and nonplaneswalker loyalty untouched")
    void removesOnlyPlaneswalkerLoyaltyCounters() {
        Permanent planeswalker = addReadyPlaneswalker(player2, 4);
        planeswalker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());
        creature.setCounterCount(CounterType.LOYALTY, 4);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castPestilentHaze(1);

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(planeswalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(creature.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Loyalty mode can be chosen with no planeswalkers and leaves small creatures alive")
    void loyaltyModeDoesNotRequirePlaneswalkers() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());

        castPestilentHaze(1);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    private void castPestilentHaze(int modeIndex) {
        harness.setHand(player1, List.of(new PestilentHaze()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorceryWithModes(player1, 0, 1, new int[]{modeIndex}, List.of());
        harness.passBothPriorities();
    }

    private Permanent addReadyPlaneswalker(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GarrukUnleashed());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
