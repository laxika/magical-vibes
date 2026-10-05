package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Panharmonicon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarduSiegebreaker.class, GrizzlyBears.class, Panharmonicon.class})
class MarduSiegebreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by exiling up to one other creature you control")
    void entersAndExilesAnotherCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castMarduSiegebreaker();

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mardu Siegebreaker");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        Permanent source = findPermanent(player1, "Mardu Siegebreaker");
        assertThat(gd.getCardsExiledByPermanent(source.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Does not exile the target if it leaves before the enters ability resolves")
    void doesNotExileWhenSourceLeavesBeforeResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castMarduSiegebreaker();

        Permanent source = findPermanent(player1, "Mardu Siegebreaker");
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Creates a tapped attacking copy for each opponent and sacrifices it at the next end step")
    void createsAttackingCopyForEachOpponent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castMarduSiegebreaker();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        Permanent source = findPermanent(player1, "Mardu Siegebreaker");
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(source)));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, harness::passBothPriorities);

        List<Permanent> tokens = findPermanents(player1, "Grizzly Bears");
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.get(0);
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.isAttackedThisTurn()).isFalse();
        assertThat(token.getAttackTarget()).isEqualTo(player2.getId());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
    }

    @Test
    void canEnterWithoutAnotherCreatureAndAttackWithoutCreatingTokens() {
        castMarduSiegebreaker();
        harness.passBothPriorities();

        Permanent source = findPermanent(player1, "Mardu Siegebreaker");
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(source)));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, harness::passBothPriorities);

        assertThat(findPermanents(player1, "Mardu Siegebreaker")).containsExactly(source);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void canChooseNoTargetEvenWhenAnotherCreatureIsAvailable() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castMarduSiegebreaker();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).containsExactly(bears);
        Permanent source = findPermanent(player1, "Mardu Siegebreaker");
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(source)));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, harness::passBothPriorities);

        assertThat(findPermanents(player1, "Grizzly Bears")).containsExactly(bears);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void returnsExiledCreatureWhenSiegebreakerLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castMarduSiegebreaker();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        Permanent source = findPermanent(player1, "Mardu Siegebreaker");

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().tryDestroyPermanent(gd, source));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Mardu Siegebreaker");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(findPermanent(player1, "Grizzly Bears").getId()).isNotEqualTo(bears.getId());
    }

    @Test
    void copiesEachCardExiledByDoubledEntersAbility() {
        harness.addToBattlefield(player1, new Panharmonicon());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castMarduSiegebreaker();
        harness.handlePermanentChosen(player1, first.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        Permanent source = findPermanent(player1, "Mardu Siegebreaker");
        assertThat(gd.getCardsExiledByPermanent(source.getId())).hasSize(2);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(source)));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, harness::passBothPriorities);

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2)
                .allSatisfy(token -> {
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(token.isTapped()).isTrue();
                    assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
                });

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void tokensSurviveOpponentsEndStepWhenTheirCreatorsTurnEndedEarly() {
        Permanent token = createAttackingBearToken();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).containsExactly(token);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void doesNotSacrificeTokenControlledByOpponentAtCreatorsEndStep() {
        Permanent token = createAttackingBearToken();
        gd.playerBattlefields.get(player1.getId()).remove(token);
        gd.playerBattlefields.get(player2.getId()).add(token);
        gd.stolenCreatures.put(token.getId(), player1.getId());
        token.recordControlChange();
        token.setAttacking(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Grizzly Bears")).containsExactly(token);
    }

    private Permanent createAttackingBearToken() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castMarduSiegebreaker();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        Permanent source = findPermanent(player1, "Mardu Siegebreaker");
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(source)));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, harness::passBothPriorities);
        return findPermanent(player1, "Grizzly Bears");
    }

    private void castMarduSiegebreaker() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new MarduSiegebreaker(), "{1}{R}{W}{B}");
        harness.passBothPriorities();
    }
}
