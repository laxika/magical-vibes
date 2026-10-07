package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GunnerConscript;
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

@CardUsed({StolenStrategy.class, GunnerConscript.class, Island.class})
class StolenStrategyTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of your upkeep, exiles the top card of each opponent's library")
    void upkeepExilesEachOpponentsTopCard() {
        Permanent strategy = addStrategy();
        Card ownTop = new Island();
        Card opponentTop = new GunnerConscript();
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
        addStrategy();
        Card exiled = new GunnerConscript();
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

    @Test
    void permissionSurvivesSourceLeavingAfterResolution() {
        Permanent strategy = addStrategy();
        Card exiled = new GunnerConscript();
        harness.setLibrary(player2, List.of(exiled));

        advanceToUpkeep(player1);
        harness.withAutoStop(TurnStep.UPKEEP, this::resolveAllTriggers);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, strategy);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == exiled);
    }

    @Test
    void triggerGrantsPermissionWhenSourceLeavesBeforeResolution() {
        Permanent strategy = addStrategy();
        Card exiled = new GunnerConscript();
        harness.setLibrary(player2, List.of(exiled));

        advanceToUpkeep(player1);
        assertThat(gd.stack).isNotEmpty();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, strategy);
        harness.withAutoStop(TurnStep.UPKEEP, this::resolveAllTriggers);
        assertThat(gd.findExiledCard(exiled.getId())).isNotNull();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == exiled);
    }

    @Test
    void originalAbilityControllerRetainsPermissionAfterControlChanges() {
        Permanent strategy = addStrategy();
        Card exiled = new GunnerConscript();
        harness.setLibrary(player2, List.of(exiled));

        advanceToUpkeep(player1);
        harness.withAutoStop(TurnStep.UPKEEP, this::resolveAllTriggers);
        gd.playerBattlefields.get(player1.getId()).remove(strategy);
        gd.playerBattlefields.get(player2.getId()).add(strategy);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == exiled);
    }

    @Test
    void newControllerDoesNotGainPreviousUpkeepPermission() {
        Permanent strategy = addStrategy();
        Card exiled = new GunnerConscript();
        harness.setLibrary(player2, List.of(exiled));

        advanceToUpkeep(player1);
        harness.withAutoStop(TurnStep.UPKEEP, this::resolveAllTriggers);
        gd.playerBattlefields.get(player1.getId()).remove(strategy);
        gd.playerBattlefields.get(player2.getId()).add(strategy);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromExile(player2, exiled.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
    }

    @Test
    void noninstantSpellStillRequiresNormalTiming() {
        addStrategy();
        Card exiled = new GunnerConscript();
        harness.setLibrary(player2, List.of(exiled));

        advanceToUpkeep(player1);
        harness.withAutoStop(TurnStep.UPKEEP, this::resolveAllTriggers);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, exiled.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(exiled.getId())).isNotNull();
    }

    @Test
    void opponentUpkeepDoesNotTriggerStrategy() {
        Permanent strategy = addStrategy();
        Card ownTop = new Island();
        Card opponentTop = new GunnerConscript();
        harness.setLibrary(player1, List.of(ownTop));
        harness.setLibrary(player2, List.of(opponentTop));

        advanceToUpkeep(player2);
        harness.withAutoStop(TurnStep.UPKEEP, this::resolveAllTriggers);

        assertThat(gd.getCardsExiledByPermanent(strategy.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownTop);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop);
    }

    @Test
    void emptyOpponentLibraryDoesNotExileControllersCards() {
        Permanent strategy = addStrategy();
        Card ownTop = new Island();
        harness.setLibrary(player1, List.of(ownTop));
        harness.setLibrary(player2, List.of());

        advanceToUpkeep(player1);
        harness.withAutoStop(TurnStep.UPKEEP, this::resolveAllTriggers);

        assertThat(gd.getCardsExiledByPermanent(strategy.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownTop);
    }

    @Test
    void unusedExiledCardCannotBeCastOnFollowingTurn() {
        addStrategy();
        Card exiled = new GunnerConscript();
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setLibrary(player2, List.of(exiled, new Island(), new Island()));

        advanceToUpkeep(player1);
        harness.withAutoStop(TurnStep.UPKEEP, this::resolveAllTriggers);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromExile(player2, exiled.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
        assertThat(harness.getCastingPermissionService()
                .getCastableExiledCardIds(gd, player1.getId())).doesNotContain(exiled.getId());
        assertThat(gd.findExiledCard(exiled.getId())).isNotNull();
    }

    private Permanent addStrategy() {
        return harness.addToBattlefieldAndReturn(player1, new StolenStrategy());
    }
}
