package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerCreaturesCantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConcussiveBoltTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has DealDamageToTargetPlayerEffect(4) + MetalcraftConditionalEffect wrapping TargetPlayerCreaturesCantBlockThisTurnEffect")
    void hasCorrectStructure() {
        ConcussiveBolt card = new ConcussiveBolt();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(DealDamageToTargetPlayerEffect.class);
        assertThat(((DealDamageToTargetPlayerEffect) card.getEffects(EffectSlot.SPELL).get(0)).damage()).isEqualTo(4);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(MetalcraftConditionalEffect.class);
        MetalcraftConditionalEffect metalcraft = (MetalcraftConditionalEffect) card.getEffects(EffectSlot.SPELL).get(1);
        assertThat(metalcraft.wrapped()).isInstanceOf(TargetPlayerCreaturesCantBlockThisTurnEffect.class);
    }

    // ===== Without metalcraft =====

    @Test
    @DisplayName("Deals 4 damage to target player without metalcraft")
    void deals4DamageToPlayerWithoutMetalcraft() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ConcussiveBolt()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Does not prevent blocking without metalcraft")
    void doesNotPreventBlockingWithoutMetalcraft() {
        harness.setLife(player2, 20);
        Permanent creature = addReadyCreature(player2);

        harness.setHand(player1, List.of(new ConcussiveBolt()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(creature.isCantBlockThisTurn()).isFalse();
    }

    // ===== With metalcraft =====

    @Test
    @DisplayName("Deals 4 damage and prevents blocking with metalcraft")
    void deals4DamageAndPreventsBlockingWithMetalcraft() {
        harness.setLife(player2, 20);
        Permanent creature = addReadyCreature(player2);

        harness.setHand(player1, List.of(new ConcussiveBolt()));
        harness.addMana(player1, ManaColor.RED, 5);
        addThreeArtifacts(player1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(creature.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("All creatures of target player can't block with metalcraft")
    void allCreaturesOfTargetPlayerCantBlockWithMetalcraft() {
        Permanent creature1 = addReadyCreature(player2);
        Permanent creature2 = addReadyCreature(player2);

        harness.setHand(player1, List.of(new ConcussiveBolt()));
        harness.addMana(player1, ManaColor.RED, 5);
        addThreeArtifacts(player1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(creature1.isCantBlockThisTurn()).isTrue();
        assertThat(creature2.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Metalcraft can't-block prevents declaring blockers")
    void metalcraftCantBlockPreventsDeclaringBlockers() {
        Permanent attacker = addReadyCreature(player1);
        Permanent blocker = addReadyCreature(player2);

        harness.setHand(player1, List.of(new ConcussiveBolt()));
        harness.addMana(player1, ManaColor.RED, 5);
        addThreeArtifacts(player1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Controller's own creatures are not affected by metalcraft")
    void controllersOwnCreaturesNotAffected() {
        Permanent ownCreature = addReadyCreature(player1);

        harness.setHand(player1, List.of(new ConcussiveBolt()));
        harness.addMana(player1, ManaColor.RED, 5);
        addThreeArtifacts(player1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.isCantBlockThisTurn()).isFalse();
    }

    // ===== Metalcraft lost before resolution =====

    @Test
    @DisplayName("Does not prevent blocking if metalcraft lost before resolution")
    void doesNotPreventBlockingIfMetalcraftLostBeforeResolution() {
        harness.setLife(player2, 20);
        Permanent creature = addReadyCreature(player2);

        harness.setHand(player1, List.of(new ConcussiveBolt()));
        harness.addMana(player1, ManaColor.RED, 5);
        addThreeArtifacts(player1);

        harness.castSorcery(player1, 0, player2.getId());

        // Remove artifacts before resolution
        gd.playerBattlefields.get(player1.getId()).removeIf(
                p -> p.getCard().getName().equals("Spellbook") || p.getCard().getName().equals("Leonin Scimitar"));

        harness.passBothPriorities();

        // Damage still dealt
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        // But blocking not prevented
        assertThat(creature.isCantBlockThisTurn()).isFalse();
    }

    // ===== Helpers =====

    private void addThreeArtifacts(Player player) {
        harness.addToBattlefield(player, new Spellbook());
        harness.addToBattlefield(player, new LeoninScimitar());
        harness.addToBattlefield(player, new Spellbook());
    }

    private Permanent addReadyCreature(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
