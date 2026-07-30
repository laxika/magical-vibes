package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinGrenadeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Goblin deals 5 damage to target player")
    void dealsFiveDamageToPlayer() {
        Permanent goblin = new Permanent(new GoblinPiker());
        gd.playerBattlefields.get(player1.getId()).add(goblin);

        harness.setHand(player1, List.of(new GoblinGrenade()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), goblin.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        harness.assertInGraveyard(player1, "Goblin Piker");
        harness.assertInGraveyard(player1, "Goblin Grenade");
    }

    @Test
    @DisplayName("Sacrificing a Goblin deals 5 damage to target creature")
    void dealsFiveDamageToCreature() {
        Permanent goblin = new Permanent(new GoblinPiker());
        gd.playerBattlefields.get(player1.getId()).add(goblin);

        Permanent target = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new GoblinGrenade()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), goblin.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot sacrifice a non-Goblin creature")
    void cannotSacrificeNonGoblin() {
        Permanent elves = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player1.getId()).add(elves);

        harness.setHand(player1, List.of(new GoblinGrenade()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, player2.getId(), elves.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
