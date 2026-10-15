package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.RavenousSailback;
import com.github.laxika.magicalvibes.cards.r.RealmbreakerTheInvasionTree;
import com.github.laxika.magicalvibes.cards.y.YargleAndMultani;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DjeruAndHazoret.class, Forest.class, Mountain.class, RavenousSailback.class,
        RealmbreakerTheInvasionTree.class, YargleAndMultani.class})
class DjeruAndHazoretTest extends BaseCardTest {

    @Test
    void gainsHasteAndVigilanceWithOneOrFewerCardsInHand() {
        Permanent djeruAndHazoret = harness.addToBattlefieldAndReturn(player1, new DjeruAndHazoret());
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));

        assertThat(djeruAndHazoret.isAttacking()).isTrue();
        assertThat(djeruAndHazoret.isTapped()).isFalse();
    }

    @Test
    void doesNotGainHasteWithMoreThanOneCardInHand() {
        harness.addToBattlefield(player1, new DjeruAndHazoret());
        harness.setHand(player1, List.of(new Forest(), new Mountain()));

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void attacksToOfferOnlyLegendaryCreatureAndCastItForFree() {
        addReadyDjeruAndHazoret();
        Card forest = new Forest();
        Card sailback = new RavenousSailback();
        Card yargle = new YargleAndMultani();
        harness.setLibrary(player1, List.of(forest, sailback, yargle, new Mountain(), new Forest(), new RavenousSailback(), new Mountain()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(yargle);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(yargle);
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(yargle.getId());

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        harness.castFromExile(player1, yargle.getId());
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == yargle
                && entry.getEntryType() == StackEntryType.CREATURE_SPELL);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard() == yargle);
    }

    @Test
    void mayDeclineAndPutAllLookedCardsOnBottom() {
        addReadyDjeruAndHazoret();
        Card forest = new Forest();
        Card sailback = new RavenousSailback();
        Card yargle = new YargleAndMultani();
        harness.setLibrary(player1, List.of(forest, sailback, yargle, new Mountain(), new Forest(), new RavenousSailback(), new Mountain()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(7);
    }

    private Permanent addReadyDjeruAndHazoret() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        return addCreatureReady(player1, new DjeruAndHazoret());
    }

    @Test
    void gainsHasteAndVigilanceWithExactlyOneCardInItsControllersHand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DjeruAndHazoret());
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Forest(), new Mountain()));

        declareAttackers(List.of(0));

        assertThat(creature.isAttacking()).isTrue();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void readyCreatureAttacksTappedWithTwoCardsInHand() {
        Permanent creature = addReadyDjeruAndHazoret();
        harness.setHand(player1, List.of(new Forest(), new Mountain()));

        declareAttackers(List.of(0));

        assertThat(creature.isAttacking()).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void gainingCardsAfterAttackingDoesNotTapOrRemoveAttacker() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DjeruAndHazoret());
        harness.setHand(player1, List.of());
        declareAttackers(List.of(0));

        harness.setHand(player1, List.of(new Forest(), new Mountain()));

        assertThat(creature.isAttacking()).isTrue();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void noLegendaryCreaturePutsCardsOnBottomWithoutOfferingAnOrderChoice() {
        addReadyDjeruAndHazoret();
        List<Card> looked = List.of(new RealmbreakerTheInvasionTree(), new Mountain(), new Forest(),
                new Mountain(), new Forest(), new Mountain());
        Card untouched = new Forest();
        harness.setLibrary(player1, List.of(looked.get(0), looked.get(1), looked.get(2),
                looked.get(3), looked.get(4), looked.get(5), untouched));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 7))
                .containsExactlyInAnyOrderElementsOf(looked);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void shortLibraryAllowsSelectingALegendaryCreatureAndBottomsTheRest() {
        addReadyDjeruAndHazoret();
        Card legendary = new DjeruAndHazoret();
        Card forest = new Forest();
        Card mountain = new Mountain();
        harness.setLibrary(player1, List.of(forest, legendary, mountain));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(legendary);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(legendary);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, mountain);
    }

    @Test
    void emptyLibraryResolvesWithoutAChoice() {
        addReadyDjeruAndHazoret();
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void freeCreatureCastStillRequiresMainPhaseTiming() {
        addReadyDjeruAndHazoret();
        Card legendary = new YargleAndMultani();
        harness.setLibrary(player1, List.of(legendary, new Forest(), new Mountain()));
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        assertThatThrownBy(() -> harness.castFromExile(player1, legendary.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(legendary);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castFromExile(player1, legendary.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == legendary);
    }

    @Test
    void exiledCreatureCannotBeCastAfterTheTurnEnds() {
        addReadyDjeruAndHazoret();
        harness.setHand(player1, List.of());
        Card legendary = new DjeruAndHazoret();
        harness.setLibrary(player1, List.of(legendary, new Forest(), new Mountain(),
                new Forest(), new Mountain(), new Forest(), new Mountain()));
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromExile(player1, legendary.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(legendary);
    }

    @Test
    void opponentDoesNotReceivePermissionToCastTheExiledCreature() {
        addReadyDjeruAndHazoret();
        Card legendary = new DjeruAndHazoret();
        harness.setLibrary(player1, List.of(legendary, new Forest(), new Mountain()));
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromExile(player2, legendary.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(legendary);
    }
}
