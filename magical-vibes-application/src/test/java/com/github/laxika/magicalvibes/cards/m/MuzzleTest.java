package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CrossbowInfantry;
import com.github.laxika.magicalvibes.cards.j.JhovallQueen;
import com.github.laxika.magicalvibes.cards.k.KyrenToy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Muzzle.class, CrossbowInfantry.class, JhovallQueen.class, KyrenToy.class})
class MuzzleTest extends BaseCardTest {

    @Test
    @DisplayName("Can target a creature with Muzzle")
    void canTargetCreature() {
        Permanent creature = addCreatureReady(player2, new JhovallQueen());
        harness.setHand(player1, List.of(new Muzzle()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resolving Muzzle attaches it to the target creature")
    void resolvingAttachesToTargetCreature() {
        Permanent creature = addCreatureReady(player1, new JhovallQueen());
        harness.setHand(player1, List.of(new Muzzle()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Muzzle
                        && creature.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Muzzle")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new KyrenToy());
        harness.setHand(player1, List.of(new Muzzle()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanted attacker deals no combat damage")
    void enchantedAttackerDealsNoCombatDamage() {
        harness.setLife(player2, 20);

        Permanent attacker = addCreatureReady(player1, new JhovallQueen());
        attacker.setAttacking(true);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Muzzle());
        aura.setAttachedTo(attacker.getId());

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Damage dealt to the enchanted creature is not prevented")
    void damageToEnchantedCreatureStillApplies() {
        Permanent source = addCreatureReady(player1, new CrossbowInfantry());
        Permanent creature = addCreatureReady(player2, new JhovallQueen());
        creature.setAttacking(true);

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Muzzle());
        aura.setAttachedTo(creature.getId());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), null,
                creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Jhovall Queen");
    }

    @Test
    @DisplayName("Enchanted creature deals no noncombat damage")
    void enchantedCreatureDealsNoNoncombatDamage() {
        Permanent source = addCreatureReady(player1, new CrossbowInfantry());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Muzzle());
        aura.setAttachedTo(source.getId());

        Permanent target = addCreatureReady(player2, new JhovallQueen());
        target.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), null,
                target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Jhovall Queen");
    }
}
