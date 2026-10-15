package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Brushstrider;
import com.github.laxika.magicalvibes.cards.c.ChromaticLantern;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SavageSurge.class, Brushstrider.class, ChromaticLantern.class})
class SavageSurgeTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Savage Surge boosts and untaps the target creature")
    void boostsAndUntapsTarget() {
        Permanent target = addTappedCreature(player2);
        castSavageSurge(target);

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An already untapped creature is simply boosted")
    void untappedCreatureJustGetsBoost() {
        Permanent target = addTappedCreature(player1);
        target.untap();
        castSavageSurge(target);

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostExpiresAtEndOfTurn() {
        Permanent target = addTappedCreature(player1);
        castSavageSurge(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addTappedCreature(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ChromaticLantern());
        harness.setHand(player1, List.of(new SavageSurge()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castSavageSurge(Permanent target) {
        harness.setHand(player1, List.of(new SavageSurge()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    @Test
    @DisplayName("Only the targeted creature is boosted and untapped")
    void leavesOtherCreaturesUnchanged() {
        Permanent target = addTappedCreature(player2);
        Permanent other = addTappedCreature(player1);

        castSavageSurge(target);

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(target.isTapped()).isFalse();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(other.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Multiple Savage Surges stack their boosts until cleanup")
    void repeatedBoostsStackAndExpire() {
        Permanent target = addTappedCreature(player1);
        castSavageSurge(target);
        castSavageSurge(target);

        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Neither effect happens when the target leaves before resolution")
    void removedTargetPreventsBothEffects() {
        Permanent target = addTappedCreature(player2);
        Permanent other = addTappedCreature(player2);
        harness.setHand(player1, List.of(new SavageSurge()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.isTapped()).isTrue();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(other.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Savage Surge");
    }

    private Permanent addTappedCreature(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new Brushstrider());
        perm.setSummoningSick(false);
        perm.tap();
        return perm;
    }
}
