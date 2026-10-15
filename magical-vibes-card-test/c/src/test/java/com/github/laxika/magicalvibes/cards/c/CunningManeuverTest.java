package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ErdwalIlluminator;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.TurtleDuck;
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

@CardUsed({CunningManeuver.class, GrizzlyBears.class, Plains.class, ErdwalIlluminator.class, TurtleDuck.class})
class CunningManeuverTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts the target creature by +3/+1 and creates a Clue")
    void boostsTargetAndCreatesClue() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castCunningManeuver(bear);

        assertThat(bear.getPowerModifier()).isEqualTo(3);
        assertThat(bear.getToughnessModifier()).isEqualTo(1);
        assertThat(bear.getEffectivePower()).isEqualTo(5);
        assertThat(bear.getEffectiveToughness()).isEqualTo(3);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("The boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castCunningManeuver(bear);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Does not create a Clue when the target is illegal on resolution")
    void fizzlesWithoutCreatingClueWhenTargetLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCunningManeuver();
        harness.castInstant(player1, 0, bear.getId());
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        prepareCunningManeuver();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, plains.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can boost your own creature and sacrifice the resulting Clue to draw a card")
    void ownCreatureAndClueDrawAbility() {
        Permanent duck = harness.addToBattlefieldAndReturn(player1, new TurtleDuck());
        Plains drawnCard = new Plains();
        harness.setLibrary(player1, List.of(drawnCard));
        castCunningManeuver(duck);

        assertThat(duck.getEffectivePower()).isEqualTo(3);
        assertThat(duck.getEffectiveToughness()).isEqualTo(5);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
        Permanent clue = findPermanent(player1, "Clue");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Creating a Clue directly does not trigger investigate abilities")
    void directClueCreationDoesNotInvestigate() {
        Permanent illuminator = harness.addToBattlefieldAndReturn(player1, new ErdwalIlluminator());
        castCunningManeuver(illuminator);
        resolveAllTriggers();

        assertThat(illuminator.getEffectivePower()).isEqualTo(4);
        assertThat(illuminator.getEffectiveToughness()).isEqualTo(4);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    private void castCunningManeuver(Permanent target) {
        prepareCunningManeuver();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void prepareCunningManeuver() {
        harness.setHand(player1, List.of(new CunningManeuver()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
