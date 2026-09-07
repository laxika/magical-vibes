package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MistwaySpy.class, GrizzlyBears.class, Shock.class})
class MistwaySpyTest extends BaseCardTest {

    @Test
    void turningFaceUpMakesControlledCreaturesInvestigateOnCombatDamage() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent spy = castFaceDown();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(spy));
        harness.passBothPriorities();

        spy.setSummoningSick(false);
        int bearsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bears);
        int spyIndex = gd.playerBattlefields.get(player1.getId()).indexOf(spy);
        declareAttackers(List.of(bearsIndex, spyIndex));
        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    void disguiseWardCountersSpellTargetingFaceDownSpy() {
        Permanent spy = castFaceDown();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, spy.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(spy.isFaceDown()).isTrue();
        harness.assertInGraveyard(player2, "Shock");
    }

    private Permanent castFaceDown() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MistwaySpy()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player1, "Mistway Spy");
    }
}
