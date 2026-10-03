package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.e.Expansion;
import com.github.laxika.magicalvibes.cards.s.SauroformHybrid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CodeOfConstraint.class, SauroformHybrid.class, Forest.class, Expansion.class})
class CodeOfConstraintTest extends BaseCardTest {

    @Test
    void debuffsDrawsTapsAndLocksDuringMainPhase() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());
        harness.setHand(player1, List.of(new CodeOfConstraint()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    void onlyDebuffsAndDrawsOutsideMainPhase() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());
        harness.setHand(player1, List.of(new CodeOfConstraint()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(bear.isTapped()).isFalse();
        assertThat(bear.getSkipUntapCount()).isZero();
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new CodeOfConstraint()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void addendumAppliesDuringPostcombatMainAndLocksExactlyOneUntap() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());
        creature.setTapped(true);
        harness.setHand(player1, List.of(new CodeOfConstraint()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-2);
        harness.assertInHand(player1, "Forest");
        harness.performUntapStep(player1);
        assertThat(creature.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void opponentsMainPhaseDoesNotGrantAddendum() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SauroformHybrid());
        harness.setHand(player1, List.of(new CodeOfConstraint()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-2);
        harness.assertInHand(player1, "Forest");
        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getSkipUntapCount()).isZero();
    }

    @Test
    void illegalTargetPreventsDrawing() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());
        harness.setHand(player1, List.of(new CodeOfConstraint()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, creature.getId());
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed({CodeOfConstraint.class, SauroformHybrid.class, Forest.class, Expansion.class})
    void spellCopyDoesNotReceiveAddendumBonus() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());
        CodeOfConstraint original = new CodeOfConstraint();
        harness.setHand(player1, List.of(original, new Expansion()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0, creature.getId());
        harness.castInstant(player1, 0, 0, original.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getSkipUntapCount()).isZero();
    }
}
