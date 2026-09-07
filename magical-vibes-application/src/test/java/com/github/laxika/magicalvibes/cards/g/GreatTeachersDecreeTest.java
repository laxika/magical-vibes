package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreatTeachersDecree.class, GrizzlyBears.class})
class GreatTeachersDecreeTest extends BaseCardTest {

    @Test
    void boostsCreaturesYouControl() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(4);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(opposingCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opposingCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void reboundOffersAFreeCastAtNextUpkeep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GreatTeachersDecree card = new GreatTeachersDecree();
        harness.setHand(player1, List.of(card));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        harness.assertInGraveyard(player1, "Great Teacher's Decree");
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    private void cast() {
        harness.setHand(player1, List.of(new GreatTeachersDecree()));
        addMana();
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
