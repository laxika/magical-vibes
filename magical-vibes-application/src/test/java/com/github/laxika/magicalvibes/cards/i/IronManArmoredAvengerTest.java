package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IronManArmoredAvenger.class, GrizzlyBears.class, Island.class})
class IronManArmoredAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing a card puts a +1/+1 counter on a target creature")
    void drawingPutsCounterOnTargetCreature() {
        harness.addToBattlefield(player1, new IronManArmoredAvenger());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The draw trigger only allows creature targets")
    void drawTriggerOnlyAllowsCreatureTargets() {
        harness.addToBattlefield(player1, new IronManArmoredAvenger());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Island());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToDraw(player1);
        harness.passBothPriorities();

        Permanent land = gd.playerBattlefields.get(player1.getId()).get(2);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking gives flying to other attacking modified creatures")
    void attackingGivesFlyingToOtherAttackingModifiedCreatures() {
        addCreatureReady(player1, new IronManArmoredAvenger());
        Permanent modifiedAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent unmodifiedAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());
        modifiedAttacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, modifiedAttacker, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, unmodifiedAttacker, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonAttacker, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The attack trigger's flying grant wears off at end of turn")
    void flyingGrantWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new IronManArmoredAvenger());
        Permanent modifiedAttacker = addCreatureReady(player1, new GrizzlyBears());
        modifiedAttacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, modifiedAttacker, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, modifiedAttacker, Keyword.FLYING)).isFalse();
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
