package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChillHaunting.class, GrizzlyBears.class, Shock.class, FountainOfYouth.class})
class ChillHauntingTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling creature cards sets X and gives the target creature -X/-X")
    void debuffsTargetByNumberOfExiledCreatureCards() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChillHaunting()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, target.getId(), List.of(0));

        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getXValue()).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(1)
                .allMatch(Shock.class::isInstance);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exiling enough creature cards gives a lethal -X/-X")
    void lethalDebuffDestroysTarget() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChillHaunting()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, target.getId(), List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Exiling zero creature cards leaves the target unchanged")
    void zeroExilesLeavesTargetUnchanged() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChillHaunting()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, target.getId(), List.of());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("The temporary debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChillHaunting()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, target.getId(), List.of(0));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Only creature cards can be exiled for the additional cost")
    void cannotExileNonCreatureCards() {
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new ChillHaunting()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstantWithMultipleGraveyardExile(
                player1, 0, player2.getId(), List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The spell cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new ChillHaunting()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstantWithMultipleGraveyardExile(
                player1, 0, artifact.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
