package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArmadilloCloak;
import com.github.laxika.magicalvibes.cards.b.BenalishLancer;
import com.github.laxika.magicalvibes.cards.c.ChargingTroll;
import com.github.laxika.magicalvibes.cards.o.OrimsTouch;
import com.github.laxika.magicalvibes.cards.r.Repulse;
import com.github.laxika.magicalvibes.cards.r.Rout;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShivanZombie.class, BenalishLancer.class, ChargingTroll.class, ArmadilloCloak.class,
        Repulse.class, OrimsTouch.class, Rout.class})
class ShivanZombieTest extends BaseCardTest {

    @Test
    @DisplayName("White creature cannot block Shivan Zombie")
    void whiteCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new ShivanZombie());
        attacker.setAttacking(true);

        addCreatureReady(player2, new BenalishLancer());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("White creature deals no combat damage to Shivan Zombie")
    void whiteCreatureDealsNoCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new ChargingTroll());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new ShivanZombie());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player2, "Shivan Zombie");
    }

    @Test
    @DisplayName("Shivan Zombie cannot be enchanted by a white Aura")
    void cannotBeEnchantedByWhiteAura() {
        Permanent zombie = addCreatureReady(player1, new ShivanZombie());

        harness.setHand(player2, List.of(new ArmadilloCloak()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player2, 0, zombie.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Shivan Zombie can be targeted by a blue instant")
    void canBeTargetedByBlueInstant() {
        Permanent zombie = addCreatureReady(player1, new ShivanZombie());

        harness.setHand(player2, List.of(new Repulse()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0, zombie.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Even its controller cannot target Shivan Zombie with a white instant")
    void cannotBeTargetedByControllersWhiteInstant() {
        Permanent zombie = addCreatureReady(player1, new ShivanZombie());
        harness.setHand(player1, List.of(new OrimsTouch()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, zombie.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Nonwhite creatures can block and deal lethal damage to Shivan Zombie")
    void nonwhiteCreatureCanBlockAndDealDamage() {
        addCreatureReady(player1, new ShivanZombie());
        addCreatureReady(player2, new ShivanZombie());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertNotOnBattlefield(player1, "Shivan Zombie");
        harness.assertNotOnBattlefield(player2, "Shivan Zombie");
        harness.assertInGraveyard(player1, "Shivan Zombie");
        harness.assertInGraveyard(player2, "Shivan Zombie");
    }

    @Test
    @DisplayName("Protection does not stop untargeted destruction by a white spell")
    void whiteBoardWipeDestroysZombie() {
        addCreatureReady(player1, new ShivanZombie());
        harness.setHand(player1, List.of(new Rout()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Shivan Zombie");
        harness.assertInGraveyard(player1, "Shivan Zombie");
    }
}
