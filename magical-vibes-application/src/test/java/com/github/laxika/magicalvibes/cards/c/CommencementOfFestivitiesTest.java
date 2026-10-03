package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CommencementOfFestivities.class, GrizzlyBears.class, Shock.class})
class CommencementOfFestivitiesTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage that would be dealt to players")
    void preventsCombatDamageToPlayers() {
        harness.setLife(player1, 20);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        castCommencementOfFestivities();
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not prevent combat damage dealt to creatures")
    void doesNotPreventCombatDamageToCreatures() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        castCommencementOfFestivities();
        resolveCombat(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Does not prevent noncombat damage to players")
    void doesNotPreventNoncombatDamageToPlayers() {
        harness.setLife(player1, 20);
        castCommencementOfFestivities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Also prevents combat damage to the caster's opponent")
    void preventsCombatDamageToOpponent() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        castCommencementOfFestivities();
        resolveCombat(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Combat damage prevention expires at the end of the turn")
    void preventionExpiresAtEndOfTurn() {
        harness.setLife(player1, 20);
        castCommencementOfFestivities();
        harness.passUntil(player2, TurnStep.UPKEEP);

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    private void castCommencementOfFestivities() {
        harness.setHand(player1, List.of(new CommencementOfFestivities()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);
    }
}
