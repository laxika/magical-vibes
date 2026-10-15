package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindTransferProtocol.class, GrizzlyBears.class, Spellbook.class, Island.class})
class MindTransferProtocolTest extends BaseCardTest {

    @Test
    void turnsCreatureIntoArtifactCreatureDrawsACardAndRevertsAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card draw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(draw));
        cast(target);

        assertThat(gqs.isArtifact(gd, target)).isTrue();
        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, target)).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void turnsNoncreatureArtifactIntoArtifactCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        cast(target);

        assertThat(gqs.isArtifact(gd, target)).isTrue();
        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    void cannotTargetALand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new MindTransferProtocol()));
        addCastMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    @Test
    void countersModifyTheAnimatedBasePowerAndToughness() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLibrary(player1, List.of(new Island()));

        cast(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(7);
    }

    @Test
    void noncreatureArtifactStopsBeingACreatureAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setLibrary(player1, List.of(new Island()));
        cast(target);

        assertThat(gqs.isCreature(gd, target)).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, target)).isFalse();
        assertThat(gqs.isArtifact(gd, target)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    void doesNotDrawWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card draw = new Island();
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player1, List.of(new MindTransferProtocol()));
        addCastMana();
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
        harness.assertInGraveyard(player1, "Mind Transfer Protocol");
        assertThat(gd.stack).isEmpty();
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new MindTransferProtocol()));
        addCastMana();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addCastMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
