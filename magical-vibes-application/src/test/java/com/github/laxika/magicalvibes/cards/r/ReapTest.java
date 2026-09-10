package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.d.DarklingStalker;
import com.github.laxika.magicalvibes.cards.d.DreadOfNight;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Reap.class, DarkRitual.class, DarklingStalker.class, DreadOfNight.class, TrainedArmodon.class})
class ReapTest extends BaseCardTest {

    @Test
    @DisplayName("X counts every black permanent the targeted opponent controls, not just creatures")
    void xCountsAllBlackPermanents() {
        harness.addToBattlefield(player1, new DarklingStalker());
        harness.addToBattlefield(player2, new DarklingStalker());
        harness.addToBattlefield(player2, new DreadOfNight());
        harness.addToBattlefield(player2, new TrainedArmodon());
        harness.setGraveyard(player1, List.of(new TrainedArmodon(), new TrainedArmodon(), new TrainedArmodon()));
        harness.setHand(player1, List.of(new Reap()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, player2.getId());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).hasSize(3);
    }

    @Test
    @DisplayName("Chosen cards go from graveyard to hand on resolution")
    void chosenCardsReturnToHand() {
        harness.addToBattlefield(player2, new DarklingStalker());
        harness.setGraveyard(player1, List.of(new TrainedArmodon(), new TrainedArmodon()));
        harness.setHand(player1, List.of(new Reap()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, player2.getId());

        List<UUID> validIds = new ArrayList<>(
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, List.of(validIds.getFirst()));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Reap");
    }

    @Test
    @DisplayName("Any card type can be returned, not only creatures")
    void anyCardTypeCanBeReturned() {
        harness.addToBattlefield(player2, new DarklingStalker());
        harness.setGraveyard(player1, List.of(new DarkRitual()));
        harness.setHand(player1, List.of(new Reap()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, player2.getId());

        List<UUID> validIds = new ArrayList<>(
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Dark Ritual");
    }

    @Test
    @DisplayName("With no black permanents X is zero, so nothing is returned")
    void zeroBlackPermanentsReturnsNothing() {
        harness.addToBattlefield(player2, new TrainedArmodon());
        harness.setGraveyard(player1, List.of(new TrainedArmodon()));
        harness.setHand(player1, List.of(new Reap()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Trained Armodon");
        harness.assertInGraveyard(player1, "Reap");
    }

    @Test
    @DisplayName("X is locked when Reap is cast")
    void xIsLockedWhenCast() {
        harness.addToBattlefield(player2, new DarklingStalker());
        harness.setGraveyard(player1, List.of(new TrainedArmodon(), new TrainedArmodon()));
        harness.setHand(player1, List.of(new Reap()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, player2.getId());

        harness.addToBattlefield(player2, new DreadOfNight());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing no cards is allowed when X is greater than zero")
    void canChooseNoCards() {
        harness.addToBattlefield(player2, new DarklingStalker());
        harness.addToBattlefield(player2, new DreadOfNight());
        harness.setGraveyard(player1, List.of(new TrainedArmodon(), new DarkRitual()));
        harness.setHand(player1, List.of(new Reap()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Trained Armodon");
        harness.assertInGraveyard(player1, "Dark Ritual");
        harness.assertInGraveyard(player1, "Reap");
    }

    @Test
    @DisplayName("Targeting yourself is rejected — the target must be an opponent")
    void cannotTargetSelf() {
        harness.addToBattlefield(player1, new DarklingStalker());
        harness.setGraveyard(player1, List.of(new TrainedArmodon()));
        harness.setHand(player1, List.of(new Reap()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
