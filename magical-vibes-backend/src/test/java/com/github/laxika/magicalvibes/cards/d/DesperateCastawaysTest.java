package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GolemsHeart;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessControllerControlsMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DesperateCastawaysTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has STATIC CantAttackUnlessControllerControlsMatchingPermanentEffect with artifact predicate")
    void hasCorrectStructure() {
        DesperateCastaways card = new DesperateCastaways();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(CantAttackUnlessControllerControlsMatchingPermanentEffect.class);
        CantAttackUnlessControllerControlsMatchingPermanentEffect effect =
                (CantAttackUnlessControllerControlsMatchingPermanentEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.controllerPermanentPredicate()).isInstanceOf(PermanentIsArtifactPredicate.class);
    }

    // ===== Attack restriction =====

    @Test
    @DisplayName("Can attack when controller controls an artifact")
    void canAttackWhenControllerControlsArtifact() {
        harness.setLife(player2, 20);
        Permanent castaways = addCreatureReady(player1, new DesperateCastaways());
        harness.addToBattlefield(player1, new GolemsHeart());

        declareAttackers(player1, List.of(0));

        // Attack went through — defender takes 2 damage (power of Desperate Castaways)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Cannot attack when controller does not control an artifact")
    void cannotAttackWithoutArtifact() {
        Permanent castaways = addCreatureReady(player1, new DesperateCastaways());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot attack when only opponent controls an artifact")
    void cannotAttackWhenOnlyOpponentControlsArtifact() {
        Permanent castaways = addCreatureReady(player1, new DesperateCastaways());
        harness.addToBattlefield(player2, new GolemsHeart());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can block without controlling an artifact")
    void canBlockWithoutArtifact() {
        harness.setLife(player1, 20);
        // Opponent has a vanilla attacker
        Permanent attacker = addCreatureReady(player2, new DesperateCastaways());
        // Give opponent an artifact so the opponent's Castaways can attack
        harness.addToBattlefield(player2, new GolemsHeart());

        // Player1 has their own Castaways with no artifact — still should be able to block
        Permanent blocker = addCreatureReady(player1, new DesperateCastaways());

        // Declare the opponent's creature as attacker
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player2, List.of(0));

        // Now declare blocker
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        // Blocker was accepted — the creature is blocking
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isBlocking()).isTrue();
    }

    // ===== Helper methods =====

    private Permanent addCreatureReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
