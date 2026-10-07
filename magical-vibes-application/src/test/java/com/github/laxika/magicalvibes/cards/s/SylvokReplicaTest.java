package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.t.TrueConviction;
import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.a.AccordersShield;
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

@CardUsed({SylvokReplica.class, AccordersShield.class, TrueConviction.class,
        CarapaceForger.class, Island.class, DarksteelMyr.class})
class SylvokReplicaTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability sacrifices Sylvok Replica and puts ability on the stack")
    void activatingAbilitySacrificesAndPutsOnStack() {
        addReadyReplica(player1);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        // Sylvok Replica should be sacrificed immediately (cost)
        harness.assertNotOnBattlefield(player1, "Sylvok Replica");
        harness.assertInGraveyard(player1, "Sylvok Replica");

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Sylvok Replica");
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Resolving ability destroys target artifact")
    void resolvingAbilityDestroysTargetArtifact() {
        addReadyReplica(player1);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Accorder's Shield");
        harness.assertInGraveyard(player2, "Accorder's Shield");
    }

    @Test
    @DisplayName("Resolving ability destroys target enchantment")
    void resolvingAbilityDestroysTargetEnchantment() {
        addReadyReplica(player1);
        Permanent target = addReadyEnchantment(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "True Conviction");
        harness.assertInGraveyard(player2, "True Conviction");
    }

    @Test
    @DisplayName("Can target own artifact")
    void canTargetOwnArtifact() {
        addReadyReplica(player1);
        Permanent target = addReadyArtifact(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Accorder's Shield");
        harness.assertInGraveyard(player1, "Accorder's Shield");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyReplica(player1);
        Permanent target = addReadyArtifact(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with only colorless mana (needs green)")
    void cannotActivateWithOnlyColorlessMana() {
        addReadyReplica(player1);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate with summoning sickness (no tap cost)")
    void canActivateWithSummoningSickness() {
        SylvokReplica card = new SylvokReplica();
        harness.addToBattlefield(player1, card);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target creature")
    void cannotTargetCreature() {
        addReadyReplica(player1);
        Permanent creature = addCreatureReady(player2, new CarapaceForger());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target land")
    void cannotTargetLand() {
        addReadyReplica(player1);
        Permanent land = addReadyLand(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyReplica(player1);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Accorder's Shield"));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Can destroy an artifact creature")
    void destroysArtifactCreature() {
        addReadyReplica(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SylvokReplica());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Sylvok Replica");
        harness.assertInGraveyard(player2, "Sylvok Replica");
    }

    @Test
    @DisplayName("Can target itself and sacrifice itself before resolution")
    void canTargetItself() {
        Permanent replica = addReadyReplica(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, replica.getId());

        harness.assertNotOnBattlefield(player1, "Sylvok Replica");
        harness.assertInGraveyard(player1, "Sylvok Replica");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Indestructible target survives but Replica is still sacrificed")
    void indestructibleTargetSurvives() {
        addReadyReplica(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelMyr());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Darksteel Myr");
        harness.assertNotOnBattlefield(player1, "Sylvok Replica");
        harness.assertInGraveyard(player1, "Sylvok Replica");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can activate while tapped")
    void canActivateWhileTapped() {
        Permanent replica = addReadyReplica(player1);
        replica.tap();
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sylvok Replica");
        harness.assertInGraveyard(player2, "Accorder's Shield");
    }

    private Permanent addReadyReplica(Player player) {
        return addCreatureReady(player, new SylvokReplica());
    }

    private Permanent addReadyArtifact(Player player) {
        return addCreatureReady(player, new AccordersShield());
    }

    private Permanent addReadyEnchantment(Player player) {
        return harness.addToBattlefieldAndReturn(player, new TrueConviction());
    }

    private Permanent addReadyLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Island());
    }
}
