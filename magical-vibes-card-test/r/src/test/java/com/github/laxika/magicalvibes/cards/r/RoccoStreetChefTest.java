package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoccoStreetChef.class, Forest.class, GrizzlyBears.class})
class RoccoStreetChefTest extends BaseCardTest {

    @Test
    void exilesTheTopCardOfEachPlayersLibraryAndGrantsPlayPermission() {
        harness.addToBattlefield(player1, new RoccoStreetChef());
        Forest playerOneCard = new Forest();
        GrizzlyBears playerTwoCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(playerOneCard));
        harness.setLibrary(player2, List.of(playerTwoCard));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(playerOneCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(playerTwoCard);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(playerOneCard.getId(), player1.getId())
                .containsEntry(playerTwoCard.getId(), player2.getId());
    }

    @Test
    void createsACounterAndFoodWhenAnOpponentPlaysALandFromExile() {
        Permanent rocco = addCreatureReady(player1, new RoccoStreetChef());
        Forest land = new Forest();
        gd.addToExile(player2.getId(), land);
        gd.exilePlayPermissions.put(land.getId(), player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.playCardFromExile(gd, player2, land.getId(), null, null);
        harness.handlePermanentChosen(player1, rocco.getId());
        resolveAllTriggers();

        assertThat(rocco.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Food")).isNotNull();
    }

    @Test
    void createsACounterAndFoodWhenAnOpponentCastsASpellFromExile() {
        Permanent rocco = addCreatureReady(player1, new RoccoStreetChef());
        GrizzlyBears spell = new GrizzlyBears();
        gd.addToExile(player2.getId(), spell);
        gd.exilePlayPermissions.put(spell.getId(), player2.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.playCardFromExile(gd, player2, spell.getId(), null, null);
        harness.handlePermanentChosen(player1, rocco.getId());
        resolveAllTriggers();

        assertThat(rocco.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Food")).isNotNull();
    }

    @Test
    void permissionExpiresAsTheControllersNextEndStepBegins() {
        harness.addToBattlefield(player1, new RoccoStreetChef());
        Forest card = new Forest();
        harness.setLibrary(player1, List.of(card, new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.exilePlayPermissions).containsEntry(card.getId(), player1.getId());
        gd.turnNumber += 2;
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        resolveAllTriggers();
    }

    @Test
    void controllerPlayingAnExiledLandCanRewardAnOpponentsCreatureAndUseTheFood() {
        Permanent rocco = addCreatureReady(player1, new RoccoStreetChef());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Forest land = new Forest();
        harness.setExile(player1, List.of(land));
        gd.exilePlayPermissions.put(land.getId(), player1.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromExile(player1, land.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(rocco.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        assertThat(countPermanents(player2, "Food")).isZero();
        Permanent food = findPermanent(player1, "Food");
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(food), null, null);
        assertThat(countPermanents(player1, "Food")).isZero();
        resolveAllTriggers();
        harness.assertLife(player1, 13);
    }

    @Test
    void controllerCastingFromExileRewardsBeforeTheSpellResolves() {
        Permanent rocco = addCreatureReady(player1, new RoccoStreetChef());
        GrizzlyBears spell = new GrizzlyBears();
        harness.setExile(player1, List.of(spell));
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFromExile(player1, spell.getId());
        harness.handlePermanentChosen(player1, rocco.getId());
        harness.passBothPriorities();

        assertThat(rocco.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    void illegalCreatureTargetPreventsFoodCreation() {
        Permanent rocco = addCreatureReady(player1, new RoccoStreetChef());
        Forest land = new Forest();
        harness.setExile(player2, List.of(land));
        gd.exilePlayPermissions.put(land.getId(), player2.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player2, land.getId());
        harness.handlePermanentChosen(player1, rocco.getId());
        gd.playerBattlefields.get(player1.getId()).remove(rocco);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    void playingAndCastingFromHandDoNotTriggerRewards() {
        Permanent rocco = addCreatureReady(player1, new RoccoStreetChef());
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(rocco.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    void exiledCardRemainsPlayableAfterRoccoLeavesTheBattlefield() {
        Permanent rocco = addCreatureReady(player1, new RoccoStreetChef());
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.setLibrary(player2, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        gd.playerBattlefields.get(player1.getId()).remove(rocco);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromExile(player1, land.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(land);
        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }
}
