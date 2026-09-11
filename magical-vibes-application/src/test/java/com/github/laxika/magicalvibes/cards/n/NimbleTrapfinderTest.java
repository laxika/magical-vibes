package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FaerieMiscreant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BoggartBrute;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NimbleTrapfinder.class, FaerieMiscreant.class, Forest.class, FugitiveWizard.class,
        GrizzlyBears.class, BoggartBrute.class, SoulWarden.class})
class NimbleTrapfinderTest extends BaseCardTest {

    @Test
    @DisplayName("Can be blocked before a party creature enters this turn")
    void canBeBlockedBeforePartyCreatureEnters() {
        Permanent trapfinder = addReadyCreature(player1, new NimbleTrapfinder());
        trapfinder.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());

        prepareBlockerDeclaration();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(trapfinder)))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Can't be blocked after a party creature enters under its controller's control")
    void cantBeBlockedAfterPartyCreatureEnters() {
        Permanent trapfinder = addReadyCreature(player1, new NimbleTrapfinder());
        trapfinder.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new FugitiveWizard());

        prepareBlockerDeclaration();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(trapfinder)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Does not become unblockable when a nonparty creature enters")
    void doesNotBecomeUnblockableForNonpartyCreature() {
        Permanent trapfinder = addReadyCreature(player1, new NimbleTrapfinder());
        trapfinder.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        prepareBlockerDeclaration();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(trapfinder)))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A full party grants a combat-damage draw trigger at beginning of combat")
    void fullPartyDrawsOnCombatDamage() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        Permanent trapfinder = addReadyCreature(player1, new NimbleTrapfinder());
        Permanent otherCreature = addReadyCreature(player1, new GrizzlyBears());
        addFullParty();
        trapfinder.setAttacking(true);
        otherCreature.setAttacking(true);

        advanceToCombat(player1);
        resolveUnblockedCombat();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not grant the combat-damage draw trigger without a full party")
    void doesNotDrawWithoutFullParty() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent trapfinder = addReadyCreature(player1, new NimbleTrapfinder());
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new FaerieMiscreant());
        trapfinder.setAttacking(true);

        advanceToCombat(player1);
        resolveUnblockedCombat();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void addFullParty() {
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new FaerieMiscreant());
        harness.addToBattlefield(player1, new BoggartBrute());
        harness.addToBattlefield(player1, new FugitiveWizard());
    }

    private void prepareBlockerDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void resolveUnblockedCombat() {
        prepareBlockerDeclaration();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }
}
