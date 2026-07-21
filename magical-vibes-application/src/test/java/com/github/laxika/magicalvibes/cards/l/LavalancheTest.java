package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LavalancheTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to target player and each creature that player controls")
    void dealsXDamageToPlayerAndTheirCreatures() {
        harness.setHand(player1, List.of(new Lavalanche()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2, dies to 3
        harness.addToBattlefield(player2, new GiantSpider());   // 2/4, survives 3

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        // Target player takes X (=3) damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        // The 2/2 dies to 3 damage
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // The 2/4 survives 3 damage
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Giant Spider"));
    }

    @Test
    @DisplayName("Does not damage the caster's own creatures")
    void doesNotDamageCastersCreatures() {
        harness.setHand(player1, List.of(new Lavalanche()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        // Caster's creature is unharmed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }
}
