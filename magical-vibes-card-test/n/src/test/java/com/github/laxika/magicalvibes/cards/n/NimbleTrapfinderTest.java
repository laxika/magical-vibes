package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FaerieMiscreant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BoggartBrute;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
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
        GrizzlyBears.class, BoggartBrute.class, SoulWarden.class, TurnToFrog.class})
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


    @Test
    @DisplayName("Its own entry does not prevent blocking")
    void ownEntryDoesNotPreventBlocking() {
        Permanent trapfinder = harness.enterBattlefieldAndReturn(player1, new NimbleTrapfinder());
        trapfinder.setSummoningSick(false);
        assertCanBeBlocked(trapfinder);
    }

    @Test
    @DisplayName("An opponent's qualifying entry does not prevent blocking")
    void opponentEntryDoesNotPreventBlocking() {
        Permanent trapfinder = addReadyCreature(player1, new NimbleTrapfinder());
        harness.enterBattlefieldAndReturn(player2, new NimbleTrapfinder());
        assertCanBeBlocked(trapfinder);
    }

    @Test
    @DisplayName("A qualifying entry still counts after that creature leaves")
    void qualifyingEntryStillCountsAfterCreatureLeaves() {
        Permanent trapfinder = addReadyCreature(player1, new NimbleTrapfinder());
        Permanent otherRogue = harness.enterBattlefieldAndReturn(player1, new NimbleTrapfinder());
        gd.playerBattlefields.get(player1.getId()).remove(otherRogue);
        harness.setGraveyard(player1, List.of(otherRogue.getCard()));
        trapfinder.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        prepareBlockerDeclaration();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(trapfinder)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Losing the full party after resolution does not remove the draw ability")
    void drawAbilityRemainsAfterPartyIsLost() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        Permanent trapfinder = addReadyCreature(player1, new NimbleTrapfinder());
        Permanent otherCreature = addReadyCreature(player1, new GrizzlyBears());
        addFullParty();
        advanceToCombat(player1);
        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent != trapfinder && permanent != otherCreature);
        trapfinder.setAttacking(true);
        otherCreature.setAttacking(true);
        resolveUnblockedCombat();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Creatures entering after resolution do not gain the draw ability")
    void lateCreatureDoesNotGainDrawAbility() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        Permanent trapfinder = addReadyCreature(player1, new NimbleTrapfinder());
        addFullParty();
        advanceToCombat(player1);
        Permanent lateCreature = harness.enterBattlefieldAndReturn(player1, new NimbleTrapfinder());
        lateCreature.setSummoningSick(false);
        lateCreature.setAttacking(true);
        trapfinder.setAttacking(true);
        resolveUnblockedCombat();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }


    @Test
    @DisplayName("A later ability-removal effect removes the granted combat-damage draw ability")
    void laterAbilityRemovalPreventsDraw() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        addReadyCreature(player1, new NimbleTrapfinder());
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        addFullParty();
        advanceToCombat(player1);

        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, attacker.getId());
        attacker.setAttacking(true);
        resolveUnblockedCombat();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The full party must still exist when the combat trigger resolves")
    void partyLostBeforeResolutionDoesNotGrantDraw() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent trapfinder = addReadyCreature(player1, new NimbleTrapfinder());
        addFullParty();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard() instanceof FugitiveWizard);
        harness.passBothPriorities();
        trapfinder.setAttacking(true);
        resolveUnblockedCombat();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void assertCanBeBlocked(Permanent trapfinder) {
        trapfinder.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        prepareBlockerDeclaration();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(trapfinder)))))
                .doesNotThrowAnyException();
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
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
    }

    private void resolveUnblockedCombat() {
        prepareBlockerDeclaration();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, card);
        creature.setSummoningSick(false);
        return creature;
    }
}
