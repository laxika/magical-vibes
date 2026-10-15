package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.cards.f.FledglingMawcor;
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

@CardUsed({SpiritLoop.class, BenalishCavalry.class, FledglingMawcor.class})
class SpiritLoopTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature's combat damage causes Spirit Loop's controller to gain that much life")
    void gainsLifeFromCombatDamage() {
        harness.setLife(player1, 10);
        Permanent creature = addCreatureReady(player1, new BenalishCavalry());
        attachSpiritLoop(player1, creature);
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("Enchanted creature's noncombat damage causes Spirit Loop's controller to gain that much life")
    void gainsLifeFromNoncombatDamage() {
        harness.setLife(player1, 10);
        Permanent creature = addCreatureReady(player1, new FledglingMawcor());
        attachSpiritLoop(player1, creature);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 11);
    }

    @Test
    @DisplayName("Enchanted creature's noncombat damage to a creature causes Spirit Loop's controller to gain that much life")
    void gainsLifeWhenNoncombatDamageHitsCreature() {
        harness.setLife(player1, 10);
        Permanent creature = addCreatureReady(player1, new FledglingMawcor());
        attachSpiritLoop(player1, creature);
        Permanent target = addCreatureReady(player2, new BenalishCavalry());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 11);
    }

    @Test
    @DisplayName("Spirit Loop can enchant a creature its controller controls")
    void canEnchantControlledCreature() {
        Permanent creature = addCreatureReady(player1, new BenalishCavalry());
        harness.setHand(player1, List.of(new SpiritLoop()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Spirit Loop");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Spirit Loop returns to its owner's hand when put into a graveyard from the battlefield")
    void returnsToHandAfterLeavingBattlefieldForGraveyard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BenalishCavalry());
        Permanent aura = attachSpiritLoop(player1, creature);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Spirit Loop");
        harness.assertNotInGraveyard(player1, "Spirit Loop");
        harness.assertNotOnBattlefield(player1, "Spirit Loop");
    }

    @Test
    @DisplayName("Spirit Loop cannot target a creature controlled by an opponent")
    void cannotTargetOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BenalishCavalry());
        harness.setHand(player1, List.of(new SpiritLoop()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Spirit Loop returns to its owner even when another player controlled it")
    void returnsToOwnerRatherThanController() {
        Permanent creature = addCreatureReady(player2, new BenalishCavalry());
        SpiritLoop card = new SpiritLoop();
        card.setOwnerId(player1.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, card);
        aura.setAttachedTo(creature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        resolveAllTriggers();

        harness.assertInHand(player1, "Spirit Loop");
        harness.assertNotInHand(player2, "Spirit Loop");
        harness.assertNotInGraveyard(player1, "Spirit Loop");
    }

    @Test
    @DisplayName("Spirit Loop returns after the enchanted creature dies")
    void returnsAfterEnchantedCreatureDies() {
        Permanent creature = addCreatureReady(player1, new BenalishCavalry());
        attachSpiritLoop(player1, creature);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Benalish Cavalry");
        harness.assertInHand(player1, "Spirit Loop");
        harness.assertNotOnBattlefield(player1, "Spirit Loop");
    }

    @Test
    @DisplayName("Each Spirit Loop triggers separately for the same damage")
    void multipleAurasEachGainLife() {
        harness.setLife(player1, 10);
        Permanent creature = addCreatureReady(player1, new FledglingMawcor());
        attachSpiritLoop(player1, creature);
        attachSpiritLoop(player1, creature);

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 19);
    }

    private Permanent attachSpiritLoop(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new SpiritLoop());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
