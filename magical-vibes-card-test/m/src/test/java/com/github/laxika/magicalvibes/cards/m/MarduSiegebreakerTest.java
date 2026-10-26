package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Panharmonicon;
import com.github.laxika.magicalvibes.cards.p.ParallelLives;
import com.github.laxika.magicalvibes.cards.q.QueenAllenalOfRuadach;
import com.github.laxika.magicalvibes.cards.w.WorldwalkerHelm;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarduSiegebreaker.class, GrizzlyBears.class, Panharmonicon.class, ParallelLives.class,
        QueenAllenalOfRuadach.class, Memnite.class, WorldwalkerHelm.class})
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
        resolveAllTriggers();

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
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void doesNotSacrificeTokenControlledByOpponentAtCreatorsEndStep() {
        Permanent token = createAttackingBearToken();
        setOwnerAndController(token, player1, player2);
        token.recordControlChange();
        token.setAttacking(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Grizzly Bears")).containsExactly(token);
    }

    @Test
    @DisplayName("With Queen Allenal and Parallel Lives, the controller orders them before the copy enters")
    void asksToOrderTokenReplacements() {
        attackUnderQueenAndParallelLives();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.context()).isInstanceOf(ChoiceContext.ReplacementEffectOrder.class);
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
    }

    @Test
    @DisplayName("Applying Queen Allenal first doubles the copy and the Soldier, all tapped, attacking and sacrificed")
    void queenFirstDoublesCopyAndSoldier() {
        attackUnderQueenAndParallelLives();

        chooseReplacement("Queen Allenal of Ruadach");

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttacking()).isTrue();
            assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        });
        assertThat(findPermanents(player1, "Soldier")).hasSize(2).allSatisfy(token -> {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttacking()).isTrue();
            assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        });

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }

    @Test
    @DisplayName("Applying Parallel Lives first doubles the copy and adds one undoubled Soldier")
    void parallelLivesFirstAddsOneSoldier() {
        attackUnderQueenAndParallelLives();

        chooseReplacement("Parallel Lives");

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2)
                .allSatisfy(token -> assertThat(token.getAttackTarget()).isEqualTo(player2.getId()));
        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
    }

    @Test
    @DisplayName("A noncreature token added to the event enters tapped but not attacking")
    void addedNoncreatureTokenIsNotAttacking() {
        Permanent memnite = harness.addToBattlefieldAndReturn(player1, new Memnite());
        castMarduSiegebreaker();
        harness.handlePermanentChosen(player1, memnite.getId());
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new WorldwalkerHelm());

        Permanent source = findPermanent(player1, "Mardu Siegebreaker");
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(source)));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, harness::passBothPriorities);

        Permanent copy = findPermanent(player1, "Memnite");
        assertThat(copy.isAttacking()).isTrue();
        assertThat(copy.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(findPermanents(player1, "Map")).hasSize(1)
                .allSatisfy(map -> assertThat(map.isAttacking()).isFalse());
    }

    private void attackUnderQueenAndParallelLives() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castMarduSiegebreaker();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new QueenAllenalOfRuadach());
        harness.addToBattlefield(player1, new ParallelLives());

        Permanent source = findPermanent(player1, "Mardu Siegebreaker");
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(source)));
        harness.passBothPriorities();
    }

    private void chooseReplacement(String optionPrefix) {
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> harness.handleListChoice(player1,
                choice.options().stream().filter(option -> option.startsWith(optionPrefix)).findFirst().orElseThrow()));
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
