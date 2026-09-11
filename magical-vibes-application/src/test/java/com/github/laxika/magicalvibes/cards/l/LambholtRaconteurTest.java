package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LambholtRaconteur.class, LambholtRavager.class, Opt.class, GrizzlyBears.class})
class LambholtRaconteurTest extends BaseCardTest {

    @Test
    void frontFaceDealsOneDamageForControllerNoncreatureSpell() {
        harness.addToBattlefield(player1, new LambholtRaconteur());
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    void frontFaceDoesNotTriggerForCreatureSpell() {
        harness.addToBattlefield(player1, new LambholtRaconteur());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    void backFaceDealsTwoDamageForControllerNoncreatureSpell() {
        harness.addToBattlefield(player1, new LambholtRavager());
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void opponentNoncreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new LambholtRaconteur());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Opt()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    void dayNightTransformsBothFaces() {
        gd.dayNight = DayNight.DAY;
        Permanent raconteur = harness.addToBattlefieldAndReturn(player1, new LambholtRaconteur());

        gd.spellsCastLastTurn.clear();
        advanceToNextTurn(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(raconteur.getCard()).isInstanceOf(LambholtRavager.class);

        gd.recordSpellCast(player1.getId(), new Opt());
        gd.recordSpellCast(player1.getId(), new Opt());
        advanceToNextTurn(player2);

        assertThat(raconteur.getCard()).isInstanceOf(LambholtRaconteur.class);
    }

    private void advanceToNextTurn(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
