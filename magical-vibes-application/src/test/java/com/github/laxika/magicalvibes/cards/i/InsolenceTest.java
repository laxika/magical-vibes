package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InsolenceTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot enchant a non-creature permanent")
    void cannotEnchantNonCreature() {
        Permanent land = new Permanent(new Mountain());
        gd.playerBattlefields.get(player2.getId()).add(land);
        harness.setHand(player1, List.of(new Insolence()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Tapping the enchanted creature deals 2 damage to its controller")
    void tappingEnchantedCreatureDamagesController() {
        Permanent creature = addCreatureWithAura(player2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.tapPermanent(player2, 0);
        resolveStackFully();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping an un-enchanted creature does not deal damage")
    void tappingUnenchantedCreatureDoesNotDealDamage() {
        addCreatureReady(player2, new LlanowarElves());
        harness.setLife(player2, 20);

        harness.tapPermanent(player2, 0);
        resolveStackFully();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private Permanent addCreatureWithAura(Player creatureController) {
        Permanent creature = addCreatureReady(creatureController, new LlanowarElves());
        Permanent aura = new Permanent(new Insolence());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return creature;
    }

    private void resolveStackFully() {
        for (int i = 0; i < 8 && (!gd.stack.isEmpty() || !gd.pendingManaAbilityTriggers.isEmpty()); i++) {
            harness.passBothPriorities();
        }
    }
}
