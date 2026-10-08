package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KnightOfTheKeep;
import com.github.laxika.magicalvibes.cards.r.RovingKeep;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WintermoorCommander.class, KnightOfTheKeep.class, GrizzlyBears.class, RovingKeep.class})
class WintermoorCommanderTest extends BaseCardTest {

    @Test
    void toughnessEqualsKnightsYouControl() {
        Permanent commander = addCreatureReady(player1, new WintermoorCommander());
        addCreatureReady(player1, new KnightOfTheKeep());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new KnightOfTheKeep());

        assertThat(gqs.getEffectivePower(gd, commander)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, commander)).isEqualTo(2);

        addCreatureReady(player1, new KnightOfTheKeep());

        assertThat(gqs.getEffectivePower(gd, commander)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, commander)).isEqualTo(3);
    }

    @Test
    void attackingGivesAnotherKnightYouControlIndestructibleUntilEndOfTurn() {
        Permanent commander = addCreatureReady(player1, new WintermoorCommander());
        Permanent knight = addCreatureReady(player1, new KnightOfTheKeep());
        Permanent nonKnight = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingKnight = addCreatureReady(player2, new KnightOfTheKeep());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, knight.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, commander, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, knight, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonKnight, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingKnight, Keyword.INDESTRUCTIBLE)).isFalse();

        gs.declareBlockers(gd, player2, List.of());
        advanceToNextTurn(player1);

        assertThat(gqs.hasKeyword(gd, knight, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void toughnessDecreasesWhenAnotherKnightLeaves() {
        Permanent commander = addCreatureReady(player1, new WintermoorCommander());
        Permanent knight = addCreatureReady(player1, new KnightOfTheKeep());
        assertThat(gqs.getEffectiveToughness(gd, commander)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(knight);

        assertThat(gqs.getEffectiveToughness(gd, commander)).isEqualTo(1);
    }

    @Test
    void attackTriggerRejectsSourceNonKnightAndOpposingKnight() {
        Permanent commander = addCreatureReady(player1, new WintermoorCommander());
        Permanent knight = addCreatureReady(player1, new KnightOfTheKeep());
        Permanent nonKnight = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingKnight = addCreatureReady(player2, new KnightOfTheKeep());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, commander.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonKnight.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposingKnight.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, knight.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, knight, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void anotherCommanderIsALegalTargetAndGrantSurvivesSourceLeaving() {
        Permanent commander = addCreatureReady(player1, new WintermoorCommander());
        Permanent otherCommander = addCreatureReady(player1, new WintermoorCommander());
        addCreatureReady(player2, new KnightOfTheKeep());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, otherCommander.getId());
        gd.playerBattlefields.get(player1.getId()).remove(commander);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, otherCommander, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.getEffectiveToughness(gd, otherCommander)).isEqualTo(1);
    }

    @Test
    void attackWithoutAnotherKnightDoesNotRequireTargetInput() {
        Permanent commander = addCreatureReady(player1, new WintermoorCommander());
        addCreatureReady(player2, new KnightOfTheKeep());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, commander, Keyword.INDESTRUCTIBLE)).isFalse();
    }
    @Test
    void deathtouchDestroysABlockerWithMoreToughnessThanCommanderPower() {
        addCreatureReady(player1, new WintermoorCommander());
        addCreatureReady(player2, new RovingKeep());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player2, "Roving Keep");
        harness.assertNotOnBattlefield(player2, "Roving Keep");
    }
    private void advanceToNextTurn(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
