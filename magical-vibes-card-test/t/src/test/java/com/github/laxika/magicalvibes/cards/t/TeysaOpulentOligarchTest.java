package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.n.NoviceInspector;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TeysaOpulentOligarch.class, NoviceInspector.class, Shock.class})
class TeysaOpulentOligarchTest extends BaseCardTest {

    @Test
    void investigatesForEachOpponentWhoLostLifeThisTurn() {
        harness.addToBattlefield(player1, new TeysaOpulentOligarch());
        dealDamageToOpponent();

        advanceToEndStep();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void doesNotInvestigateWhenNoOpponentLostLifeThisTurn() {
        harness.addToBattlefield(player1, new TeysaOpulentOligarch());

        advanceToEndStep();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void createsOnlyOneSpiritWhenMultipleCluesArePutIntoGraveyardInOneTurn() {
        harness.addToBattlefield(player1, new TeysaOpulentOligarch());
        castNoviceInspector();
        dealDamageToOpponent();
        advanceToEndStep();

        List<Permanent> clues = List.copyOf(findPermanents(player1, "Clue"));
        assertThat(clues).hasSize(2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        for (Permanent clue : clues) {
            int index = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
            harness.activateAbility(player1, index, null, null);
            resolveAllTriggers();
        }

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    void investigatesOnlyOnceForAnOpponentWhoLostLifeMultipleTimes() {
        harness.addToBattlefield(player1, new TeysaOpulentOligarch());
        dealDamageToOpponent();
        dealDamageToOpponent();

        advanceToEndStep();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void doesNotInvestigateForItsControllersLifeLoss() {
        harness.addToBattlefield(player1, new TeysaOpulentOligarch());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        advanceToEndStep();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void doesNotInvestigateDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new TeysaOpulentOligarch());
        dealDamageToOpponent();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void opposingClueDoesNotTriggerOrConsumeTheOncePerTurnLimit() {
        harness.addToBattlefield(player1, new TeysaOpulentOligarch());
        harness.enterBattlefieldAndReturn(player2, new NoviceInspector());
        resolveAllTriggers();
        Permanent opposingClue = findPermanent(player2, "Clue");
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(opposingClue), null, null);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Spirit")).isEmpty();

        castNoviceInspector();
        Permanent ownClue = findPermanent(player1, "Clue");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(ownClue), null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getEffectivePower()).isEqualTo(1);
        assertThat(spirit.getEffectiveToughness()).isEqualTo(1);
        assertThat(spirit.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    void createsSpiritDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new TeysaOpulentOligarch());
        castNoviceInspector();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent clue = findPermanent(player1, "Clue");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    void canTriggerAgainOnTheNextTurn() {
        harness.addToBattlefield(player1, new TeysaOpulentOligarch());
        castNoviceInspector();
        castNoviceInspector();
        Permanent firstClue = findPermanent(player1, "Clue");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(firstClue), null, null);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        Permanent secondClue = findPermanent(player1, "Clue");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(secondClue), null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
    }

    @Test
    void nonClueDeathDoesNotConsumeTheOncePerTurnLimit() {
        harness.addToBattlefield(player1, new TeysaOpulentOligarch());
        castNoviceInspector();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, findPermanent(player1, "Novice Inspector").getId());
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Spirit")).isEmpty();

        Permanent clue = findPermanent(player1, "Clue");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    private void castNoviceInspector() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new NoviceInspector()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();
    }

    private void dealDamageToOpponent() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
