package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.c.Capsize;
import com.github.laxika.magicalvibes.cards.c.Commandeer;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.cards.r.RootwaterHunter;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Capsize.class, Commandeer.class, Counterspell.class, HornedTurtle.class, RootwaterHunter.class})
class CounterspellTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack targeting a spell")
    void castingTargetsSpell() {
        HornedTurtle turtle = new HornedTurtle();
        harness.setHand(player1, List.of(turtle));
        harness.addMana(player1, ManaColor.BLUE, 3);

        Counterspell counterspell = new Counterspell();
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, turtle.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry entry = gd.stack.getLast();
        assertThat(entry.getCard()).isSameAs(counterspell);
        assertThat(entry.getTargetId()).isEqualTo(turtle.getId());
    }

    @Test
    @DisplayName("Resolving counters a creature spell")
    void countersCreatureSpell() {
        HornedTurtle turtle = new HornedTurtle();
        harness.setHand(player1, List.of(turtle));
        harness.addMana(player1, ManaColor.BLUE, 3);

        Counterspell counterspell = new Counterspell();
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, turtle.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(turtle.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(turtle.getId()));
    }

    @Test
    @DisplayName("Resolving counters a non-creature spell")
    void countersNonCreatureSpell() {
        HornedTurtle turtle = new HornedTurtle();
        var turtlePermanent = harness.addToBattlefieldAndReturn(player1, turtle);

        Capsize capsize = new Capsize();
        harness.setHand(player1, List.of(capsize));
        harness.addMana(player1, ManaColor.BLUE, 3);

        Counterspell counterspell = new Counterspell();
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, turtlePermanent.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, capsize.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(capsize.getId()));
        assertThat(gd.stack)
                .noneMatch(se -> se.getCard().getId().equals(capsize.getId()));
    }

    @Test
    @DisplayName("Puts a spell controlled by another player into its owner's graveyard")
    @CardUsed(Commandeer.class)
    void putsControlledSpellIntoOwnersGraveyard() {
        HornedTurtle turtle = new HornedTurtle();
        var turtlePermanent = harness.addToBattlefieldAndReturn(player1, turtle);

        Capsize capsize = new Capsize();
        Counterspell counterspell = new Counterspell();
        harness.setHand(player1, List.of(capsize, counterspell));
        harness.addMana(player1, ManaColor.BLUE, 5);

        Commandeer commandeer = new Commandeer();
        harness.setHand(player2, List.of(commandeer, new Counterspell(), new Counterspell()));

        harness.castInstant(player1, 0, turtlePermanent.getId());
        harness.passPriority(player1);
        harness.castInstantWithAlternateExileFromHand(player2, 0, capsize.getId(), List.of(1, 2));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.castInstant(player1, 0, capsize.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(capsize.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(capsize.getId()));
    }

    @Test
    @DisplayName("Fizzles if target spell is no longer on the stack")
    void fizzlesIfTargetRemoved() {
        HornedTurtle turtle = new HornedTurtle();
        harness.setHand(player1, List.of(turtle));
        harness.addMana(player1, ManaColor.BLUE, 3);

        Counterspell counterspell = new Counterspell();
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, turtle.getId());

        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getId().equals(turtle.getId()));

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(counterspell.getId()));
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        var turtle = harness.addToBattlefieldAndReturn(player1, new HornedTurtle());
        Counterspell counterspell = new Counterspell();
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, turtle.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spell on the stack");

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(counterspell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target an activated ability")
    void cannotTargetActivatedAbility() {
        Permanent hunter = addCreatureReady(player1, new RootwaterHunter());
        harness.activateAbility(player1, 0, null, player2.getId());

        Counterspell counterspell = new Counterspell();
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, hunter.getCard().getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spell on the stack");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(counterspell);
    }
}
