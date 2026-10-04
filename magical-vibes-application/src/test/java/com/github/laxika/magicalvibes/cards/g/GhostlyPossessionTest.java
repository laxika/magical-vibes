package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BlazingTorch;
import com.github.laxika.magicalvibes.cards.b.BrimstoneVolley;
import com.github.laxika.magicalvibes.cards.l.LanternSpirit;
import com.github.laxika.magicalvibes.cards.r.RageThrower;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({GhostlyPossession.class, WalkingCorpse.class, LanternSpirit.class,
        BlazingTorch.class, BrimstoneVolley.class, RageThrower.class})
class GhostlyPossessionTest extends BaseCardTest {

    @Test
    @DisplayName("Can target a creature with Ghostly Possession")
    void canTargetCreature() {
        Permanent corpse = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new GhostlyPossession()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, corpse.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Ghostly Possession")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new BlazingTorch());
        harness.setHand(player1, List.of(new GhostlyPossession()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        Permanent artifact = findPermanent(player1, "Blazing Torch");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanted creature has flying")
    void enchantedCreatureHasFlying() {
        Permanent corpse = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        corpse.setSummoningSick(false);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GhostlyPossession());
        aura.setAttachedTo(corpse.getId());

        assertThat(gqs.hasKeyword(gd, corpse, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature deals no combat damage to defending player")
    void enchantedAttackerDealsNoCombatDamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent corpse = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        corpse.setSummoningSick(false);
        corpse.setAttacking(true);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GhostlyPossession());
        aura.setAttachedTo(corpse.getId());

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
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GhostlyPossession());
        aura.setAttachedTo(attacker.getId());

        // 2/1 flying blocker — would die to 2 damage but attacker's combat damage is prevented
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new LanternSpirit());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();

        // Both creatures survive because damage to and by the attacker is prevented.
        harness.assertOnBattlefield(player2, "Lantern Spirit");
        harness.assertOnBattlefield(player1, "Walking Corpse");
    }

    @Test
    @DisplayName("Enchanted creature takes no combat damage when blocking")
    void enchantedBlockerTakesNoCombatDamage() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        // 2/1 flying blocker enchanted with Ghostly Possession — should survive combat
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new LanternSpirit());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new GhostlyPossession());
        aura.setAttachedTo(blocker.getId());

        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();

        // Neither creature takes damage from the enchanted blocker or its attacker.
        harness.assertOnBattlefield(player2, "Lantern Spirit");
        harness.assertOnBattlefield(player1, "Walking Corpse");
    }

    @Test
    @DisplayName("Non-combat damage to enchanted creature is not prevented")
    void nonCombatDamageIsNotPrevented() {
        // 2/2 creature enchanted with Ghostly Possession
        Permanent corpse = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        corpse.setSummoningSick(false);

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new GhostlyPossession());
        aura.setAttachedTo(corpse.getId());

        // Brimstone Volley deals 3 non-combat damage — should kill the 2/2
        harness.setHand(player1, List.of(new BrimstoneVolley()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, corpse.getId());
        harness.passBothPriorities();

        // The creature should be dead — non-combat damage is not prevented
        harness.assertInGraveyard(player2, "Walking Corpse");
    }

    @Test
    @DisplayName("Creature loses flying and damage prevention when Ghostly Possession is removed")
    void effectsStopWhenRemoved() {
        Permanent corpse = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        corpse.setSummoningSick(false);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GhostlyPossession());
        aura.setAttachedTo(corpse.getId());

        // Verify effects are active
        assertThat(gqs.hasKeyword(gd, corpse, Keyword.FLYING)).isTrue();

        // Remove Ghostly Possession
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        // Verify effects are gone
        assertThat(gqs.hasKeyword(gd, corpse, Keyword.FLYING)).isFalse();

        corpse.setAttacking(true);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Aura resolves on an opponent's creature and prevents its combat damage")
    void canEnchantOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new GhostlyPossession()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Ghostly Possession");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();

        creature.setSummoningSick(false);
        creature.setAttacking(true);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);
        harness.resolveCombatDamage();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Other creatures still deal combat damage and do not gain flying")
    void otherCreaturesAreUnaffected() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        enchanted.setSummoningSick(false);
        enchanted.setAttacking(true);
        Permanent other = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        other.setSummoningSick(false);
        other.setAttacking(true);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GhostlyPossession());
        aura.setAttachedTo(enchanted.getId());
        harness.setLife(player2, 20);

        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Enchanted creature can still deal noncombat damage")
    void nonCombatDamageByEnchantedCreatureIsNotPrevented() {
        Permanent thrower = harness.addToBattlefieldAndReturn(player1, new RageThrower());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GhostlyPossession());
        aura.setAttachedTo(thrower.getId());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BrimstoneVolley()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, victim.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }
}
