package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.c.Commandeer;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Counterspell.class, GrizzlyBears.class, Unsummon.class, ProdigalSorcerer.class})
class CounterspellTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack targeting a spell")
    void castingTargetsSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Counterspell counterspell = new Counterspell();
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry entry = gd.stack.getLast();
        assertThat(entry.getCard()).isSameAs(counterspell);
        assertThat(entry.getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Resolving counters a creature spell")
    void countersCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Counterspell counterspell = new Counterspell();
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Resolving counters a non-creature spell")
    void countersNonCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        var bearPermanent = harness.addToBattlefieldAndReturn(player1, bears);

        Unsummon unsummon = new Unsummon();
        harness.setHand(player1, List.of(unsummon));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Counterspell counterspell = new Counterspell();
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, bearPermanent.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, unsummon.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(unsummon.getId()));
        assertThat(gd.stack)
                .noneMatch(se -> se.getCard().getId().equals(unsummon.getId()));
    }

    @Test
    @DisplayName("Puts a spell controlled by another player into its owner's graveyard")
    @CardUsed(Commandeer.class)
    void putsControlledSpellIntoOwnersGraveyard() {
        GrizzlyBears bears = new GrizzlyBears();
        var bearPermanent = harness.addToBattlefieldAndReturn(player1, bears);

        Unsummon unsummon = new Unsummon();
        Counterspell counterspell = new Counterspell();
        harness.setHand(player1, List.of(unsummon, counterspell));
        harness.addMana(player1, ManaColor.BLUE, 3);

        Commandeer commandeer = new Commandeer();
        harness.setHand(player2, List.of(commandeer, new Counterspell(), new Counterspell()));

        harness.castInstant(player1, 0, bearPermanent.getId());
        harness.passPriority(player1);
        harness.castInstantWithAlternateExileFromHand(player2, 0, unsummon.getId(), List.of(1, 2));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.castInstant(player1, 0, unsummon.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(unsummon.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(unsummon.getId()));
    }

    @Test
    @DisplayName("Fizzles if target spell is no longer on the stack")
    void fizzlesIfTargetRemoved() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Counterspell counterspell = new Counterspell();
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getId().equals(bears.getId()));

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(counterspell.getId()));
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        var bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Counterspell counterspell = new Counterspell();
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spell on the stack");

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(counterspell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target an activated ability")
    void cannotTargetActivatedAbility() {
        Permanent sorcerer = addCreatureReady(player1, new ProdigalSorcerer());
        harness.activateAbility(player1, 0, null, player2.getId());

        Counterspell counterspell = new Counterspell();
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, sorcerer.getCard().getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spell on the stack");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(counterspell);
    }
}
