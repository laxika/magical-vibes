package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScaleOfChissGoria.class, Forest.class, Ornithopter.class, Bonesplitter.class})
class ScaleOfChissGoriaTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping it gives target creature +0/+1 until end of turn")
    void tappingItBoostsTargetCreature() {
        Permanent scale = harness.addToBattlefieldAndReturn(player1, new ScaleOfChissGoria());
        Permanent ornithopter = addCreatureReady(player2, new Ornithopter());

        harness.activateAbility(player1, 0, null, ornithopter.getId());
        harness.passBothPriorities();

        assertThat(scale.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, ornithopter)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, ornithopter)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ornithopter)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, ornithopter)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent scale = harness.addToBattlefieldAndReturn(player1, new ScaleOfChissGoria());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(scale.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Affinity for artifacts reduces its generic mana cost")
    void affinityForArtifactsReducesGenericCost() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new Bonesplitter());
        }
        harness.setHand(player1, List.of(new ScaleOfChissGoria()));

        harness.castArtifact(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity reduces the generic cost by one for each artifact")
    void affinityReducesCostOncePerArtifact() {
        for (int i = 0; i < 2; i++) {
            harness.addToBattlefield(player1, new Bonesplitter());
        }
        harness.setHand(player1, List.of(new ScaleOfChissGoria()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity counts only artifacts controlled by the spell's controller")
    void affinityCountsOnlyControlledArtifacts() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player2, new Bonesplitter());
        }
        harness.setHand(player1, List.of(new ScaleOfChissGoria()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Flash allows casting it during an opponent's turn")
    void flashAllowsCastingDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new ScaleOfChissGoria()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.passPriority(player2);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }
}
