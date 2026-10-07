package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TelJiladDefiance.class, CarapaceForger.class, GoldMyr.class, IronMyr.class})
class TelJiladDefianceTest extends BaseCardTest {

    

    @Test
    @DisplayName("Resolving Tel-Jilad Defiance grants protection from artifacts and draws a card")
    void grantsProtectionAndDrawsCard() {
        harness.addToBattlefield(player1, new CarapaceForger());
        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new TelJiladDefiance()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID targetId = harness.getPermanentId(player1, "Carapace Forger");
        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();

        // Protection from artifacts was granted
        Permanent bears = findPermanent(player1, "Carapace Forger");
        assertThat(bears.getProtectionFromCardTypes()).contains(CardType.ARTIFACT);

        // Drew a card
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Cannot target a non-creature with Tel-Jilad Defiance")
    void cannotTargetNonCreature() {
        harness.setHand(player1, List.of(new TelJiladDefiance()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles and does not draw when target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player1, new CarapaceForger());
        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new TelJiladDefiance()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID targetId = harness.getPermanentId(player1, "Carapace Forger");
        harness.castInstant(player1, 0, targetId);

        // Remove the target before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Protection from artifacts prevents blocking by artifact creature")
    void protectionPreventsBlockingByArtifactCreature() {
        // Set up: Carapace Forger has protection from artifacts
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new CarapaceForger());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.getProtectionFromCardTypes().add(CardType.ARTIFACT);

        // Iron Myr is an artifact creature
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new IronMyr());
        blocker.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from artifacts allows blocking by non-artifact creature")
    void protectionAllowsBlockingByNonArtifactCreature() {
        // Set up: Carapace Forger #1 has protection from artifacts
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new CarapaceForger());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.getProtectionFromCardTypes().add(CardType.ARTIFACT);

        // Carapace Forger #2 is a non-artifact creature — can block
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new CarapaceForger());
        blocker.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Protection from artifacts prevents combat damage from artifact creature")
    void protectionPreventsCombatDamageFromArtifactCreature() {
        // Iron Myr (1/1 artifact creature) attacks, Carapace Forger blocks with protection from artifacts
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new IronMyr());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new CarapaceForger());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        blocker.getProtectionFromCardTypes().add(CardType.ARTIFACT);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Iron Myr's 1 damage to Carapace Forger is prevented (protection).
        // Carapace Forger' 2 damage kills Iron Myr (1/1)
        harness.assertOnBattlefield(player2, "Carapace Forger");
        harness.assertNotOnBattlefield(player1, "Iron Myr");
    }

    @Test
    @DisplayName("Protection from artifacts is cleared at end of turn")
    void protectionClearedAtEndOfTurn() {
        harness.addToBattlefield(player1, new CarapaceForger());
        harness.setHand(player1, List.of(new TelJiladDefiance()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID targetId = harness.getPermanentId(player1, "Carapace Forger");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent bears = findPermanent(player1, "Carapace Forger");
        assertThat(bears.getProtectionFromCardTypes()).contains(CardType.ARTIFACT);

        // Simulate end of turn cleanup
        bears.resetModifiers();
        assertThat(bears.getProtectionFromCardTypes()).isEmpty();
    }

    @Test
    @DisplayName("An opponent's artifact creature can gain protection while the caster draws")
    void canTargetOpponentsArtifactCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoldMyr());
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();
        harness.setHand(player1, List.of(new TelJiladDefiance()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getProtectionFromCardTypes()).contains(CardType.ARTIFACT);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
        harness.assertInGraveyard(player1, "Tel-Jilad Defiance");
    }

    @Test
    @DisplayName("Already having protection from artifacts does not stop the spell or its draw")
    void drawsWhenTargetAlreadyHasProtection() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GoldMyr());
        target.getProtectionFromCardTypes().add(CardType.ARTIFACT);
        harness.setHand(player1, List.of(new TelJiladDefiance()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getProtectionFromCardTypes()).contains(CardType.ARTIFACT);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Tel-Jilad Defiance");
    }

    @Test
    @DisplayName("Turn progression removes the granted protection during cleanup")
    void protectionExpiresThroughTurnProgression() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CarapaceForger());
        harness.setHand(player1, List.of(new TelJiladDefiance()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(target.getProtectionFromCardTypes()).contains(CardType.ARTIFACT);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.getProtectionFromCardTypes()).doesNotContain(CardType.ARTIFACT);
        harness.assertOnBattlefield(player1, "Carapace Forger");
    }
}
