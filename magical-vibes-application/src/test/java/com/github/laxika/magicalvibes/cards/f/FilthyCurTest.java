package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CephalidScout;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FilthyCur.class, Firebolt.class, CephalidScout.class})
class FilthyCurTest extends BaseCardTest {

    @Test
    @DisplayName("Non-combat damage makes Filthy Cur's controller lose that much life")
    void nonCombatDamageMakesControllerLoseLife() {
        Permanent cur = harness.addToBattlefieldAndReturn(player2, new FilthyCur());
        harness.setHand(player1, List.of(new Firebolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, cur.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player2, "Filthy Cur");
    }

    @Test
    @DisplayName("Combat damage makes Filthy Cur's controller lose that much life")
    void combatDamageMakesControllerLoseLife() {
        Permanent attacker = addCreatureReady(player1, new CephalidScout());
        Permanent cur = addCreatureReady(player2, new FilthyCur());
        harness.setLife(player2, 20);

        attacker.setAttacking(true);

        cur.setBlocking(true);
        cur.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertOnBattlefield(player2, "Filthy Cur");
    }
}
