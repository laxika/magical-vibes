package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReciprocateTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature that dealt damage to you this turn")
    void exilesCreatureThatDealtDamageToYou() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        gd.combatDamageToPlayersThisTurn
                .computeIfAbsent(bears.getId(), k -> ConcurrentHashMap.newKeySet())
                .add(player1.getId());

        castReciprocate(player1, bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target a creature that dealt damage only to another player")
    void cannotTargetCreatureThatDealtDamageToAnotherPlayer() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        gd.combatDamageToPlayersThisTurn
                .computeIfAbsent(bears.getId(), k -> ConcurrentHashMap.newKeySet())
                .add(player2.getId());

        assertThatThrownBy(() -> castReciprocate(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature that dealt no damage this turn")
    void cannotTargetCreatureThatDealtNoDamage() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> castReciprocate(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castReciprocate(com.github.laxika.magicalvibes.model.Player caster, UUID targetId) {
        harness.setHand(caster, List.of(new Reciprocate()));
        harness.addMana(caster, ManaColor.WHITE, 1);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }
}
