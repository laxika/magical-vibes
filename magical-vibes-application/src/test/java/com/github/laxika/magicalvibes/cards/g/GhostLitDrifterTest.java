package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhostLitDrifter.class, GrizzlyBears.class, Forest.class})
class GhostLitDrifterTest extends BaseCardTest {

    @Test
    @DisplayName("The battlefield ability gives another target creature flying")
    void battlefieldAbilityGivesAnotherCreatureFlying() {
        addCreatureReady(player1, new GhostLitDrifter());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The battlefield ability cannot target Ghost-Lit Drifter itself")
    void battlefieldAbilityRejectsSelfTarget() {
        Permanent drifter = addCreatureReady(player1, new GhostLitDrifter());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, drifter.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Channel gives exactly X target creatures flying and discards the card")
    void channelGivesExactlyXCreaturesFlying() {
        Permanent firstTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondTarget = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GhostLitDrifter()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.ensurePriority(player1);
        harness.getGameService().activateHandAbility(
                gd, player1, 0, 0, null, 2, List.of(firstTarget.getId(), secondTarget.getId()));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, firstTarget, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondTarget, Keyword.FLYING)).isTrue();
        harness.assertInGraveyard(player1, "Ghost-Lit Drifter");
    }

    @Test
    @DisplayName("Channel requires exactly X legal creature targets")
    void channelRejectsWrongTargetCountAndNonCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new GhostLitDrifter()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.ensurePriority(player1);
        assertThatThrownBy(() -> harness.getGameService().activateHandAbility(
                gd, player1, 0, 0, null, 2, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.getGameService().activateHandAbility(
                gd, player1, 0, 0, null, 1, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Ghost-Lit Drifter");
    }

    @Test
    @DisplayName("Both flying abilities wear off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new GhostLitDrifter());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }
}
