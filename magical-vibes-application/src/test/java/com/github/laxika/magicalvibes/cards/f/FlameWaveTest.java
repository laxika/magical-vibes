package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlameWaveTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to target player and each creature that player controls")
    void deals4DamageToPlayerAndTheirCreatures() {
        harness.setHand(player1, List.of(new FlameWave()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Target player takes 4 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        // Both 2/2 bears die to 4 damage
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears"))
                .count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not damage the caster's own creatures")
    void doesNotDamageCastersCreatures() {
        harness.setHand(player1, List.of(new FlameWave()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Caster's creature is unharmed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }
}
