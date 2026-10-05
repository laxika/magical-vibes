package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IreShaman.class, Mountain.class})
class IreShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Turning Ire Shaman face up adds a counter and exiles the top card with play permission")
    void turningFaceUpAddsCounterAndExilesTopCardWithPlayPermission() {
        Card topCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard));
        Permanent shaman = castFaceDown();

        turnFaceUp(shaman);
        assertThat(shaman.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(topCard.getId()));
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(topCard.getId());
    }

    @Test
    @DisplayName("Ire Shaman's play permission expires at end of turn")
    void playPermissionExpiresAtEndOfTurn() {
        Card topCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard));
        Permanent shaman = castFaceDown();

        turnFaceUp(shaman);
        harness.passBothPriorities();
        assertThat(gd.exilePlayPermissions).containsKey(topCard.getId());

        harness.inMutationScope(
                () -> GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(topCard.getId());
    }

    @Test
    void turningFaceUpWithoutPayingMegamorphTriggersExileButAddsNoCounter() {
        Card topCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard));
        Permanent shaman = castFaceDown();

        harness.inMutationScope(() -> gs.turnPermanentFaceUpWithoutPayingManaCost(gd, shaman));
        resolveAllTriggers();

        assertThat(shaman.isFaceDown()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(shaman.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void emptyLibraryDoesNotPreventMegamorphCounter() {
        harness.setLibrary(player1, List.of());
        Permanent shaman = castFaceDown();

        turnFaceUp(shaman);
        resolveAllTriggers();

        assertThat(shaman.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    @Test
    void exiledCreatureRequiresItsNormalManaCostAndCanBeCastThisTurn() {
        Card topCard = new IreShaman();
        harness.setLibrary(player1, List.of(topCard));
        Permanent shaman = castFaceDown();
        turnFaceUp(shaman);
        resolveAllTriggers();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, topCard.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Ire Shaman")).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void exiledLandCanBePlayedButStillUsesTheLandPlayForTheTurn() {
        Card topCard = new Mountain();
        Card secondLand = new Mountain();
        harness.setLibrary(player1, List.of(topCard));
        Permanent shaman = castFaceDown();
        turnFaceUp(shaman);
        resolveAllTriggers();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromExile(player1, topCard.getId());

        harness.assertOnBattlefield(player1, "Mountain");
        harness.setHand(player1, List.of(secondLand));
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void castingFaceUpDoesNotExileOrAddCounter() {
        Card topCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new IreShaman()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Ire Shaman").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void menaceRejectsOneBlockerButAllowsTwo() {
        Permanent attacker = addCreatureReady(player1, new IreShaman());
        addCreatureReady(player2, new IreShaman());
        addCreatureReady(player2, new IreShaman());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new IreShaman()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        return findPermanent(player1, "Ire Shaman");
    }

    private void turnFaceUp(Permanent shaman) {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(shaman));
    }
}
