package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.Confiscate;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GolgariThug;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MercurialTransformation;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({UbaMask.class, Forest.class, GrizzlyBears.class, GolgariThug.class,
        Confiscate.class, MercurialTransformation.class})
class UbaMaskTest extends BaseCardTest {

    @Test
    @DisplayName("A draw is replaced — the card is exiled with a play permission for the drawer")
    void drawIsReplacedByExile() {
        harness.addToBattlefield(player1, new UbaMask());
        Card top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, new Forest()));

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.passUntil(TurnStep.DRAW);

        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(top.getId());
    }

    @Test
    @DisplayName("The opponent's draw is replaced too, and the permission is theirs")
    void opponentDrawIsAlsoReplaced() {
        harness.addToBattlefield(player1, new UbaMask());
        Card top = new GrizzlyBears();
        harness.setLibrary(player2, List.of(top, new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        harness.assertNotInHand(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castFromExile(player2, top.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The exiled card can be cast that turn")
    void exiledCardCanBeCast() {
        harness.addToBattlefield(player1, new UbaMask());
        Card top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, top.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The exiled card can be played as a land that turn")
    void exiledLandCanBePlayed() {
        harness.addToBattlefield(player1, new UbaMask());
        Card top = new Forest();
        harness.setLibrary(player1, List.of(top));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, top.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
    }

    @Test
    @DisplayName("The play permission ends when Uba Mask leaves the battlefield")
    void permissionEndsWhenMaskLeavesBattlefield() {
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new UbaMask());
        Card top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, mask));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Declining dredge still lets Uba Mask replace the draw")
    void decliningDredgeStillUsesUbaMask() {
        harness.addToBattlefield(player1, new UbaMask());
        GolgariThug dredger = new GolgariThug();
        Card top = new Forest();
        harness.setGraveyard(player1, List.of(dredger));
        harness.setLibrary(player1, List.of(top, new Forest(), new Forest(), new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(top);
    }

    @Test
    @DisplayName("An empty library exiles nothing and does not lose the game")
    void emptyLibraryDoesNothing() {
        harness.addToBattlefield(player1, new UbaMask());
        harness.setLibrary(player1, List.of());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.winnerPlayerId).isNull();
    }

    @Test
    void losingAbilitiesStopsReplacingDraws() {
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new UbaMask());
        transformMask(mask);
        Card top = new Forest();
        harness.setLibrary(player1, List.of(top));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(top);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
    }

    @Test
    void losingAbilitiesSuspendsPermissionForPreviouslyExiledCards() {
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new UbaMask());
        Card top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        transformMask(mask);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
    }

    @Test
    void changingMaskControllerPreservesDrawersPermission() {
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new UbaMask());
        Card top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Confiscate()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player2, 0, mask.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Uba Mask");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, top.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void unplayedCardsRemainExiledButCannotBePlayedNextTurn() {
        harness.addToBattlefield(player1, new UbaMask());
        Card top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
    }

    @Test
    void permissionDoesNotWaiveManaCostOrCreatureTiming() {
        harness.addToBattlefield(player1, new UbaMask());
        Card top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
    }

    @Test
    void permissionDoesNotAllowASecondLandInOneTurn() {
        harness.addToBattlefield(player1, new UbaMask());
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 2));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, first.getId());

        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(second);
    }

    private void transformMask(Permanent mask) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MercurialTransformation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, mask.getId());
        harness.handleListChoice(player1, "Become a blue Frog creature with base power and toughness 1/1");
    }
}
