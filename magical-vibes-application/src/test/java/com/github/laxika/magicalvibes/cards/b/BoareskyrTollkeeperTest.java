package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoareskyrTollkeeper.class, Forest.class, GrizzlyBears.class, Unsummon.class})
class BoareskyrTollkeeperTest extends BaseCardTest {

    @Test
    void selectedCreaturePerpetuallyEntersTapped() {
        Card bears = new GrizzlyBears();
        harness.setHand(player1, List.of(new BoareskyrTollkeeper()));
        harness.setHand(player2, List.of(new Forest(), bears));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.RevealedMatchingHandCardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player2, 1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(bears.getId()) && permanent.isTapped());
    }

    @Test
    void selectedLandEntersTappedButUnselectedCreatureDoesNot() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        harness.setHand(player2, List.of(forest, bears));
        resolveTollkeeperTrigger();

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(forest, bears);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player2, 0);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(forest.getId()) && permanent.isTapped());

        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(bears.getId()) && !permanent.isTapped());
    }

    @Test
    void controllerMustChooseExactlyOneRevealedCreatureOrLand() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card instant = new Unsummon();
        harness.setHand(player2, List.of(forest, instant, bears));
        resolveTollkeeperTrigger();

        var choice = (PendingInteraction.RevealedMatchingHandCardChoice) gd.interaction.activeInteraction();
        assertThat(choice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.cards()).containsExactly(forest, bears);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(instant.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(forest.getId(), bears.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(forest, instant, bears);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void handWithoutCreaturesOrLandsDoesNotRequireChoice() {
        Card instant = new Unsummon();
        harness.setHand(player2, List.of(instant));
        resolveTollkeeperTrigger();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(instant);
    }

    @Test
    void emptyHandDoesNotRequireChoice() {
        harness.setHand(player2, List.of());
        resolveTollkeeperTrigger();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void selectedCreatureStillEntersTappedAfterReturningToHandAndSourceLeaving() {
        Card bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        resolveTollkeeperTrigger();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(bears.getId()) && permanent.isTapped());

        harness.setHand(player1, List.of(new Unsummon(), new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Boareskyr Tollkeeper"));
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(bears);

        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(bears.getId()) && permanent.isTapped());
    }

    private void resolveTollkeeperTrigger() {
        harness.setHand(player1, List.of(new BoareskyrTollkeeper()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
