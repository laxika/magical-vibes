package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.p.ParallelLives;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AltaRIbnLaAhad.class, AssassinInitiate.class, GrizzlyBears.class,
        InvasionOfZendikar.class, ParallelLives.class})
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
        harness.handleMultipleCardsChosen(player1, List.of(graveyardAssassin.getId()));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());

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
        resolveAllTriggers();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handlePermanentChosen(player1, player2.getId()));

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
        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handlePermanentChosen(player1, player2.getId()));

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
    void doubledTokensEachHaveAnAttackTarget() {
        Card exiled = new AssassinInitiate();
        harness.setExile(player1, List.of(exiled));
        gd.exiledCardsWithMemoryCounters.add(exiled.getId());
        addCreatureReady(player1, new AltaRIbnLaAhad());
        harness.addToBattlefield(player1, new ParallelLives());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            while (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
                harness.handlePermanentChosen(player1, player2.getId());
            }
        });

        assertThat(findPermanents(player1, "Assassin Initiate")).hasSize(2).allSatisfy(token -> {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        });
    }

    @Test
    void endOfCombatExileUsesTheStackBeforeRemovingTokens() {
        Card exiled = new AssassinInitiate();
        harness.setExile(player1, List.of(exiled));
        gd.exiledCardsWithMemoryCounters.add(exiled.getId());
        addCreatureReady(player1, new AltaRIbnLaAhad());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handlePermanentChosen(player1, player2.getId()));
        harness.passUntil(TurnStep.DECLARE_BLOCKERS);
        gs.declareBlockers(gd, player2, List.of());
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(findPermanents(player1, "Assassin Initiate")).hasSize(1);
        assertThat(gd.stack).isNotEmpty();
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Assassin Initiate")).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiled);
    }
}
