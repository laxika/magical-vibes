package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EclipsedMerrow;
import com.github.laxika.magicalvibes.cards.c.ChitteringHost;
import com.github.laxika.magicalvibes.cards.g.GrafRats;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MidnightScavengers;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TemporalCleansing.class, EclipsedMerrow.class, Island.class,
        ChitteringHost.class, GrafRats.class, MidnightScavengers.class})
class TemporalCleansingTest extends BaseCardTest {

    @Test
    @DisplayName("The target's owner can keep it second from the top")
    void targetOwnerChoosesSecondFromTop() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EclipsedMerrow());
        Card topCard = new Island();
        Card bottomCard = new Island();
        harness.setLibrary(player2, List.of(topCard, bottomCard));

        castTemporalCleansing(target);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.TargetLibraryDestinationChoice.class)
                .options()).containsExactly("Second from the top", "Bottom");

        harness.handleListChoice(player2, "Second from the top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, target.getCard(), bottomCard);
        harness.assertNotOnBattlefield(player2, "Eclipsed Merrow");
        harness.assertInGraveyard(player1, "Temporal Cleansing");
    }

    @Test
    @DisplayName("The target's owner can put it on the bottom")
    void targetOwnerChoosesBottom() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EclipsedMerrow());
        Card topCard = new Island();
        Card bottomCard = new Island();
        harness.setLibrary(player2, List.of(topCard, bottomCard));

        castTemporalCleansing(target);
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, bottomCard, target.getCard());
        harness.assertNotOnBattlefield(player2, "Eclipsed Merrow");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new TemporalCleansing()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second from the top puts the target into an empty library")
    void secondFromTopInEmptyLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EclipsedMerrow());
        harness.setLibrary(player2, List.of());

        castTemporalCleansing(target);
        harness.handleListChoice(player2, "Second from the top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard());
        harness.assertNotOnBattlefield(player2, "Eclipsed Merrow");
    }

    @Test
    @DisplayName("The owner chooses the destination even when another player controls the target")
    void ownerChoosesForOpponentsPermanent() {
        Card card = new EclipsedMerrow();
        card.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, card);
        Card topCard = new Island();
        Card bottomCard = new Island();
        harness.setLibrary(player1, List.of(topCard, bottomCard));
        harness.setLibrary(player2, List.of());

        castTemporalCleansing(target);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.TargetLibraryDestinationChoice.class)
                .playerId()).isEqualTo(player1.getId());
        assertThatThrownBy(() -> harness.handleListChoice(player2, "Bottom"))
                .isInstanceOf(IllegalStateException.class);
        harness.handleListChoice(player1, "Bottom");

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, bottomCard, card);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertNotOnBattlefield(player2, "Eclipsed Merrow");
    }

    @Test
    @DisplayName("Convoke can pay the entire cost using creatures with summoning sickness")
    void convokePaysEntireCost() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new EclipsedMerrow());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new EclipsedMerrow());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new EclipsedMerrow());
        Permanent fourth = harness.addToBattlefieldAndReturn(player1, new EclipsedMerrow());
        first.setSummoningSick(true);
        second.setSummoningSick(true);
        third.setSummoningSick(true);
        fourth.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EclipsedMerrow());
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new TemporalCleansing()));

        harness.castInstantWithConvoke(player1, 0, List.of(target.getId()),
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId()));

        assertThat(List.of(first, second, third, fourth)).allMatch(Permanent::isTapped);
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard());
        harness.assertInGraveyard(player1, "Temporal Cleansing");
    }

    @Test
    @DisplayName("A tapped creature cannot help pay with convoke")
    void tappedCreatureCannotConvoke() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EclipsedMerrow());
        creature.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EclipsedMerrow());
        harness.setHand(player1, List.of(new TemporalCleansing()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0,
                List.of(target.getId()), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Eclipsed Merrow");
    }

    @Test
    @DisplayName("The owner can put both components of a melded target on the bottom")
    void ownerCanChooseBottomForMeldedPermanent() {
        Card rats = new GrafRats();
        Card scavengers = new MidnightScavengers();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChitteringHost());
        target.getMeldComponentCards().addAll(List.of(rats, scavengers));
        Card topCard = new Island();
        Card bottomCard = new Island();
        harness.setLibrary(player2, List.of(topCard, bottomCard));

        castTemporalCleansing(target);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        harness.handleListChoice(player2, "Bottom");

        List<Card> library = gd.playerDecks.get(player2.getId());
        assertThat(library).hasSize(4);
        assertThat(library.subList(0, 2)).containsExactly(topCard, bottomCard);
        assertThat(library.subList(2, 4)).containsExactlyInAnyOrder(rats, scavengers);
        harness.assertNotOnBattlefield(player2, "Chittering Host");
    }

    private void castTemporalCleansing(Permanent target) {
        harness.setHand(player1, List.of(new TemporalCleansing()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }

}
