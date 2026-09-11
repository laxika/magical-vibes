package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MerfolkOfThePearlTrident;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GaseousForm.class, GrizzlyBears.class, Island.class, MerfolkOfThePearlTrident.class,
        ProdigalSorcerer.class, Shock.class})
class GaseousFormTest extends BaseCardTest {

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Can target a creature with Gaseous Form")
    void canTargetCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GaseousForm()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void resolvingAttachesToTargetCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        GaseousForm gaseousForm = new GaseousForm();
        harness.setHand(player1, List.of(gaseousForm));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == gaseousForm
                        && p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Gaseous Form")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new GaseousForm()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        Permanent land = findPermanent(player1, "Island");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Combat damage prevention — enchanted creature deals no combat damage =====

    @Test
    @DisplayName("Enchanted creature deals no combat damage to defending player")
    void enchantedAttackerDealsNoCombatDamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GaseousForm());
        aura.setAttachedTo(bears.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Enchanted creature deals no combat damage to blocking creature")
    void enchantedAttackerDealsNoCombatDamageToBlocker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GaseousForm());
        aura.setAttachedTo(attacker.getId());

        // 1/1 blocker — would die to 2 damage but attacker's combat damage is prevented
        Permanent blocker = addCreatureReady(player2, new MerfolkOfThePearlTrident());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Blocker survives because the enchanted attacker's combat damage is prevented
        harness.assertOnBattlefield(player2, "Merfolk of the Pearl Trident");
    }

    // ===== Combat damage prevention — enchanted creature takes no combat damage =====

    @Test
    @DisplayName("Enchanted creature takes no combat damage when blocking")
    void enchantedBlockerTakesNoCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        // 1/1 blocker enchanted with Gaseous Form — should survive combat
        Permanent blocker = addCreatureReady(player2, new MerfolkOfThePearlTrident());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new GaseousForm());
        aura.setAttachedTo(blocker.getId());

        resolveCombat();

        // Blocker survives because combat damage to it is prevented
        harness.assertOnBattlefield(player2, "Merfolk of the Pearl Trident");
    }

    // ===== Non-combat damage is NOT prevented =====

    @Test
    @DisplayName("Non-combat damage to enchanted creature is not prevented")
    void nonCombatDamageIsNotPrevented() {
        // 2/2 creature enchanted with Gaseous Form
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new GaseousForm());
        aura.setAttachedTo(bears.getId());

        // Shock deals 2 non-combat damage — should kill the 2/2
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        // Bears should be dead — non-combat damage is not prevented
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Enchanted creature deals non-combat damage normally")
    void nonCombatDamageByEnchantedCreatureIsNotPrevented() {
        harness.setLife(player1, 20);

        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new GaseousForm());
        aura.setAttachedTo(sorcerer.getId());

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(sorcerer), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }
}
