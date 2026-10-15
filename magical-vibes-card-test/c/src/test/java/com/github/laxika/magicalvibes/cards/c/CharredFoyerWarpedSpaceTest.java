package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.g.Glimmerburst;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.laxika.magicalvibes.model.ManaColor.RED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CharredFoyerWarpedSpace.class, Opt.class, Glimmerburst.class, Mountain.class})
class CharredFoyerWarpedSpaceTest extends BaseCardTest {

    @Test
    void charredFoyerExilesTheTopCardAtTheBeginningOfUpkeep() {
        castRoom(0);
        Opt topCard = new Opt();
        harness.setLibrary(player1, List.of(topCard));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }

    @Test
    void warpedSpaceLetsTheControllerCastOneExiledSpellForFreeEachTurn() {
        castRoom(1);
        Opt firstSpell = new Opt();
        Opt secondSpell = new Opt();
        harness.setExile(player1, List.of(firstSpell, secondSpell));
        gd.exilePlayPermissions.put(firstSpell.getId(), player1.getId());
        gd.exilePlayPermissions.put(secondSpell.getId(), player1.getId());

        harness.castFromExile(player1, firstSpell.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThatThrownBy(() -> harness.castFromExile(player1, secondSpell.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void warpedSpaceDoesNotApplyWhileItsDoorIsLocked() {
        castRoom(0);
        Opt spell = new Opt();
        harness.setExile(player1, List.of(spell));
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void charredFoyerTriggerStillResolvesAfterItsDoorIsLocked() {
        castRoom(0);
        Glimmerburst topCard = new Glimmerburst();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).getFirst().lockRoomDoor(0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }

    @Test
    void lockedCharredFoyerDoesNotTriggerDuringUpkeep() {
        castRoom(1);
        Glimmerburst topCard = new Glimmerburst();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void charredFoyerPermissionRequiresPayingManaAndExpiresAtEndOfTurn() {
        castRoom(0);
        Glimmerburst topCard = new Glimmerburst();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
    }

    @Test
    void warpedSpaceDoesNotGrantPermissionToCastArbitraryExiledCards() {
        castRoom(1);
        Glimmerburst spell = new Glimmerburst();
        harness.setExile(player1, List.of(spell));

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void warpedSpaceDoesNotMakeSpellsFromHandFree() {
        castRoom(1);
        harness.setHand(player1, List.of(new Glimmerburst()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void warpedSpaceUseResetsForTheOpponentsTurn() {
        castRoom(1);
        Glimmerburst firstSpell = new Glimmerburst();
        Glimmerburst secondSpell = new Glimmerburst();
        harness.setExile(player1, List.of(firstSpell, secondSpell));
        gd.exilePlayPermissions.put(firstSpell.getId(), player1.getId());
        gd.exilePlayPermissions.put(secondSpell.getId(), player1.getId());
        harness.setLibrary(player1, List.of(new Glimmerburst(), new Glimmerburst(), new Glimmerburst()));
        harness.castFromExile(player1, firstSpell.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.castFromExile(player1, secondSpell.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(secondSpell);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void unlockingWarpedSpaceEnablesItsAlternativeCost() {
        castRoom(0);
        harness.addMana(player1, RED, 6);
        harness.unlockRoomDoor(player1, 0, 1);
        Glimmerburst spell = new Glimmerburst();
        harness.setExile(player1, List.of(spell));
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());

        harness.castFromExile(player1, spell.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(spell);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void charredFoyerAllowsPlayingAnExiledLandOnlyDuringTheMainPhase() {
        castRoom(0);
        Mountain land = new Mountain();
        harness.setLibrary(player1, List.of(land, new Glimmerburst()));
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);
        harness.withAutoStop(TurnStep.UPKEEP, harness::passBothPriorities);

        assertThatThrownBy(() -> harness.castFromExile(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, land.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(land);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(land.getId()));
    }

    @Test
    void eachWarpedSpaceProvidesItsOwnUsePerTurn() {
        castRoom(1);
        castRoom(1);
        Glimmerburst firstSpell = new Glimmerburst();
        Glimmerburst secondSpell = new Glimmerburst();
        Glimmerburst thirdSpell = new Glimmerburst();
        harness.setExile(player1, List.of(firstSpell, secondSpell, thirdSpell));
        gd.exilePlayPermissions.put(firstSpell.getId(), player1.getId());
        gd.exilePlayPermissions.put(secondSpell.getId(), player1.getId());
        gd.exilePlayPermissions.put(thirdSpell.getId(), player1.getId());

        harness.castFromExile(player1, firstSpell.getId());
        harness.castFromExile(player1, secondSpell.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThatThrownBy(() -> harness.castFromExile(player1, thirdSpell.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void charredFoyerDoesNothingWithAnEmptyLibrary() {
        castRoom(0);
        harness.setLibrary(player1, List.of());
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    private void castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new CharredFoyerWarpedSpace()));
        harness.addMana(player1, RED, doorIndex == 0 ? 4 : 6);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
    }
}
