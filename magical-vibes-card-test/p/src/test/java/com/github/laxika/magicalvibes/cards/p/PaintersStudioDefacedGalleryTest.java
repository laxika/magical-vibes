package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BashfulBeastie;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PaintersStudioDefacedGallery.class, BashfulBeastie.class, Forest.class})
class PaintersStudioDefacedGalleryTest extends BaseCardTest {

    @Test
    void painterStudioExilesTopTwoCardsAndAllowsPlayingThem() {
        Card first = new BashfulBeastie();
        Card second = new BashfulBeastie();
        harness.setLibrary(player1, List.of(first, second));

        castRoom(0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
    }

    @Test
    void defacedGalleryBoostsOnlyAttackingCreaturesYouControl() {
        Permanent room = harness.addToBattlefieldAndReturn(player1, new PaintersStudioDefacedGallery());
        room.unlockRoomDoor(1);
        Permanent attacker = addCreatureReady(player1, new BashfulBeastie());
        Permanent nonAttacker = addCreatureReady(player1, new BashfulBeastie());
        Permanent opponent = addCreatureReady(player2, new BashfulBeastie());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, nonAttacker)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(5);
    }

    @Test
    void defacedGalleryBoostWearsOffAtEndOfTurn() {
        Permanent room = harness.addToBattlefieldAndReturn(player1, new PaintersStudioDefacedGallery());
        room.unlockRoomDoor(1);
        Permanent attacker = addCreatureReady(player1, new BashfulBeastie());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(5);
    }

    @Test
    void paintersStudioAllowsCastingExiledCreatureAndPlayingExiledLand() {
        Card creature = new BashfulBeastie();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(creature, land));
        castRoom(0);

        harness.castFromExile(player1, land.getId());
        harness.assertOnBattlefield(player1, "Forest");
        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bashful Beastie");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(creature, land);
    }

    @Test
    void paintersStudioPermissionLastsThroughNextTurnAndThenExpires() {
        Card creature = new BashfulBeastie();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(creature, land, new Forest(), new Forest(), new Forest()));
        castRoom(0);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(creature.getId(), player1.getId())
                .containsEntry(land.getId(), player1.getId());
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Bashful Beastie");

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(land.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(land);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void paintersStudioExilesOnlyAvailableCardsFromShortLibrary() {
        Card onlyCard = new BashfulBeastie();
        harness.setLibrary(player1, List.of(onlyCard));
        castRoom(0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void castingDefacedGalleryDoesNotExileCardsUntilPaintersStudioIsUnlocked() {
        Card first = new BashfulBeastie();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        castRoom(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.unlockRoomDoor(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
    }

    @Test
    void lockedDefacedGalleryDoesNotBoostAttackersAfterCastingPaintersStudio() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        castRoom(0);
        Permanent attacker = addCreatureReady(player1, new BashfulBeastie());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(5);
    }

    @Test
    void roomEnteringWithBothDoorsLockedDoesNotBoostAttackers() {
        harness.addToBattlefield(player1, new PaintersStudioDefacedGallery());
        Permanent attacker = addCreatureReady(player1, new BashfulBeastie());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(5);
    }

    @Test
    void defacedGalleryBoostsEachAttackerOnlyOnceWhenSeveralCreaturesAttack() {
        castRoom(1);
        Permanent first = addCreatureReady(player1, new BashfulBeastie());
        Permanent second = addCreatureReady(player1, new BashfulBeastie());

        declareAttackers(player1, List.of(1, 2));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    void defacedGalleryDoesNotTriggerWhenOpponentAttacks() {
        castRoom(1);
        Permanent attacker = addCreatureReady(player2, new BashfulBeastie());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(5);
    }

    private Permanent castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new PaintersStudioDefacedGallery()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, doorIndex == 0 ? 2 : 1);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}
