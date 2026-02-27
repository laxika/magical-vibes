package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfDefenderControlsMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScrapdiverSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Scrapdiver Serpent has correct static effect")
    void hasCorrectProperties() {
        ScrapdiverSerpent card = new ScrapdiverSerpent();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(CantBeBlockedIfDefenderControlsMatchingPermanentEffect.class);
        CantBeBlockedIfDefenderControlsMatchingPermanentEffect effect =
                (CantBeBlockedIfDefenderControlsMatchingPermanentEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.defenderPermanentPredicate()).isEqualTo(new PermanentIsArtifactPredicate());
    }

    @Test
    @DisplayName("Scrapdiver Serpent can't be blocked when defending player controls an artifact")
    void cantBeBlockedWhenDefenderControlsArtifact() {
        // Defender controls an artifact (Ornithopter)
        harness.addToBattlefield(player2, new Ornithopter());

        // Defender also has a creature that could block
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        // Serpent is attacking
        Permanent serpent = new Permanent(new ScrapdiverSerpent());
        serpent.setSummoningSick(false);
        serpent.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(serpent);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        // Attempting to block the Serpent should fail
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Scrapdiver Serpent can be blocked when defending player controls no artifacts")
    void canBeBlockedWhenDefenderControlsNoArtifact() {
        harness.setLife(player2, 20);

        // Defender has only a non-artifact creature
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        // Serpent is attacking
        Permanent serpent = new Permanent(new ScrapdiverSerpent());
        serpent.setSummoningSick(false);
        serpent.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(serpent);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        // Blocking should succeed — no artifacts on defender's side
        // GrizzlyBears (2/2) blocks and dies to Serpent (5/5), but Serpent is blocked so no player damage
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Defender's life should remain 20 (Serpent was blocked)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Unblocked Scrapdiver Serpent deals 5 damage")
    void dealsFiveDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent serpent = new Permanent(new ScrapdiverSerpent());
        serpent.setSummoningSick(false);
        serpent.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(serpent);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }
}
