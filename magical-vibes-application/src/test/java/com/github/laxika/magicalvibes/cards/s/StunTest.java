package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CursedScroll;
import com.github.laxika.magicalvibes.cards.j.JackalPup;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Stun.class, TrainedArmodon.class, CursedScroll.class, JackalPup.class})
class StunTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Stun puts it on the stack with target creature")
    void castingPutsOnStack() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TrainedArmodon());
        harness.setHand(player1, List.of(new Stun()));
        addStunMana();

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Cannot cast Stun without enough mana")
    void cannotCastWithoutEnoughMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TrainedArmodon());
        harness.setHand(player1, List.of(new Stun()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast Stun without red mana")
    void cannotCastWithoutRedMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TrainedArmodon());
        harness.setHand(player1, List.of(new Stun()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CursedScroll());
        harness.setHand(player1, List.of(new Stun()));
        addStunMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Resolving Stun makes target unable to block and draws a card")
    void resolvingSetsCantBlockAndDraws() {
        Permanent attacker = addCreatureReady(player1, new TrainedArmodon());
        Permanent blocker = addCreatureReady(player2, new TrainedArmodon());
        JackalPup drawnCard = new JackalPup();

        harness.setHand(player1, List.of(new Stun()));
        harness.setLibrary(player1, List.of(drawnCard));
        addStunMana();

        harness.castAndResolveInstant(player1, 0, blocker.getId());

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(drawnCard.getId());

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Stun goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TrainedArmodon());
        harness.setHand(player1, List.of(new Stun()));
        addStunMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player1, "Stun");
    }

    @Test
    @DisplayName("Stun fizzles and does not draw if target is removed before resolution")
    void fizzlesAndDoesNotDrawIfTargetRemoved() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TrainedArmodon());
        harness.setHand(player1, List.of(new Stun()));
        harness.setLibrary(player1, List.of(new JackalPup()));
        addStunMana();

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void addStunMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}

