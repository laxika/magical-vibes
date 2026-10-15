package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IchorWellspring;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Panharmonicon;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SaheeliRai.class, GrizzlyBears.class, IchorWellspring.class, MindStone.class,
        Ornithopter.class, Clone.class, Panharmonicon.class, PropheticPrism.class})
class SaheeliRaiTest extends BaseCardTest {

    @Test
    @DisplayName("+1 scries 1 and deals 1 damage to each opponent")
    void plusOneScriesAndDamagesEachOpponent() {
        Permanent saheeli = addReadySaheeli(3);
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
        assertThat(saheeli.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("-2 creates an hasty artifact token copy and exiles it at the next end step")
    void minusTwoCreatesHastyArtifactTokenAndExilesIt() {
        addReadySaheeli(3);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
    }

    @Test
    @DisplayName("-2 cannot target an artifact or creature an opponent controls")
    void minusTwoRequiresArtifactOrCreatureYouControl() {
        addReadySaheeli(3);
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-7 puts up to three differently named artifact cards onto the battlefield")
    void minusSevenSearchesForThreeDifferentArtifactNames() {
        addReadySaheeli(7);
        harness.setLibrary(player1, List.of(
                new MindStone(), new MindStone(), new IchorWellspring(), new Ornithopter(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch firstSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(firstSearch.params().cards()).allMatch(card -> card.hasType(CardType.ARTIFACT));
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Mind Stone")).hasSize(1);
        assertThat(findPermanents(player1, "Ichor Wellspring")).hasSize(1);
        assertThat(findPermanents(player1, "Ornithopter")).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Mind Stone"));
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void plusOneStillDealsDamageWithAnEmptyLibrary() {
        addReadySaheeli(3);
        harness.setLibrary(player1, List.of());
        int ownLife = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(ownLife);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 1);
    }

    @Test
    void minusTwoCanCopyANoncreatureArtifactAndCopiesItsManaAbility() {
        addReadySaheeli(3);
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new MindStone());

        harness.activateAbility(player1, 0, 1, null, stone.getId());
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Mind Stone").stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);
        harness.activateAbility(player1, tokenIndex, 0, null, null);

        assertThat(token.isTapped()).isTrue();
    }

    @Test
    void hasteGrantedToTheTokenIsNotCopiedByClone() {
        addReadySaheeli(3);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();
        Permanent token = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();

        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, token.getId());

        Permanent clone = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof Clone)
                .findFirst().orElseThrow();
        assertThat(clone.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(clone.getCard().getKeywords()).doesNotContain(Keyword.HASTE);
        assertThat(clone.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
    }

    @Test
    void ultimateCanFindZeroCardsEvenWhenArtifactsAreAvailable() {
        addReadySaheeli(7);
        harness.setLibrary(player1, List.of(new MindStone()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(findPermanents(player1, "Mind Stone")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void ultimateArtifactsEnterSimultaneouslyRegardlessOfSelectionOrder() {
        addReadySaheeli(7);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new PropheticPrism(), new Panharmonicon(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    private Permanent addReadySaheeli(int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new SaheeliRai());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
