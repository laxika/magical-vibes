package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummitIntimidator.class})
class SummitIntimidatorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes the targeted creature unable to block this turn")
    void etbMakesTargetUnableToBlock() {
        Permanent blocker = addCreatureReady(player2, new SummitIntimidator());
        harness.setHand(player1, List.of(new SummitIntimidator()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0, 0, blocker.getId());
        resolveAllTriggers();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The restriction wears off at end of turn")
    void restrictionWearsOffAtEndOfTurn() {
        Permanent blocker = addCreatureReady(player2, new SummitIntimidator());
        harness.setHand(player1, List.of(new SummitIntimidator()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0, 0, blocker.getId());
        resolveAllTriggers();
        assertThat(blocker.isCantBlockThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The targeted creature cannot legally block until the turn ends")
    void restrictionAffectsBlockLegalityThroughEndStep() {
        Permanent target = addCreatureReady(player2, new SummitIntimidator());
        harness.setHand(player1, List.of(new SummitIntimidator()));
        harness.addMana(player1, ManaColor.RED, 4);
        assertThat(bls.canBlock(gd, target)).isTrue();

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(bls.canBlock(gd, target)).isFalse();
        harness.forceStep(TurnStep.END_STEP);
        assertThat(bls.canBlock(gd, target)).isFalse();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(bls.canBlock(gd, target)).isTrue();
    }

    @Test
    @DisplayName("The ETB can target a creature its controller controls")
    void canTargetOwnCreature() {
        Permanent target = addCreatureReady(player1, new SummitIntimidator());
        harness.setHand(player1, List.of(new SummitIntimidator()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Only the targeted creature is prevented from blocking")
    void onlyTargetIsRestricted() {
        Permanent target = addCreatureReady(player2, new SummitIntimidator());
        Permanent other = addCreatureReady(player2, new SummitIntimidator());
        harness.setHand(player1, List.of(new SummitIntimidator()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(other.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The ETB resolves even after its source leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Permanent target = addCreatureReady(player2, new SummitIntimidator());
        harness.setHand(player1, List.of(new SummitIntimidator()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        Permanent source = gd.playerBattlefields.get(player1.getId()).removeFirst();
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        resolveAllTriggers();

        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("An ETB whose target leaves does not restrict another creature")
    void removedTargetDoesNotRestrictAnotherCreature() {
        Permanent target = addCreatureReady(player2, new SummitIntimidator());
        Permanent other = addCreatureReady(player2, new SummitIntimidator());
        harness.setHand(player1, List.of(new SummitIntimidator()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        resolveAllTriggers();

        assertThat(other.isCantBlockThisTurn()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
