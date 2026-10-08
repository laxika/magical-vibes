package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.l.LlanowarAugur;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SproutSwarm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({VoidMirror.class, MindStone.class, Ornithopter.class, SproutSwarm.class, LlanowarAugur.class})
class VoidMirrorTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell cast using only colorless mana")
    void countersSpellCastUsingOnlyColorlessMana() {
        harness.addToBattlefield(player1, new VoidMirror());
        castMindStone(player2, ManaColor.COLORLESS, 2);

        harness.assertInGraveyard(player2, "Mind Stone");
    }

    @Test
    @DisplayName("Does not counter a spell when colored mana was spent")
    void doesNotCounterSpellWhenColoredManaWasSpent() {
        harness.addToBattlefield(player1, new VoidMirror());
        castMindStone(player2, ManaColor.BLUE, 2);

        harness.assertOnBattlefield(player2, "Mind Stone");
    }

    @Test
    @DisplayName("Counters its controller's spell cast using only colorless mana")
    void countersControllersSpell() {
        harness.addToBattlefield(player1, new VoidMirror());
        castMindStone(player1, ManaColor.COLORLESS, 2);

        harness.assertInGraveyard(player1, "Mind Stone");
    }

    @Test
    @DisplayName("One colored mana alongside colorless mana prevents the counter trigger")
    void doesNotCounterMixedManaPayment() {
        harness.addToBattlefield(player1, new VoidMirror());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        castMindStone(player2, ManaColor.BLUE, 1);

        harness.assertOnBattlefield(player2, "Mind Stone");
    }

    @Test
    @DisplayName("Counters a zero-cost spell even with unused colored mana in the pool")
    void countersZeroCostSpellWithUnusedColoredMana() {
        harness.addToBattlefield(player1, new VoidMirror());
        harness.setHand(player1, List.of(new Ornithopter()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Colored creatures used for convoke do not count as colored mana spent")
    void countersSpellPaidEntirelyWithConvoke() {
        harness.addToBattlefield(player1, new VoidMirror());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new LlanowarAugur());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new LlanowarAugur());
        harness.setHand(player1, List.of(new SproutSwarm()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sprout Swarm");
        harness.assertNotOnBattlefield(player1, "Saproling");
    }

    @Test
    @DisplayName("Counters a spell paid with colorless mana and a green creature's convoke")
    void countersSpellPaidWithColorlessManaAndConvoke() {
        harness.addToBattlefield(player1, new VoidMirror());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LlanowarAugur());
        harness.setHand(player1, List.of(new SproutSwarm()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sprout Swarm");
        harness.assertNotOnBattlefield(player1, "Saproling");
    }

    private void castMindStone(Player player, ManaColor manaColor, int amount) {
        harness.setHand(player, List.of(new MindStone()));
        harness.addMana(player, manaColor, amount);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castArtifact(player, 0);
        harness.passBothPriorities();
    }
}
