package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Capsize;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({ShadowRift.class, HornedTurtle.class, Island.class, Capsize.class})
class ShadowRiftTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Shadow Rift gives the target shadow and draws a card")
    void grantsShadowAndDraws() {
        Permanent turtle = harness.addToBattlefieldAndReturn(player1, new HornedTurtle());
        harness.setHand(player1, List.of(new ShadowRift()));
        harness.setLibrary(player1, List.of(new HornedTurtle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, turtle.getId());

        assertThat(turtle.hasKeyword(Keyword.SHADOW)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Shadow wears off at end of turn")
    void shadowWearsOff() {
        Permanent turtle = harness.addToBattlefieldAndReturn(player1, new HornedTurtle());
        harness.setHand(player1, List.of(new ShadowRift()));
        harness.setLibrary(player1, List.of(new HornedTurtle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, turtle.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(turtle.hasKeyword(Keyword.SHADOW)).isFalse();
    }

    @Test
    @DisplayName("A creature without shadow cannot block an attacker given shadow")
    void creatureWithoutShadowCannotBlockShadowAttacker() {
        Permanent attacker = addCreatureReady(player1, new HornedTurtle());
        addCreatureReady(player2, new HornedTurtle());
        attacker.setAttacking(true);

        harness.setHand(player1, List.of(new ShadowRift()));
        harness.setLibrary(player1, List.of(new HornedTurtle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, attacker.getId());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shadow");
    }

    @Test
    @DisplayName("A creature given shadow cannot block an attacker without shadow")
    void shadowBlockerCannotBlockNonShadowAttacker() {
        Permanent attacker = addCreatureReady(player1, new HornedTurtle());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new HornedTurtle());

        harness.setHand(player2, List.of(new ShadowRift()));
        harness.setLibrary(player2, List.of(new HornedTurtle()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, blocker.getId());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shadow");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new ShadowRift()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Targeting an opponent's creature still draws for the caster")
    void targetingOpponentDrawsForCaster() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HornedTurtle());
        HornedTurtle drawnCard = new HornedTurtle();
        harness.setHand(player1, List.of(new ShadowRift()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.hasKeyword(Keyword.SHADOW)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("No card is drawn when the target leaves before resolution")
    void departedTargetPreventsDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HornedTurtle());
        HornedTurtle undrawnCard = new HornedTurtle();
        harness.setHand(player1, List.of(new ShadowRift()));
        harness.setHand(player2, List.of(new Capsize()));
        harness.setLibrary(player1, List.of(undrawnCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.assertNotOnBattlefield(player1, "Horned Turtle");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawnCard);
        harness.assertInGraveyard(player1, "Shadow Rift");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creatures both granted shadow can block each other")
    void shadowCreatureCanBlockShadowAttacker() {
        Permanent attacker = addCreatureReady(player1, new HornedTurtle());
        Permanent blocker = addCreatureReady(player2, new HornedTurtle());
        harness.setHand(player1, List.of(new ShadowRift(), new ShadowRift()));
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, attacker.getId());
        harness.castAndResolveInstant(player1, 0, blocker.getId());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargetIds()).contains(attacker.getId());
    }
}
