package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GoblinLegionnaire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DivineLight.class, GoblinLegionnaire.class})
class DivineLightTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all damage to creatures you control this turn")
    void preventsDamageToControlledCreatures() {
        castDivineLight();
        Permanent bears = addCreatureReady(player1, new GoblinLegionnaire());

        dealDamage(player2, bears.getId());

        assertThat(bears.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Goblin Legionnaire");
    }

    @Test
    @DisplayName("Does not prevent damage to an opponent's creature")
    void doesNotPreventDamageToOpponentsCreature() {
        Permanent bears = addCreatureReady(player2, new GoblinLegionnaire());
        castDivineLight();

        dealDamage(player1, bears.getId());

        harness.assertInGraveyard(player2, "Goblin Legionnaire");
    }

    @Test
    @DisplayName("Does not prevent damage to players")
    void doesNotPreventDamageToPlayers() {
        harness.setLife(player1, 20);
        castDivineLight();

        dealDamage(player2, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Prevention wears off at end of turn")
    void preventionWearsOffAtEndOfTurn() {
        Permanent bears = addCreatureReady(player1, new GoblinLegionnaire());
        castDivineLight();

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);
        dealDamage(player2, bears.getId());

        harness.assertInGraveyard(player1, "Goblin Legionnaire");
    }

    @Test
    @DisplayName("Prevents combat damage to creatures you control")
    void preventsCombatDamageToControlledCreatures() {
        Permanent attacker = addCreatureReady(player2, new GoblinLegionnaire());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player1, new GoblinLegionnaire());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        castDivineLight();
        resolveCombat(player2);

        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Goblin Legionnaire");
        harness.assertInGraveyard(player2, "Goblin Legionnaire");
    }

    private void castDivineLight() {
        harness.castFromHand(player1, new DivineLight(), "{W}");
        harness.passBothPriorities();
    }

    private void dealDamage(Player caster, java.util.UUID targetId) {
        Permanent source = addCreatureReady(caster, new GoblinLegionnaire());
        harness.addMana(caster, ManaColor.RED, 1);
        harness.forceActivePlayer(caster);
        int sourceIndex = gd.playerBattlefields.get(caster.getId()).indexOf(source);
        harness.activateAbility(caster, sourceIndex, 0, null, targetId);
        harness.passBothPriorities();
    }
}
