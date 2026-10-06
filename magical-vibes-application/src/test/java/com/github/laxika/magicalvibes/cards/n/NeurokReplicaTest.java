package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NeurokReplica.class, CopperMyr.class, Island.class})
class NeurokReplicaTest extends BaseCardTest {


    @Test
    @DisplayName("Activating ability sacrifices Neurok Replica and puts ability on the stack")
    void activatingAbilitySacrificesAndPutsOnStack() {
        addReadyReplica(player1);
        Permanent target = addCreatureReady(player2, new CopperMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        // Neurok Replica should be sacrificed immediately (cost)
        harness.assertNotOnBattlefield(player1, "Neurok Replica");
        harness.assertInGraveyard(player1, "Neurok Replica");

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Neurok Replica");
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }


    @Test
    @DisplayName("Resolving ability returns target creature to its owner's hand")
    void resolvingAbilityReturnsCreatureToHand() {
        addReadyReplica(player1);
        Permanent target = addCreatureReady(player2, new CopperMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        // Target creature should be back in owner's hand
        harness.assertNotOnBattlefield(player2, "Copper Myr");
        harness.assertInHand(player2, "Copper Myr");
    }

    @Test
    @DisplayName("Bounced creature does not go to graveyard")
    void bouncedCreatureDoesNotGoToGraveyard() {
        addReadyReplica(player1);
        Permanent target = addCreatureReady(player2, new CopperMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Copper Myr");
    }

    @Test
    @DisplayName("Can target own creature")
    void canTargetOwnCreature() {
        addReadyReplica(player1);
        Permanent target = addCreatureReady(player1, new CopperMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Copper Myr");
        harness.assertInHand(player1, "Copper Myr");
    }


    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyReplica(player1);
        Permanent target = addCreatureReady(player2, new CopperMyr());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with only colorless mana (needs blue)")
    void cannotActivateWithOnlyColorlessMana() {
        addReadyReplica(player1);
        Permanent target = addCreatureReady(player2, new CopperMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    @DisplayName("Can activate with summoning sickness (no tap cost)")
    void canActivateWithSummoningSickness() {
        NeurokReplica card = new NeurokReplica();
        harness.addToBattlefield(player1, card);
        Permanent target = addCreatureReady(player2, new CopperMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
    }


    @Test
    @DisplayName("Cannot target non-creature permanent")
    void cannotTargetNonCreaturePermanent() {
        addReadyReplica(player1);
        Permanent land = addReadyLand(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    @DisplayName("Ability fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyReplica(player1);
        Permanent target = addCreatureReady(player2, new CopperMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Copper Myr"));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }


    @Test
    @DisplayName("Returns a controlled creature to its owner rather than its controller")
    void returnsCreatureToOwner() {
        addReadyReplica(player1);
        CopperMyr card = new CopperMyr();
        card.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, card);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Copper Myr");
        harness.assertNotInHand(player1, "Copper Myr");
        harness.assertInHand(player2, "Copper Myr");
    }

    @Test
    @DisplayName("Can target itself, but sacrificing it leaves the ability without a legal target")
    void canTargetItself() {
        Permanent replica = addReadyReplica(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, replica.getId());

        harness.assertInGraveyard(player1, "Neurok Replica");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Neurok Replica");
        harness.assertNotInHand(player1, "Neurok Replica");
    }

    @Test
    @DisplayName("Can activate while tapped because the ability has no tap cost")
    void canActivateWhileTapped() {
        Permanent replica = addReadyReplica(player1);
        replica.tap();
        Permanent target = addCreatureReady(player2, new CopperMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Neurok Replica");
        harness.assertInHand(player2, "Copper Myr");
    }

    private Permanent addReadyReplica(Player player) {
        return addCreatureReady(player, new NeurokReplica());
    }

    private Permanent addReadyLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Island());
    }
}
