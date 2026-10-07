package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ActionNewsCrew;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TurtleBlimp.class, ActionNewsCrew.class})
class TurtleBlimpTest extends BaseCardTest {

    @Test
    void createsMutantTokenWhenItEnters() {
        harness.setHand(player1, List.of(new TurtleBlimp()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mutant")).hasSize(1);
        assertThat(findPermanents(player2, "Mutant")).isEmpty();
        Permanent token = findPermanent(player1, "Mutant");
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.MUTANT);
    }

    @Test
    void crewAnimatesBlimpUntilEndOfTurn() {
        Permanent blimp = addCreatureReady(player1, new TurtleBlimp());
        Permanent crewer = addCreatureReady(player1, new ActionNewsCrew());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, blimp)).isTrue();
        assertThat(crewer.isTapped()).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, blimp)).isFalse();
    }

    @Test
    void newlyCreatedMutantCanCrewImmediately() {
        harness.setHand(player1, List.of(new TurtleBlimp()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        Permanent blimp = findPermanent(player1, "Turtle Blimp");
        Permanent mutant = findPermanent(player1, "Mutant");

        assertThat(mutant.isSummoningSick()).isTrue();
        assertThat(mutant.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, blimp)).isFalse();
        harness.activateAbility(player1, 0, null, null);

        assertThat(mutant.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, blimp)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, blimp)).isTrue();
        assertThat(blimp.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Mutant")).hasSize(1);
    }

    @Test
    void tappedCreatureCannotPayCrewCost() {
        Permanent blimp = harness.addToBattlefieldAndReturn(player1, new TurtleBlimp());
        Permanent crewer = addCreatureReady(player1, new ActionNewsCrew());
        crewer.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power");
        assertThat(gqs.isCreature(gd, blimp)).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
