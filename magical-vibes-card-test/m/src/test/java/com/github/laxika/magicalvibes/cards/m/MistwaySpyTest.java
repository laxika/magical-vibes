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
        resolveAllTriggers();

        assertThat(spy.isFaceDown()).isTrue();
        harness.assertOnBattlefield(player1, "Mistway Spy");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void investigateContinuesAfterSpyLeavesBattlefield() {
        Permanent attacker = addCreatureReady(player1, new MistwaySpy());
        Permanent spy = castFaceDown();
        turnFaceUp(spy);
        resolveAllTriggers();

        destroySpyWithShock(spy);
        harness.assertInGraveyard(player1, "Mistway Spy");
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void removingSpyInResponseDoesNotPreventDelayedInvestigateTrigger() {
        Permanent attacker = addCreatureReady(player1, new MistwaySpy());
        Permanent spy = castFaceDown();
        turnFaceUp(spy);

        destroySpyWithShock(spy);
        harness.assertInGraveyard(player1, "Mistway Spy");
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void castingSpyFaceUpDoesNotCreateInvestigateTrigger() {
        harness.setHand(player1, List.of(new MistwaySpy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent spy = findPermanent(player1, "Mistway Spy");
        spy.setSummoningSick(false);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(spy)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    private void turnFaceUp(Permanent spy) {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(spy));
    }

    @Test
    void faceDownSpyDoesNotInvestigateOnCombatDamage() {
        Permanent spy = castFaceDown();
        spy.setSummoningSick(false);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(spy)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void opponentsCombatDamageDoesNotInvestigate() {
        Permanent attacker = addCreatureReady(player2, new MistwaySpy());
        Permanent spy = castFaceDown();
        turnFaceUp(spy);
        resolveAllTriggers();

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(attacker)));
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    void investigateTriggerExpiresAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new MistwaySpy());
        Permanent spy = castFaceDown();
        turnFaceUp(spy);
        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    private void destroySpyWithShock(Permanent spy) {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, spy.getId());
        resolveAllTriggers();
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
