package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvenFlock;
import com.github.laxika.magicalvibes.cards.k.KrosanArcher;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Skyshooter.class, AvenFlock.class, KrosanArcher.class})
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
    @CardUsed(SkysovereignConsulFlagship.class)
    @DisplayName("Cannot target a noncreature permanent with flying")
    void cannotTargetNonCreaturePermanentWithFlying() {
        addCreatureReady(player1, new Skyshooter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SkysovereignConsulFlagship());
        target.setAttacking(true);
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
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
