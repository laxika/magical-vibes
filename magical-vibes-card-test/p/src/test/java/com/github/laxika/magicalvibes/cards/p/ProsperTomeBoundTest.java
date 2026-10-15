package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProsperTomeBound.class, Forest.class, GrizzlyBears.class})
class ProsperTomeBoundTest extends BaseCardTest {

    @Test
    void exilesTopCardAtEndStepWithPermissionUntilEndOfNextTurn() {
        addProsper();
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        goToEndStepAndResolve();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd).containsKey(topCard.getId());
    }

    @Test
    void castingExiledSpellCreatesTreasure() {
        addProsper();
        GrizzlyBears spell = new GrizzlyBears();
        gd.addToExile(player1.getId(), spell);
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareMainPhase(player1);

        harness.castFromExile(player1, spell.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Treasure")).isNotNull();
    }

    @Test
    void playingExiledLandCreatesTreasure() {
        addProsper();
        Forest land = new Forest();
        gd.addToExile(player1.getId(), land);
        gd.exilePlayPermissions.put(land.getId(), player1.getId());
        prepareMainPhase(player1);

        harness.castFromExile(player1, land.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Treasure")).isNotNull();
    }

    @Test
    void castingFromHandDoesNotCreateTreasure() {
        addProsper();
        GrizzlyBears spell = new GrizzlyBears();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareMainPhase(player1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Treasure"));
    }

    @Test
    void opponentEndStepDoesNotExileCards() {
        addProsper();
        Forest ownTop = new Forest();
        Forest opponentTop = new Forest();
        harness.setLibrary(player1, List.of(ownTop));
        harness.setLibrary(player2, List.of(opponentTop));
        prepareMainPhase(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownTop);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop);
    }

    @Test
    void emptyLibraryDoesNotCreateTreasure() {
        addProsper();
        harness.setLibrary(player1, List.of());

        goToEndStepAndResolve();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    void playingLandFromHandDoesNotCreateTreasure() {
        addProsper();
        harness.setHand(player1, List.of(new Forest()));
        prepareMainPhase(player1);

        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    void opponentPlayingLandFromExileDoesNotCreateTreasure() {
        addProsper();
        Forest land = new Forest();
        harness.setExile(player2, List.of(land));
        gd.exilePlayPermissions.put(land.getId(), player2.getId());
        prepareMainPhase(player2);

        harness.castFromExile(player2, land.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Forest");
        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(countPermanents(player2, "Treasure")).isZero();
    }

    @Test
    void endStepPermissionAllowsLandPlayAfterProsperLeavesBattlefield() {
        Permanent prosper = addProsper();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land, new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        goToEndStepAndResolve();
        gd.playerBattlefields.get(player1.getId()).remove(prosper);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);

        harness.castFromExile(player1, land.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(land);
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    void permissionExpiresAtEndOfControllersNextTurnAndCardRemainsExiled() {
        addProsper();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land, new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));

        goToEndStepAndResolve();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(land.getId(), player1.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(land.getId(), player1.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(gd.exilePlayPermissions).containsEntry(land.getId(), player1.getId());

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(land.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(land);
    }

    @Test
    void opponentCastingFromExileDoesNotCreateTreasure() {
        addProsper();
        GrizzlyBears spell = new GrizzlyBears();
        harness.setExile(player2, List.of(spell));
        gd.exilePlayPermissions.put(spell.getId(), player2.getId());
        harness.addMana(player2, ManaColor.GREEN, 2);
        prepareMainPhase(player2);

        harness.castFromExile(player2, spell.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(countPermanents(player2, "Treasure")).isZero();
    }

    @Test
    void treasureTriggerResolvesAfterProsperLeavesBattlefield() {
        Permanent prosper = addProsper();
        Forest land = new Forest();
        harness.setExile(player1, List.of(land));
        gd.exilePlayPermissions.put(land.getId(), player1.getId());
        prepareMainPhase(player1);

        harness.castFromExile(player1, land.getId());
        gd.playerBattlefields.get(player1.getId()).remove(prosper);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }
    private Permanent addProsper() {
        return addCreatureReady(player1, new ProsperTomeBound());
    }

    private void goToEndStepAndResolve() {
        prepareMainPhase(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
