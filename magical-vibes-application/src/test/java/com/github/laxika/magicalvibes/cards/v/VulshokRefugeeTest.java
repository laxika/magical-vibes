package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.cards.d.Dismember;
import com.github.laxika.magicalvibes.cards.f.Firebreathing;
import com.github.laxika.magicalvibes.cards.m.MeliraSylvokOutcast;
import com.github.laxika.magicalvibes.cards.s.SlashPanther;
import com.github.laxika.magicalvibes.cards.w.Whipflare;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VulshokRefugee.class, SlashPanther.class, MeliraSylvokOutcast.class,
        VoltCharge.class, VaporSnag.class, Whipflare.class, Dismember.class, Firebreathing.class})
class VulshokRefugeeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Vulshok Refugee puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new VulshokRefugee()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(VulshokRefugee.class);
    }

    @Test
    @DisplayName("Resolving puts Vulshok Refugee on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new VulshokRefugee()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Vulshok Refugee");
    }

    @Test
    @DisplayName("Red creature cannot block Vulshok Refugee")
    void redCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new VulshokRefugee());
        attacker.setAttacking(true);

        addCreatureReady(player2, new SlashPanther());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Non-red creature can block Vulshok Refugee")
    void nonRedCreatureCanBlock() {
        Permanent attacker = addCreatureReady(player1, new VulshokRefugee());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new MeliraSylvokOutcast());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Vulshok Refugee takes no combat damage from red creature")
    void takesNoDamageFromRed() {
        Permanent attacker = addCreatureReady(player1, new SlashPanther());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new VulshokRefugee());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player2, "Vulshok Refugee");
        harness.assertInGraveyard(player1, "Slash Panther");
    }

    @Test
    @DisplayName("Vulshok Refugee takes normal combat damage from non-red creature")
    void takesNormalDamageFromNonRed() {
        Permanent attacker = addCreatureReady(player1, new MeliraSylvokOutcast());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new VulshokRefugee());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertNotOnBattlefield(player2, "Vulshok Refugee");
        harness.assertInGraveyard(player2, "Vulshok Refugee");
    }

    @Test
    @DisplayName("Cannot be targeted by red instant")
    void cannotBeTargetedByRedInstant() {
        Permanent refugee = addCreatureReady(player2, new VulshokRefugee());

        // Add valid target so spell is playable
        harness.addToBattlefield(player2, new MeliraSylvokOutcast());

        harness.setHand(player1, List.of(new VoltCharge()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, refugee.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Can be targeted by non-red instant")
    void canBeTargetedByNonRedInstant() {
        Permanent refugee = addCreatureReady(player1, new VulshokRefugee());

        harness.setHand(player1, List.of(new VaporSnag()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, refugee.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(VaporSnag.class);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Vulshok Refugee");
        harness.assertInHand(player1, "Vulshok Refugee");
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Protection prevents untargeted red damage")
    void preventsUntargetedRedDamage() {
        harness.addToBattlefield(player1, new VulshokRefugee());
        harness.addToBattlefield(player2, new MeliraSylvokOutcast());
        harness.setHand(player1, List.of(new Whipflare()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vulshok Refugee");
        harness.assertInGraveyard(player2, "Melira, Sylvok Outcast");
    }

    @Test
    @DisplayName("Protection from red does not prevent non-red toughness reduction")
    void nonRedToughnessReductionKillsRefugee() {
        Permanent refugee = harness.addToBattlefieldAndReturn(player2, new VulshokRefugee());
        harness.setHand(player1, List.of(new Dismember()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, refugee.getId());

        harness.assertNotOnBattlefield(player2, "Vulshok Refugee");
        harness.assertInGraveyard(player2, "Vulshok Refugee");
    }

    @Test
    @DisplayName("A red Aura cannot target Refugee even when its controller casts it")
    void controllerCannotEnchantWithRedAura() {
        Permanent refugee = harness.addToBattlefieldAndReturn(player1, new VulshokRefugee());
        harness.addToBattlefield(player1, new MeliraSylvokOutcast());
        harness.setHand(player1, List.of(new Firebreathing()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, refugee.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }
}
