package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BeaconOfUnrest;
import com.github.laxika.magicalvibes.cards.f.FutureSight;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.m.MahamotiDjinn;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhageTheUntouchable.class, BeaconOfUnrest.class, MahamotiDjinn.class,
        FutureSight.class, HolyDay.class, Unsummon.class})
class PhageTheUntouchableTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Phage from hand does not make its controller lose the game")
    void castFromHandDoesNotLoseGame() {
        harness.castFromHand(player1, new PhageTheUntouchable(), "{3}{B}{B}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        harness.assertOnBattlefield(player1, "Phage the Untouchable");
    }

    @Test
    @DisplayName("Entering from graveyard causes controller to lose the game")
    void enteringWithoutCastingFromHandLosesGame() {
        PhageTheUntouchable target = new PhageTheUntouchable();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("loses the game from Phage the Untouchable"));
    }

    @Test
    @DisplayName("Combat damage to player makes that player lose the game")
    void combatDamageToPlayerLosesGame() {
        Permanent phage = addCreatureReady(player1, new PhageTheUntouchable());
        phage.setAttacking(true);

        resolveCombat();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("Bob loses the game from Phage the Untouchable"));
    }

    @Test
    @DisplayName("Combat damage to a creature destroys that creature")
    void combatDamageToCreatureDestroysCreature() {
        Permanent phage = addCreatureReady(player1, new PhageTheUntouchable());
        phage.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new MahamotiDjinn());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mahamoti Djinn");
        harness.assertNotOnBattlefield(player2, "Mahamoti Djinn");
    }

    @Test
    @DisplayName("Combat damage to a creature ignores its regeneration shield")
    void combatDamageToCreatureCannotBeRegenerated() {
        Permanent phage = addCreatureReady(player1, new PhageTheUntouchable());
        phage.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new MahamotiDjinn());
        blocker.setRegenerationShield(1);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mahamoti Djinn");
        harness.assertNotOnBattlefield(player2, "Mahamoti Djinn");
    }

    @Test
    @DisplayName("Casting Phage from the library makes its controller lose")
    void castingFromLibraryLosesGame() {
        harness.addToBattlefield(player1, new FutureSight());
        harness.setLibrary(player1, List.of(new PhageTheUntouchable()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveFromLibraryTop(player1);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("Alice loses the game from Phage the Untouchable"));
    }

    @Test
    @DisplayName("Reanimating an opponent's Phage makes its new controller lose")
    void reanimatingOpponentsPhageLosesGame() {
        PhageTheUntouchable phage = new PhageTheUntouchable();
        harness.setGraveyard(player2, List.of(phage));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0, phage.getId());
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("Alice loses the game from Phage the Untouchable"));
    }

    @Test
    @DisplayName("Returning reanimated Phage to hand does not stop its loss trigger")
    void returningPhageToHandDoesNotStopLossTrigger() {
        PhageTheUntouchable phage = new PhageTheUntouchable();
        harness.setGraveyard(player1, List.of(phage));
        harness.setHand(player1, List.of(new BeaconOfUnrest(), new Unsummon()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, 0, phage.getId());
        Permanent permanent = findPermanent(player1, "Phage the Untouchable");
        harness.castAndResolveInstant(player1, 0, permanent.getId());
        harness.assertNotOnBattlefield(player1, "Phage the Untouchable");
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("Alice loses the game from Phage the Untouchable"));
    }

    @Test
    @DisplayName("Prevented combat damage to a player does not cause a loss")
    void preventedCombatDamageToPlayerDoesNotLoseGame() {
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new HolyDay(), "{W}");
        harness.passBothPriorities();
        Permanent phage = addCreatureReady(player1, new PhageTheUntouchable());
        phage.setAttacking(true);

        resolveCombat();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Prevented combat damage to a creature does not destroy it")
    void preventedCombatDamageToCreatureDoesNotDestroyIt() {
        harness.castFromHand(player1, new HolyDay(), "{W}");
        harness.passBothPriorities();
        Permanent phage = addCreatureReady(player1, new PhageTheUntouchable());
        phage.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new MahamotiDjinn());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Phage the Untouchable");
        harness.assertOnBattlefield(player2, "Mahamoti Djinn");
        assertThat(phage.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }
}
