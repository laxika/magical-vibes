package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BadMoon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
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

@CardUsed({Reap.class, BadMoon.class, GrizzlyBears.class, LlanowarElves.class, ScatheZombies.class})
class ReapTest extends BaseCardTest {

    @Test
    @DisplayName("X counts every black permanent the targeted opponent controls, not just creatures")
    void xCountsAllBlackPermanents() {
        harness.addToBattlefield(player2, new ScatheZombies());
        harness.addToBattlefield(player2, new BadMoon());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LlanowarElves(), new GrizzlyBears()));
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
        harness.addToBattlefield(player2, new ScatheZombies());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LlanowarElves()));
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
        harness.addToBattlefield(player2, new ScatheZombies());
        harness.setGraveyard(player1, List.of(new BadMoon()));
        harness.setHand(player1, List.of(new Reap()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, player2.getId());

        List<UUID> validIds = new ArrayList<>(
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Bad Moon");
    }

    @Test
    @DisplayName("With no black permanents X is zero, so nothing is returned")
    void zeroBlackPermanentsReturnsNothing() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Reap()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Reap");
    }

    @Test
    @DisplayName("Targeting yourself is rejected — the target must be an opponent")
    void cannotTargetSelf() {
        harness.addToBattlefield(player1, new ScatheZombies());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Reap()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
