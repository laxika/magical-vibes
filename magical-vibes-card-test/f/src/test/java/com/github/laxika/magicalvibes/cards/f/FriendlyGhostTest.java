package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FriendlyGhost.class, GrizzlyBears.class})
class FriendlyGhostTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives the target creature +2/+4 until end of turn")
    void etbBoostsTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolve(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
    }

    @Test
    @DisplayName("ETB can target a creature controlled by its controller")
    void etbCanTargetOwnCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castAndResolve(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolve(bears);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB fizzles if the target leaves before resolution")
    void etbFizzlesIfTargetLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FriendlyGhost()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0, 0, bears.getId());

        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).removeIf(permanent -> permanent.getId().equals(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @CardUsed({FriendlyGhost.class})
    @DisplayName("The entering Ghost can target itself on an otherwise empty battlefield")
    void etbCanTargetItself() {
        Permanent ghost = harness.enterBattlefieldAndReturn(player1, new FriendlyGhost());
        harness.handlePermanentChosen(player1, ghost.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ghost)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ghost)).isEqualTo(8);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({FriendlyGhost.class})
    @DisplayName("Entering without being cast boosts only the chosen creature")
    void etbTriggersWithoutCastingAndBoostsOnlyChosenCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FriendlyGhost());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new FriendlyGhost());
        Permanent source = harness.enterBattlefieldAndReturn(player1, new FriendlyGhost());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(8);
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(source.getPowerModifier()).isZero();
        assertThat(source.getToughnessModifier()).isZero();
    }

    @Test
    @CardUsed({FriendlyGhost.class})
    @DisplayName("The triggered boost resolves after the source leaves the battlefield")
    void etbResolvesAfterSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FriendlyGhost());
        Permanent source = harness.enterBattlefieldAndReturn(player1, new FriendlyGhost());
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Friendly Ghost");
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(8);
        assertThat(gd.stack).isEmpty();
    }

    private void castAndResolve(Permanent target) {
        harness.setHand(player1, List.of(new FriendlyGhost()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
