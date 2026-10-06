package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShamblingShell.class, Forest.class, Watchwolf.class})
class ShamblingShellTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Shambling Shell puts a +1/+1 counter on target creature")
    void sacrificeAbilityCountersTargetCreature() {
        harness.addToBattlefield(player1, new ShamblingShell());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Watchwolf());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Shambling Shell");
        harness.assertInGraveyard(player1, "Shambling Shell");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new ShamblingShell());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Shambling Shell");
    }

    @Test
    @DisplayName("May dredge Shambling Shell instead of drawing")
    void dredgesInsteadOfDrawing() {
        ShamblingShell shell = new ShamblingShell();
        List<Card> milled = List.of(new Forest(), new Watchwolf(), new Forest());
        harness.setGraveyard(player1, List.of(shell));
        harness.setLibrary(player1, milled);

        resolveDraw();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(shell);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(milled);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Can decline dredge and draw normally")
    void declinesDredge() {
        ShamblingShell shell = new ShamblingShell();
        Card topCard = new Forest();
        Card secondCard = new Watchwolf();
        Card thirdCard = new Forest();
        Card fourthCard = new Forest();
        harness.setGraveyard(player1, List.of(shell));
        harness.setLibrary(player1, List.of(topCard, secondCard, thirdCard, fourthCard));

        resolveDraw();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(shell);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard, thirdCard, fourthCard);
        assertThat(gd.cardsDrawnThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not offer dredge when the library has fewer than three cards")
    void cannotDredgeWithTooFewLibraryCards() {
        ShamblingShell shell = new ShamblingShell();
        Card topCard = new Forest();
        Card remainingCard = new Watchwolf();
        harness.setGraveyard(player1, List.of(shell));
        harness.setLibrary(player1, List.of(topCard, remainingCard));

        resolveDraw();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(shell);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.cardsDrawnThisTurn.get(player1.getId())).isEqualTo(1);
    }

    private void resolveDraw() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }

    @Test
    @DisplayName("Sacrifice is paid before the counter ability resolves, even with summoning sickness")
    void sacrificeIsPaidImmediately() {
        Permanent shell = harness.addToBattlefieldAndReturn(player1, new ShamblingShell());
        shell.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Watchwolf());

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Shambling Shell");
        harness.assertInGraveyard(player1, "Shambling Shell");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Shambling Shell can target itself but is sacrificed before resolution")
    void canTargetItself() {
        ShamblingShell card = new ShamblingShell();
        Permanent shell = harness.addToBattlefieldAndReturn(player1, card);

        harness.activateAbility(player1, 0, null, shell.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Shambling Shell");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
        assertThat(shell.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Dredge returns only the selected Shell and mills exactly three cards")
    void dredgeDoesNotReturnAnotherShellMilledWithIt() {
        ShamblingShell selected = new ShamblingShell();
        ShamblingShell milledShell = new ShamblingShell();
        List<Card> milled = List.of(new Forest(), milledShell, new Watchwolf());
        Card remaining = new Forest();
        harness.setGraveyard(player1, List.of(selected));
        harness.setLibrary(player1, List.of(milled.get(0), milled.get(1), milled.get(2), remaining));

        resolveDraw();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(selected).doesNotContain(milledShell, remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(milled);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }
}
