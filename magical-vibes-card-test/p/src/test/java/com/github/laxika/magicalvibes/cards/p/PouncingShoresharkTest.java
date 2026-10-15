package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PouncingShoreshark.class, AlmightyBrushwagg.class})
class PouncingShoresharkTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating may return a creature an opponent controls to its owner's hand")
    void mutatingMayReturnOpponentsCreatureToHand() {
        Permanent shark = addCreatureReady(player1, new PouncingShoreshark());
        Permanent bear = addCreatureReady(player2, new AlmightyBrushwagg());

        triggerMutation(shark);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Almighty Brushwagg");
        harness.assertInHand(player2, "Almighty Brushwagg");
    }

    @Test
    @DisplayName("Declining the mutation trigger leaves the opponent's creature on the battlefield")
    void decliningMutationTriggerDoesNotReturnCreature() {
        Permanent shark = addCreatureReady(player1, new PouncingShoreshark());
        Permanent bear = addCreatureReady(player2, new AlmightyBrushwagg());

        triggerMutation(shark);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Almighty Brushwagg");
        harness.assertNotInHand(player2, "Almighty Brushwagg");
    }

    @Test
    void canBeCastDuringOpponentsUpkeepWithoutTriggeringMutation() {
        addCreatureReady(player2, new AlmightyBrushwagg());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.castFromHand(player1, new PouncingShoreshark(), "{4}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Pouncing Shoreshark");
        harness.assertOnBattlefield(player2, "Almighty Brushwagg");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cannotTargetCreatureControlledByAbilityController() {
        Permanent shark = addCreatureReady(player1, new PouncingShoreshark());
        Permanent ownCreature = addCreatureReady(player1, new AlmightyBrushwagg());
        Permanent opposingCreature = addCreatureReady(player2, new AlmightyBrushwagg());

        triggerMutation(shark);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
    }

    @Test
    void mutationWithNoLegalTargetDoesNotAwaitInput() {
        Permanent shark = addCreatureReady(player1, new PouncingShoreshark());
        addCreatureReady(player1, new AlmightyBrushwagg());

        triggerMutation(shark);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Almighty Brushwagg");
    }

    @Test
    void targetThatChangesToControllersControlIsNotReturned() {
        Permanent shark = addCreatureReady(player1, new PouncingShoreshark());
        Permanent target = addCreatureReady(player2, new AlmightyBrushwagg());

        triggerMutation(shark);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        harness.assertNotInHand(player1, "Almighty Brushwagg");
        harness.assertNotInHand(player2, "Almighty Brushwagg");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void stolenCreatureReturnsToItsOwnersHand() {
        Permanent shark = addCreatureReady(player1, new PouncingShoreshark());
        Permanent target = addCreatureReady(player2, new AlmightyBrushwagg());
        gd.stolenCreatures.put(target.getId(), player1.getId());

        triggerMutation(shark);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Almighty Brushwagg");
        harness.assertInHand(player1, "Almighty Brushwagg");
        harness.assertNotInHand(player2, "Almighty Brushwagg");
    }

    private void triggerMutation(Permanent shark) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, shark, List.of(shark.getCard()), player1.getId()));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextSelfTriggeredAbilityTarget(gd));
    }
}
