package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.SafeholdElite;
import com.github.laxika.magicalvibes.cards.b.BriarberryCohort;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Aethertow.class, SafeholdElite.class, BriarberryCohort.class})
class AethertowTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving puts target attacking creature on top of its owner's library")
    void resolvingPutsAttackerOnTopOfLibrary() {
        Permanent attacker = addAttacker(player2);
        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        castAethertow(attacker.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Safehold Elite");
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Safehold Elite");
    }

    @Test
    @DisplayName("Resolving puts target blocking creature on top of its owner's library")
    void resolvingPutsBlockerOnTopOfLibrary() {
        Permanent blocker = addBlocker(player2);

        castAethertow(blocker.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Safehold Elite");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName())
                .isEqualTo("Safehold Elite");
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        harness.addToBattlefield(player2, new SafeholdElite());
        UUID targetId = harness.getPermanentId(player2, "Safehold Elite");

        harness.setHand(player1, List.of(new Aethertow()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the attacker leaves combat before resolution")
    void fizzlesIfTargetRemovedBeforeResolution() {
        Permanent attacker = addAttacker(player2);
        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        castAethertow(attacker.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    void fizzlesWhenTargetStopsAttackingButRemainsOnBattlefield() {
        Permanent attacker = addAttacker(player2);
        int librarySize = gd.playerDecks.get(player2.getId()).size();

        castAethertow(attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Safehold Elite");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySize);
        harness.assertInGraveyard(player1, "Aethertow");
    }

    @Test
    void canTargetOwnAttackingCreatureAndPayWithBlueMana() {
        Permanent attacker = addAttacker(player1);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new Aethertow()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0, attacker.getId());

        harness.assertNotOnBattlefield(player1, "Safehold Elite");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(attacker.getCard());
    }

    @Test
    void stolenAttackerReturnsToOwnersLibrary() {
        Permanent attacker = addAttacker(player2);
        gd.stolenCreatures.put(attacker.getId(), player1.getId());
        int controllerLibrarySize = gd.playerDecks.get(player2.getId()).size();

        castAethertow(attacker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Safehold Elite");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(attacker.getCard());
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(controllerLibrarySize);
    }

    @Test
    void conspireWithWhiteAndBlueCreaturesCanRetargetCopy() {
        Permanent attacker = addAttacker(player2);
        Permanent blocker = addBlocker(player2);
        Permanent whiteCreature = harness.addToBattlefieldAndReturn(player1, new SafeholdElite());
        Permanent blueCreature = harness.addToBattlefieldAndReturn(player1, new BriarberryCohort());
        harness.setHand(player1, List.of(new Aethertow()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castWithConspire(player1, 0, attacker.getId(),
                List.of(whiteCreature.getId(), blueCreature.getId()));
        assertThat(whiteCreature.isTapped()).isTrue();
        assertThat(blueCreature.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, blocker.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()).subList(0, 2))
                .containsExactly(attacker.getCard(), blocker.getCard());
        harness.assertInGraveyard(player1, "Aethertow");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Aethertow")).hasSize(1);
    }

    @Test
    void conspireCanKeepOriginalTargetAndOriginalThenFizzles() {
        Permanent attacker = addAttacker(player2);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SafeholdElite());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SafeholdElite());
        int librarySize = gd.playerDecks.get(player2.getId()).size();
        harness.setHand(player1, List.of(new Aethertow()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castWithConspire(player1, 0, attacker.getId(), List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySize + 1);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(attacker.getCard());
        harness.assertInGraveyard(player1, "Aethertow");
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    private void castAethertow(UUID targetId) {
        harness.setHand(player1, List.of(new Aethertow()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, targetId);
    }

    private Permanent addAttacker(Player owner) {
        Permanent attacker = addCreatureReady(owner, new SafeholdElite());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }

    private Permanent addBlocker(Player owner) {
        Permanent blocker = addCreatureReady(owner, new SafeholdElite());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(UUID.randomUUID());
        return blocker;
    }
}
