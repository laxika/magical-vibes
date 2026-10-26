package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ElspethSunsChampion;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.p.ParallelLives;
import com.github.laxika.magicalvibes.cards.q.QueenAllenalOfRuadach;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AltaRIbnLaAhad.class, AssassinInitiate.class, GrizzlyBears.class,
        InvasionOfZendikar.class, ParallelLives.class, QueenAllenalOfRuadach.class, ElspethSunsChampion.class})
class AltaRIbnLaAhadTest extends BaseCardTest {

    @Test
    void exilesAssassinWithMemoryCounterAndCopiesAllMemoryMarkedCreatures() {
        Card graveyardAssassin = new AssassinInitiate();
        Card exiledAssassin = new AssassinInitiate();
        harness.setGraveyard(player1, List.of(graveyardAssassin));
        harness.setExile(player1, List.of(exiledAssassin));
        gd.exiledCardsWithMemoryCounters.add(exiledAssassin.getId());
        addCreatureReady(player1, new AltaRIbnLaAhad());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        PendingInteraction.MultiGraveyardChoice choice =
                (PendingInteraction.MultiGraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice.minCount()).isZero();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.handleMultipleCardsChosen(player1, List.of(graveyardAssassin.getId()));
            resolveAllTriggers();
        });

        List<Permanent> tokens = findPermanents(player1, "Assassin Initiate");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttacking()).isTrue();
            // CR 508.4: put onto the battlefield attacking, so it never "attacked".
            assertThat(token.isAttackedThisTurn()).isFalse();
            assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        });
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(graveyardAssassin.getId(), exiledAssassin.getId());
        assertThat(gd.exiledCardsWithMemoryCounters)
                .containsExactlyInAnyOrder(graveyardAssassin.getId(), exiledAssassin.getId());
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .allMatch(action -> action.kind() == DelayedPermanentActionKind.EXILE_TOKEN_AT_END_OF_COMBAT)
                .hasSize(2);
    }

    @Test
    void doesNotExileNonAssassinCreatureFromGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        addCreatureReady(player1, new AltaRIbnLaAhad());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Assassin Initiate")).isEmpty();
    }

    @Test
    void copiesMemoryMarkedNonAssassinsWithoutAGraveyardTargetAndIgnoresOtherOwners() {
        Card ownMarked = new GrizzlyBears();
        Card ownUnmarked = new AssassinInitiate();
        Card opponentMarked = new AssassinInitiate();
        harness.setExile(player1, List.of(ownMarked, ownUnmarked));
        harness.setExile(player2, List.of(opponentMarked));
        gd.exiledCardsWithMemoryCounters.addAll(List.of(ownMarked.getId(), opponentMarked.getId()));
        addCreatureReady(player1, new AltaRIbnLaAhad());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(findPermanents(player1, "Assassin Initiate")).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(ownMarked, ownUnmarked);
    }

    @Test
    void canDeclineExilingAnAssassinAndStillCopyPreviouslyMarkedCards() {
        Card graveyardAssassin = new AssassinInitiate();
        Card exiledAssassin = new AssassinInitiate();
        harness.setGraveyard(player1, List.of(graveyardAssassin));
        harness.setExile(player1, List.of(exiledAssassin));
        gd.exiledCardsWithMemoryCounters.add(exiledAssassin.getId());
        addCreatureReady(player1, new AltaRIbnLaAhad());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.handleMultipleCardsChosen(player1, List.of());
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Assassin Initiate")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardAssassin);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiledAssassin);
    }

    @Test
    void illegalGraveyardTargetPreventsTheEntireAbilityFromResolving() {
        Card target = new AssassinInitiate();
        Card previouslyMarked = new AssassinInitiate();
        harness.setGraveyard(player1, List.of(target));
        harness.setExile(player1, List.of(previouslyMarked));
        gd.exiledCardsWithMemoryCounters.add(previouslyMarked.getId());
        addCreatureReady(player1, new AltaRIbnLaAhad());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMultipleCardsChosen(player1, List.of(target.getId())));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(previouslyMarked, target));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(findPermanents(player1, "Assassin Initiate")).isEmpty();
        assertThat(gd.exiledCardsWithMemoryCounters).containsExactly(previouslyMarked.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void tokensMayAttackABattleProtectedByTheOpponent() {
        Card exiled = new AssassinInitiate();
        harness.setExile(player1, List.of(exiled));
        gd.exiledCardsWithMemoryCounters.add(exiled.getId());
        addCreatureReady(player1, new AltaRIbnLaAhad());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        battle.setProtectorPlayerId(player2.getId());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(battle.getId());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handlePermanentChosen(player1, battle.getId()));
        assertThat(findPermanent(player1, "Assassin Initiate").getAttackTarget()).isEqualTo(battle.getId());
    }

    @Test
    @DisplayName("With Queen Allenal and Parallel Lives, Altaïr's controller orders them before the copies are created")
    void asksToOrderTokenReplacementsForTheCopies() {
        attackWithAltairUnderQueenAndParallelLives(false);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.context()).isInstanceOf(ChoiceContext.ReplacementEffectOrder.class);
        assertThat(findPermanents(player1, "Assassin Initiate")).isEmpty();
    }

    @Test
    @DisplayName("Applying Queen Allenal before Parallel Lives doubles the copy and the added Soldier, all attacking")
    void queenFirstDoublesTheCopyAndTheSoldier() {
        attackWithAltairUnderQueenAndParallelLives(false);

        chooseReplacement("Queen Allenal of Ruadach");

        assertThat(findPermanents(player1, "Assassin Initiate")).hasSize(2).allSatisfy(token -> {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttacking()).isTrue();
            assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        });
        assertThat(findPermanents(player1, "Soldier")).hasSize(2)
                .allSatisfy(soldier -> assertThat(soldier.getAttackTarget()).isEqualTo(player2.getId()));
    }

    @Test
    @DisplayName("Applying Parallel Lives before Queen Allenal adds one undoubled Soldier")
    void parallelLivesFirstAddsOneSoldier() {
        attackWithAltairUnderQueenAndParallelLives(false);

        chooseReplacement("Parallel Lives");

        assertThat(findPermanents(player1, "Assassin Initiate")).hasSize(2);
        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
    }

    @Test
    @DisplayName("Each doubled copy and the added Soldier choose their own player or planeswalker to attack")
    void doubledCopiesAndAddedSoldierChooseTheirAttackTargetsIndependently() {
        Permanent elspeth = attackWithAltairUnderQueenAndParallelLives(true);

        chooseReplacement("Parallel Lives");
        chooseAttackTarget(player2.getId());
        chooseAttackTarget(elspeth.getId());
        chooseAttackTarget(elspeth.getId());

        assertThat(findPermanents(player1, "Assassin Initiate")).hasSize(2)
                .allSatisfy(token -> assertThat(token.isAttacking()).isTrue())
                .extracting(Permanent::getAttackTarget)
                .containsExactlyInAnyOrder(player2.getId(), elspeth.getId());
        assertThat(findPermanents(player1, "Soldier")).singleElement().satisfies(soldier -> {
            assertThat(soldier.isAttacking()).isTrue();
            assertThat(soldier.getAttackTarget()).isEqualTo(elspeth.getId());
        });
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Every token of the event, the added Soldier included, is exiled at end of combat")
    void addedTokensAreExiledAtEndOfCombatToo() {
        attackWithAltairUnderQueenAndParallelLives(false);

        chooseReplacement("Queen Allenal of Ruadach");

        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .filteredOn(action -> action.kind() == DelayedPermanentActionKind.EXILE_TOKEN_AT_END_OF_COMBAT)
                .hasSize(4);
    }

    @Test
    void endOfCombatExileUsesTheStackBeforeRemovingTokens() {
        Card exiled = new AssassinInitiate();
        harness.setExile(player1, List.of(exiled));
        gd.exiledCardsWithMemoryCounters.add(exiled.getId());
        addCreatureReady(player1, new AltaRIbnLaAhad());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);
        harness.passUntil(TurnStep.DECLARE_BLOCKERS);
        gs.declareBlockers(gd, player2, List.of());
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(findPermanents(player1, "Assassin Initiate")).hasSize(1);
        assertThat(gd.stack).isNotEmpty();
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Assassin Initiate")).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiled);
    }

    private Permanent attackWithAltairUnderQueenAndParallelLives(boolean withOpposingPlaneswalker) {
        Card exiled = new AssassinInitiate();
        harness.setExile(player1, List.of(exiled));
        gd.exiledCardsWithMemoryCounters.add(exiled.getId());
        addCreatureReady(player1, new AltaRIbnLaAhad());
        harness.addToBattlefield(player1, new QueenAllenalOfRuadach());
        harness.addToBattlefield(player1, new ParallelLives());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent elspeth = null;
        if (withOpposingPlaneswalker) {
            elspeth = new Permanent(new ElspethSunsChampion());
            elspeth.setCounterCount(CounterType.LOYALTY, 4);
            gd.playerBattlefields.get(player2.getId()).add(elspeth);
        }

        declareAttackers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);
        return elspeth;
    }

    private void chooseReplacement(String optionPrefix) {
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> harness.handleListChoice(player1, choice.options()
                .stream().filter(option -> option.startsWith(optionPrefix)).findFirst().orElseThrow()));
    }

    private void chooseAttackTarget(UUID targetId) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> harness.handlePermanentChosen(player1, targetId));
    }
}
