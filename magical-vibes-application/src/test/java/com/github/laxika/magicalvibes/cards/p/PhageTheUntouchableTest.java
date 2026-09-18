package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BeaconOfUnrest;
import com.github.laxika.magicalvibes.cards.m.MahamotiDjinn;
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

@CardUsed({PhageTheUntouchable.class, BeaconOfUnrest.class, MahamotiDjinn.class})
class PhageTheUntouchableTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Phage from hand does not make its controller lose the game")
    void castFromHandDoesNotLoseGame() {
        harness.setHand(player1, List.of(new PhageTheUntouchable()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castCreature(player1, 0);
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

        harness.castSorcery(player1, 0, 0, target.getId());
        harness.passBothPriorities();

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
}
