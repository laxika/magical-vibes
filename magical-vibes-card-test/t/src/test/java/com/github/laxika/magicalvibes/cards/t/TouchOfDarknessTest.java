package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CatWarriors;
import com.github.laxika.magicalvibes.cards.q.Quagmire;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({TouchOfDarkness.class, CatWarriors.class, Quagmire.class})
class TouchOfDarknessTest extends BaseCardTest {

    @Test
    @DisplayName("Makes one or more target creatures black until end of turn")
    void makesAllTargetsBlack() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CatWarriors());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new CatWarriors());

        cast(List.of(ownCreature.getId(), opposingCreature.getId()));

        assertThat(gqs.getEffectiveColors(gd, ownCreature)).containsExactly(CardColor.BLACK);
        assertThat(gqs.getEffectiveColors(gd, opposingCreature)).containsExactly(CardColor.BLACK);
    }

    @Test
    @DisplayName("The color change wears off at end of turn")
    void colorChangeWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CatWarriors());

        cast(List.of(creature.getId()));

        assertThat(gqs.getEffectiveColors(gd, creature)).containsExactly(CardColor.BLACK);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, creature)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new Quagmire());
        harness.setHand(player1, List.of(new TouchOfDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only the selected creatures become black")
    void leavesUntargetedCreaturesUnchanged() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CatWarriors());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new CatWarriors());

        cast(List.of(target.getId()));

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLACK);
        assertThat(gqs.getEffectiveColors(gd, other)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("Requires at least one target")
    void cannotCastWithoutTargets() {
        harness.setHand(player1, List.of(new TouchOfDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.<java.util.UUID>of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target more than ninety-nine creatures")
    void canTargetOneHundredCreatures() {
        List<Permanent> creatures = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            creatures.add(harness.addToBattlefieldAndReturn(player2, new CatWarriors()));
        }

        cast(creatures.stream().map(Permanent::getId).toList());

        for (Permanent creature : creatures) {
            assertThat(gqs.getEffectiveColors(gd, creature)).containsExactly(CardColor.BLACK);
        }
    }
    @Test
    @DisplayName("Cannot select the same creature twice")
    void cannotRepeatTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CatWarriors());
        harness.setHand(player1, List.of(new TouchOfDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Still affects remaining legal targets when another target leaves")
    void resolvesForRemainingLegalTargets() {
        Permanent remaining = harness.addToBattlefieldAndReturn(player1, new CatWarriors());
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new CatWarriors());
        harness.setHand(player1, List.of(new TouchOfDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, List.of(departed.getId(), remaining.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(departed);
        harness.setGraveyard(player2, List.of(departed.getCard()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, remaining)).containsExactly(CardColor.BLACK);
        harness.assertInGraveyard(player1, "Touch of Darkness");
    }
    private void cast(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new TouchOfDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, targetIds);
    }
}
