package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StolenStrategy.class, GrizzlyBears.class, Island.class})
class StolenStrategyTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of your upkeep, exiles the top card of each opponent's library")
    void upkeepExilesEachOpponentsTopCard() {
        Permanent strategy = addStrategy();
        Card ownTop = new Island();
        Card opponentTop = new GrizzlyBears();
        harness.setLibrary(player1, List.of(ownTop));
        harness.setLibrary(player2, List.of(opponentTop));

        advanceToUpkeep(player1);
        harness.withAutoStop(TurnStep.UPKEEP, this::resolveAllTriggers);

        assertThat(gd.getCardsExiledByPermanent(strategy.getId())).containsExactly(opponentTop);
        assertThat(gd.findExiledCard(opponentTop.getId())).extracting(ExiledCardEntry::faceDown)
                .isEqualTo(false);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownTop);
    }

    @Test
    @DisplayName("The controller may cast an exiled nonland spell using mana of any color that turn")
    void castsExiledSpellWithAnyManaThatTurn() {
        Permanent strategy = addStrategy();
        Card exiled = new GrizzlyBears();
        harness.setLibrary(player2, List.of(exiled));

        advanceToUpkeep(player1);
        harness.withAutoStop(TurnStep.UPKEEP, this::resolveAllTriggers);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == exiled);
    }

    @Test
    @DisplayName("The upkeep permission does not allow playing an exiled land")
    void doesNotAllowPlayingExiledLand() {
        addStrategy();
        Card exiledLand = new Island();
        harness.setLibrary(player2, List.of(exiledLand));

        advanceToUpkeep(player1);
        harness.withAutoStop(TurnStep.UPKEEP, this::resolveAllTriggers);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, exiledLand.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
    }

    private Permanent addStrategy() {
        return harness.addToBattlefieldAndReturn(player1, new StolenStrategy());
    }
}
