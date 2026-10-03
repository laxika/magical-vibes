package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SecureTheWastes;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AminatouTheFateshifter.class, Forest.class, LlanowarElves.class, Island.class, SecureTheWastes.class})
class AminatouTheFateshifterTest extends BaseCardTest {

    @Test
    @DisplayName("+1 draws a card and puts a card from hand on top")
    void plusOneDrawsAndPutsCardOnTop() {
        addReadyAminatou(player1, 3);
        Card handCard = new LlanowarElves();
        Card libraryCard = new Forest();
        harness.setHand(player1, List.of(handCard));
        harness.setLibrary(player1, List.of(libraryCard));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(handCard.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(handCard);
    }

    @Test
    @DisplayName("-1 flickers another permanent you own under your control")
    void minusOneFlickersAnotherOwnedPermanent() {
        Permanent aminatou = addReadyAminatou(player1, 3);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(aminatou.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Llanowar Elves")
                        && !permanent.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("-1 cannot target Aminatou itself")
    void minusOneCannotTargetSource() {
        Permanent aminatou = addReadyAminatou(player1, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, aminatou.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-6 permanently rotates nonland permanents and leaves lands and Aminatou alone")
    void minusSixRotatesNonlandPermanents() {
        Permanent aminatou = addReadyAminatou(player1, 7);
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "Right");
        harness.passBothPriorities();

        assertThat(aminatou.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(controls(player1.getId(), opponentBears.getId())).isTrue();
        assertThat(controls(player2.getId(), ownBears.getId())).isTrue();
        assertThat(controls(player1.getId(), island.getId())).isTrue();
        assertThat(controls(player1.getId(), aminatou.getId())).isTrue();
    }

    @Test
    void minusOneDoesNotReturnExiledToken() {
        addReadyAminatou(player1, 3);
        harness.setHand(player1, List.of(new SecureTheWastes()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();

        harness.activateAbility(player1, 0, 1, null, token.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void minusOneReturnsOwnedPermanentControlledByOpponent() {
        addReadyAminatou(player1, 3);
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        elves.getCard().setOwnerId(player1.getId());
        gd.stolenCreatures.put(elves.getId(), player1.getId());

        harness.activateAbility(player1, 0, 1, null, elves.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        assertThat(controls(player1.getId(), elves.getId())).isFalse();
    }

    @Test
    void minusOneCannotTargetOpponentOwnedPermanentUnderYourControl() {
        addReadyAminatou(player1, 3);
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        elves.getCard().setOwnerId(player2.getId());
        gd.stolenCreatures.put(elves.getId(), player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, elves.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void plusOneCanPutTheJustDrawnCardBack() {
        addReadyAminatou(player1, 3);
        Card drawn = new Forest();
        Card next = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn, next));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(drawn.getId()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn, next);
    }

    @Test
    void minusOneLeavesManifestedInstantInExile() {
        addReadyAminatou(player1, 3);
        Card instant = new SecureTheWastes();
        Permanent manifested = harness.addToBattlefieldAndReturn(player1, instant);
        manifested.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        harness.activateAbility(player1, 0, 1, null, manifested.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Secure the Wastes");
        harness.assertNotOnBattlefield(player2, "Secure the Wastes");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(instant);
    }

    @Test
    void minusSixLeftResolvesAfterSourceDiesPayingLoyalty() {
        addReadyAminatou(player1, 6);
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Left");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Aminatou, the Fateshifter");
        assertThat(controls(player1.getId(), elves.getId())).isTrue();
    }

    private Permanent addReadyAminatou(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new AminatouTheFateshifter());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private boolean controls(UUID playerId, UUID permanentId) {
        return gd.playerBattlefields.get(playerId).stream()
                .anyMatch(permanent -> permanent.getId().equals(permanentId));
    }
}
