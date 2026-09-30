package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwarmSaboteur.class})
class SwarmSaboteurTest extends BaseCardTest {

    @Test
    void combatDamageConjuresVirusBeetleIntoHand() {
        Permanent saboteur = addCreatureReady(player1, new SwarmSaboteur());
        saboteur.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .anySatisfy(card -> {
                    assertThat(card.getName()).isEqualTo("Virus Beetle");
                    assertThat(card.isToken()).isFalse();
                });
    }

    @Test
    void blockedSaboteurDoesNotConjureVirusBeetle() {
        harness.setHand(player1, List.of());
        Permanent saboteur = addCreatureReady(player1, new SwarmSaboteur());
        saboteur.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SwarmSaboteur());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
