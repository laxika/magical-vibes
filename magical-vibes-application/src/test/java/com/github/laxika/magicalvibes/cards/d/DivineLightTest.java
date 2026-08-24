package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({DivineLight.class, GrizzlyBears.class, Shock.class})
class DivineLightTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all damage to creatures you control this turn")
    void preventsDamageToControlledCreatures() {
        castDivineLight();
        Permanent bears = addCreature(player1);

        shock(player2, bears.getId());

        assertThat(bears.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not prevent damage to an opponent's creature")
    void doesNotPreventDamageToOpponentsCreature() {
        Permanent bears = addCreature(player2);
        castDivineLight();

        shock(player1, bears.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not prevent damage to players")
    void doesNotPreventDamageToPlayers() {
        harness.setLife(player1, 20);
        castDivineLight();

        shock(player2, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Prevention wears off at end of turn")
    void preventionWearsOffAtEndOfTurn() {
        Permanent bears = addCreature(player1);
        castDivineLight();

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);
        shock(player2, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void castDivineLight() {
        harness.setHand(player1, java.util.List.of(new DivineLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.castSorcery(player1, 0, java.util.List.of());
        harness.passBothPriorities();
    }

    private void shock(Player caster, java.util.UUID targetId) {
        harness.setHand(caster, java.util.List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.forceActivePlayer(caster);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }

    private Permanent addCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }
}
