package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BackupAgent;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RefuseToYield.class, BackupAgent.class, Mountain.class, Murder.class})
class RefuseToYieldTest extends BaseCardTest {

    @Test
    void boostsAndUntapsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BackupAgent());
        target.tap();
        castRefuseToYield(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(7);
    }

    @Test
    void boostExpiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BackupAgent());
        castRefuseToYield(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new RefuseToYield()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void canBoostAnUntappedOpponentsCreatureWithoutAffectingOtherCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BackupAgent());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new BackupAgent());
        other.tap();

        castRefuseToYield(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(7);
        assertThat(other.isTapped()).isTrue();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
    }

    @Test
    void canUntapAndBoostAnOpponentsTappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BackupAgent());
        target.tap();

        castRefuseToYield(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(7);
    }

    @Test
    void boostsFromMultipleCopiesAccumulateAndExpireTogether() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BackupAgent());

        castRefuseToYield(target);
        castRefuseToYield(target);

        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(14);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    void doesNotResolveIfTargetIsDestroyedInResponse() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BackupAgent());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new BackupAgent());
        target.tap();
        other.tap();
        harness.setHand(player1, List.of(new RefuseToYield()));
        addMana();
        harness.castInstant(player1, 0, target.getId());

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof BackupAgent)
                .anyMatch(card -> card instanceof RefuseToYield);
        assertThat(other.isTapped()).isTrue();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
    }

    private void castRefuseToYield(Permanent target) {
        harness.setHand(player1, List.of(new RefuseToYield()));
        addMana();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
