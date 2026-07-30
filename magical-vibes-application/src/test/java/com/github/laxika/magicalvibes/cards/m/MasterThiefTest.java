package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MasterThiefTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains control of target artifact")
    void etbGainsControlOfTargetArtifact() {
        Permanent artifact = addArtifact(player2);

        castMasterThief(artifact.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(artifact.getId()));

        Permanent thief = findPermanent(player1, "Master Thief");
        assertThat(gd.newestControlEffectFor(artifact.getId()).sourcePermanentId()).isEqualTo(thief.getId());
    }

    @Test
    @DisplayName("Stolen artifact returns to its owner when Master Thief leaves the battlefield")
    void stolenArtifactReturnsWhenThiefBounced() {
        Permanent artifact = addArtifact(player2);

        castMasterThief(artifact.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent thief = findPermanent(player1, "Master Thief");

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, thief.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(gd.controlEffectsFor(artifact.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifact() {
        addArtifact(player2); // legal target exists so the spell is castable
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.setHand(player1, List.of(new MasterThief()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castMasterThief(UUID targetId) {
        harness.setHand(player1, List.of(new MasterThief()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0, 0, targetId);
    }

    private Permanent addArtifact(Player player) {
        Permanent perm = new Permanent(new MindStone());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
