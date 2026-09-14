package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AminatouTheFateshifter.class, Forest.class, GrizzlyBears.class, Island.class})
class AminatouTheFateshifterTest extends BaseCardTest {

    @Test
    @DisplayName("+1 draws a card and puts a card from hand on top")
    void plusOneDrawsAndPutsCardOnTop() {
        addReadyAminatou(player1, 3);
        Card handCard = new GrizzlyBears();
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
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(aminatou.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears")
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
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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

    private Permanent addReadyAminatou(Player player, int loyalty) {
        Permanent permanent = new Permanent(new AminatouTheFateshifter());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private boolean controls(UUID playerId, UUID permanentId) {
        return gd.playerBattlefields.get(playerId).stream()
                .anyMatch(permanent -> permanent.getId().equals(permanentId));
    }
}
