package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.m.MysticRemora;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Enervate.class, BalduvianBears.class, Forest.class, IcyManipulator.class, MysticRemora.class})
class EnervateTest extends BaseCardTest {

    @Test
    @DisplayName("Taps target creature and schedules a draw at the next upkeep")
    void tapsCreatureAndSchedulesDraw() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        harness.setHand(player1, List.of(new Enervate()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(bears.isTapped()).isTrue();

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can tap a target land")
    void tapsLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Enervate()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, forest.getId());

        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can tap a noncreature artifact")
    void tapsArtifact() {
        Permanent manipulator = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());
        harness.setHand(player1, List.of(new Enervate()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, manipulator.getId());

        assertThat(manipulator.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Draws a card at the beginning of the next turn's upkeep")
    void drawsCardAtNextUpkeep() {
        BalduvianBears drawnCard = new BalduvianBears();
        harness.setLibrary(player1, List.of(drawnCard));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        harness.setHand(player1, List.of(new Enervate()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        Permanent remora = harness.addToBattlefieldAndReturn(player2, new MysticRemora());
        harness.setHand(player1, List.of(new Enervate()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, remora.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact, creature, or land");
    }

    @Test
    @DisplayName("An already tapped target still allows the delayed draw")
    void tappedTargetStillDraws() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        bears.tap();
        BalduvianBears drawnCard = new BalduvianBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new Enervate()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        advanceToUpkeep(player2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("No delayed draw is created when the sole target leaves before resolution")
    void removedTargetPreventsDelayedDraw() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        BalduvianBears drawnCard = new BalduvianBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new Enervate()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, bears.getId());
        gd.playerBattlefields.get(player2.getId()).remove(bears);
        harness.setGraveyard(player2, List.of(bears.getCard()));
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }
}
