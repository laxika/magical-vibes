package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({ScuttlingButler.class, CivilServant.class})
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

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
