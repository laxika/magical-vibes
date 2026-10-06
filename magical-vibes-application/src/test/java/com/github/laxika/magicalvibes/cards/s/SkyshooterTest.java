package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvenFlock;
import com.github.laxika.magicalvibes.cards.a.AetherBurst;
import com.github.laxika.magicalvibes.cards.k.KrosanArcher;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Skyshooter.class, AvenFlock.class, KrosanArcher.class,
        SkysovereignConsulFlagship.class, AetherBurst.class})
class SkyshooterTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices Skyshooter and destroys an attacking creature with flying")
    void destroysAttackingFlyingCreature() {
        addCreatureReady(player1, new Skyshooter());
        Permanent target = addCombatCreature(player2, new AvenFlock(), true, false);
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Skyshooter");
        harness.assertInGraveyard(player1, "Skyshooter");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Aven Flock");
        harness.assertInGraveyard(player2, "Aven Flock");
    }

    @Test
    @DisplayName("Destroys a blocking creature with flying")
    void destroysBlockingFlyingCreature() {
        addCreatureReady(player1, new Skyshooter());
        Permanent target = addCombatCreature(player2, new AvenFlock(), false, true);
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Aven Flock");
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyingCreature() {
        addCreatureReady(player1, new Skyshooter());
        Permanent target = addCombatCreature(player2, new KrosanArcher(), true, false);
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a flying creature that is not attacking or blocking")
    void cannotTargetIdleFlyingCreature() {
        addCreatureReady(player1, new Skyshooter());
        Permanent target = addCombatCreature(player2, new AvenFlock(), false, false);
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with flying")
    void cannotTargetNonCreaturePermanentWithFlying() {
        addCreatureReady(player1, new Skyshooter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SkysovereignConsulFlagship());
        target.setAttacking(true);
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The tap-and-sacrifice ability requires no mana")
    void activatesWithoutMana() {
        addCreatureReady(player1, new Skyshooter());
        Permanent target = addCombatCreature(player2, new AvenFlock(), true, false);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Skyshooter");
        harness.assertNotOnBattlefield(player1, "Skyshooter");
        harness.assertOnBattlefield(player2, "Aven Flock");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Aven Flock");
    }

    @Test
    @DisplayName("Can target its controller's attacking flying creature")
    void canDestroyItsControllersFlyingCreature() {
        addCreatureReady(player1, new Skyshooter());
        Permanent target = addCombatCreature(player1, new AvenFlock(), true, false);
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Skyshooter");
        harness.assertInGraveyard(player1, "Aven Flock");
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent skyshooter = addCreatureReady(player1, new Skyshooter());
        skyshooter.tap();
        Permanent target = addCombatCreature(player2, new AvenFlock(), true, false);
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        harness.assertOnBattlefield(player1, "Skyshooter");
        harness.assertNotInGraveyard(player1, "Skyshooter");
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent skyshooter = addCreatureReady(player1, new Skyshooter());
        skyshooter.setSummoningSick(true);
        Permanent target = addCombatCreature(player2, new AvenFlock(), true, false);
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        harness.assertOnBattlefield(player1, "Skyshooter");
        harness.assertNotInGraveyard(player1, "Skyshooter");
    }

    @Test
    @DisplayName("Returning the target to hand does not refund the sacrifice")
    void sacrificeRemainsPaidWhenTargetLeavesBattlefield() {
        addCreatureReady(player1, new Skyshooter());
        Permanent target = addCombatCreature(player2, new AvenFlock(), true, false);
        addActivationMana();
        harness.setHand(player2, List.of(new AetherBurst()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.castAndResolveInstant(player2, 0, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Skyshooter");
        harness.assertNotOnBattlefield(player1, "Skyshooter");
        harness.assertInHand(player2, "Aven Flock");
        harness.assertNotInGraveyard(player2, "Aven Flock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Reach allows Skyshooter to block an attacking flyer")
    void canBlockFlyingCreature() {
        addCreatureReady(player2, new AvenFlock());
        addCreatureReady(player1, new Skyshooter());
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        harness.assertInGraveyard(player1, "Skyshooter");
        harness.assertOnBattlefield(player2, "Aven Flock");
        harness.assertLife(player1, 20);
    }

    private Permanent addCombatCreature(Player player, Card card, boolean attacking, boolean blocking) {
        Permanent permanent = addCreatureReady(player, card);
        permanent.setAttacking(attacking);
        permanent.setBlocking(blocking);
        return permanent;
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
