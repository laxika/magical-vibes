package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BallynockCohort;
import com.github.laxika.magicalvibes.cards.h.HeapDoll;
import com.github.laxika.magicalvibes.cards.i.IlluminatedFolio;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AphoticWisps.class, AshenmoorCohort.class, BallynockCohort.class,
        HeapDoll.class, IlluminatedFolio.class})
class AphoticWispsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving makes target creature black until end of turn")
    void resolvingMakesTargetBlack() {
        Permanent target = addCreatureReady(player2, new BallynockCohort());
        harness.setHand(player1, List.of(new AphoticWisps()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        // "Becomes black" replaces the colors (CR 105.3), applied by the CR 613 layer engine.
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLACK);
    }

    @Test
    @DisplayName("Target gains fear and cannot be blocked by non-black non-artifact creatures")
    void targetGainsFear() {
        Permanent attacker = addCreatureReady(player1, new BallynockCohort());
        harness.setHand(player1, List.of(new AphoticWisps()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, attacker.getId());

        attacker.setAttacking(true);
        addCreatureReady(player2, new BallynockCohort());

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FEAR)).isTrue();
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(fear)");
    }

    @Test
    @DisplayName("Fear allows a black creature to block")
    void fearAllowsBlackCreatureToBlock() {
        Permanent attacker = addCreatureReady(player1, new BallynockCohort());
        harness.setHand(player1, List.of(new AphoticWisps()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, attacker.getId());
        attacker.setAttacking(true);
        addCreatureReady(player2, new AshenmoorCohort());
        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("Fear allows an artifact creature to block")
    void fearAllowsArtifactCreatureToBlock() {
        Permanent attacker = addCreatureReady(player1, new BallynockCohort());
        harness.setHand(player1, List.of(new AphoticWisps()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, attacker.getId());
        attacker.setAttacking(true);
        addCreatureReady(player2, new HeapDoll());
        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("Color and fear wear off at end of turn")
    void colorAndFearWearOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new BallynockCohort());
        harness.setHand(player1, List.of(new AphoticWisps()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLACK);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FEAR)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.WHITE);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Resolving draws a card")
    void resolvingDrawsACard() {
        Permanent target = addCreatureReady(player2, new BallynockCohort());
        harness.setHand(player1, List.of(new AphoticWisps()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player2, new BallynockCohort());
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player2, new IlluminatedFolio());
        harness.setHand(player1, List.of(new AphoticWisps()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
