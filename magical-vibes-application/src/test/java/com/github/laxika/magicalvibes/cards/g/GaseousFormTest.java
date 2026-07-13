package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GaseousFormTest extends BaseCardTest {

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Can target a creature with Gaseous Form")
    void canTargetCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        harness.setHand(player1, List.of(new GaseousForm()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Gaseous Form")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new GaseousForm()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Combat damage prevention — enchanted creature deals no combat damage =====

    @Test
    @DisplayName("Enchanted creature deals no combat damage to defending player")
    void enchantedAttackerDealsNoCombatDamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent aura = new Permanent(new GaseousForm());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Enchanted creature deals no combat damage to blocking creature")
    void enchantedAttackerDealsNoCombatDamageToBlocker() {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent aura = new Permanent(new GaseousForm());
        aura.setAttachedTo(attacker.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        // 1/1 blocker — would die to 2 damage but attacker's combat damage is prevented
        Permanent blocker = new Permanent(new GoldMyr());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Blocker survives because the enchanted attacker's combat damage is prevented
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Gold Myr"));
    }

    // ===== Combat damage prevention — enchanted creature takes no combat damage =====

    @Test
    @DisplayName("Enchanted creature takes no combat damage when blocking")
    void enchantedBlockerTakesNoCombatDamage() {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        // 1/1 blocker enchanted with Gaseous Form — should survive combat
        Permanent blocker = new Permanent(new GoldMyr());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        Permanent aura = new Permanent(new GaseousForm());
        aura.setAttachedTo(blocker.getId());
        gd.playerBattlefields.get(player2.getId()).add(aura);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Blocker survives because combat damage to it is prevented
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Gold Myr"));
    }

    // ===== Non-combat damage is NOT prevented =====

    @Test
    @DisplayName("Non-combat damage to enchanted creature is not prevented")
    void nonCombatDamageIsNotPrevented() {
        // 2/2 creature enchanted with Gaseous Form
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        Permanent aura = new Permanent(new GaseousForm());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player2.getId()).add(aura);

        // Lightning Bolt deals 3 non-combat damage — should kill the 2/2
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        gs.playCard(gd, player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();

        // Bears should be dead — non-combat damage is not prevented
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
