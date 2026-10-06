package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BrokersAscendancy;
import com.github.laxika.magicalvibes.cards.c.CivilServant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScuttlingButler.class, CivilServant.class, BrokersAscendancy.class})
class ScuttlingButlerTest extends BaseCardTest {

    @Test
    void gainsDoubleStrikeWithTwoMulticoloredPermanents() {
        Permanent butler = addCreatureReady(player1, new ScuttlingButler());
        addCreatureReady(player1, new CivilServant());
        addCreatureReady(player1, new CivilServant());

        advanceToCombat(player1);

        assertThat(gqs.hasKeyword(gd, butler, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void doesNotGainDoubleStrikeWithFewerThanTwoMulticoloredPermanents() {
        Permanent butler = addCreatureReady(player1, new ScuttlingButler());
        addCreatureReady(player1, new CivilServant());

        advanceToCombat(player1);

        assertThat(gqs.hasKeyword(gd, butler, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void doubleStrikeWearsOffAtEndOfTurn() {
        Permanent butler = addCreatureReady(player1, new ScuttlingButler());
        addCreatureReady(player1, new CivilServant());
        addCreatureReady(player1, new CivilServant());

        advanceToCombat(player1);
        assertThat(gqs.hasKeyword(gd, butler, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.inMutationScope(() -> GameTestEngineContext.get()
                .getBean(TurnCleanupService.class)
                .applyCleanupResets(gd));

        assertThat(gqs.hasKeyword(gd, butler, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void multicoloredNoncreaturePermanentsCount() {
        Permanent butler = addCreatureReady(player1, new ScuttlingButler());
        harness.addToBattlefield(player1, new BrokersAscendancy());
        harness.addToBattlefield(player1, new BrokersAscendancy());

        advanceToCombat(player1);

        assertThat(gqs.hasKeyword(gd, butler, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void oneThreeColoredPermanentIsNotTwoMulticoloredPermanents() {
        Permanent butler = addCreatureReady(player1, new ScuttlingButler());
        harness.addToBattlefield(player1, new BrokersAscendancy());

        advanceToCombat(player1);

        assertThat(gqs.hasKeyword(gd, butler, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void doesNotTriggerDuringOpponentsCombat() {
        Permanent butler = addCreatureReady(player1, new ScuttlingButler());
        addCreatureReady(player1, new CivilServant());
        addCreatureReady(player1, new CivilServant());

        advanceToCombat(player2);

        assertThat(gqs.hasKeyword(gd, butler, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void opponentsMulticoloredPermanentsDoNotCount() {
        Permanent butler = addCreatureReady(player1, new ScuttlingButler());
        addCreatureReady(player1, new CivilServant());
        addCreatureReady(player2, new CivilServant());

        advanceToCombat(player1);

        assertThat(gqs.hasKeyword(gd, butler, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void conditionMustStillBeMetWhenTriggerResolves() {
        Permanent butler = addCreatureReady(player1, new ScuttlingButler());
        addCreatureReady(player1, new CivilServant());
        Permanent servant = addCreatureReady(player1, new CivilServant());

        beginCombat(player1);
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> gd.playerBattlefields.get(player1.getId()).remove(servant));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, butler, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void gainingSecondMulticoloredPermanentAfterCombatBeginsDoesNotTrigger() {
        Permanent butler = addCreatureReady(player1, new ScuttlingButler());
        addCreatureReady(player1, new CivilServant());

        beginCombat(player1);
        assertThat(gd.stack).isEmpty();
        addCreatureReady(player1, new CivilServant());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, butler, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void losingMulticoloredPermanentAfterResolutionDoesNotRemoveDoubleStrike() {
        Permanent butler = addCreatureReady(player1, new ScuttlingButler());
        addCreatureReady(player1, new CivilServant());
        Permanent servant = addCreatureReady(player1, new CivilServant());

        advanceToCombat(player1);
        harness.inMutationScope(() -> gd.playerBattlefields.get(player1.getId()).remove(servant));

        assertThat(gqs.hasKeyword(gd, butler, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    private void advanceToCombat(Player activePlayer) {
        beginCombat(activePlayer);
        harness.passBothPriorities();
    }

    private void beginCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
