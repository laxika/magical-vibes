package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CinderPyromancer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiritLoop.class, GrizzlyBears.class, CinderPyromancer.class})
class SpiritLoopTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature's combat damage causes its controller to gain that much life")
    void gainsLifeFromCombatDamage() {
        harness.setLife(player1, 10);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachSpiritLoop(player1, creature);
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Enchanted creature's noncombat damage causes its controller to gain that much life")
    void gainsLifeFromNoncombatDamage() {
        harness.setLife(player1, 10);
        Permanent creature = addCreatureReady(player1, new CinderPyromancer());
        attachSpiritLoop(player1, creature);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
    }

    @Test
    @DisplayName("Spirit Loop returns to its owner's hand when put into a graveyard from the battlefield")
    void returnsToHandAfterLeavingBattlefieldForGraveyard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = new Permanent(new SpiritLoop());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Spirit Loop");
        harness.assertNotInGraveyard(player1, "Spirit Loop");
        harness.assertNotOnBattlefield(player1, "Spirit Loop");
    }

    @Test
    @DisplayName("Spirit Loop cannot target a creature controlled by an opponent")
    void cannotTargetOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpiritLoop()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    private void attachSpiritLoop(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new SpiritLoop());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
    }
}
