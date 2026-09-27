package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LeoninSkyhunter;
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

@CardUsed({ToothOfChissGoria.class, LeoninSkyhunter.class, Forest.class, Bonesplitter.class})
class ToothOfChissGoriaTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping it gives target creature +1/+0 until end of turn")
    void tappingItBoostsTargetCreature() {
        Permanent tooth = harness.addToBattlefieldAndReturn(player1, new ToothOfChissGoria());
        Permanent skyhunter = addCreatureReady(player2, new LeoninSkyhunter());

        harness.activateAbility(player1, 0, null, skyhunter.getId());
        harness.passBothPriorities();

        assertThat(tooth.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, skyhunter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, skyhunter)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, skyhunter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, skyhunter)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent tooth = harness.addToBattlefieldAndReturn(player1, new ToothOfChissGoria());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(tooth.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Affinity for artifacts reduces its generic mana cost")
    void affinityForArtifactsReducesGenericCost() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new Bonesplitter());
        }
        harness.setHand(player1, List.of(new ToothOfChissGoria()));

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
        harness.setHand(player1, List.of(new ToothOfChissGoria()));
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
        harness.setHand(player1, List.of(new ToothOfChissGoria()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Affinity does not count nonartifact permanents")
    void affinityDoesNotCountNonartifactPermanents() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new ToothOfChissGoria()));

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

        harness.setHand(player1, List.of(new ToothOfChissGoria()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.passPriority(player2);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }
}
