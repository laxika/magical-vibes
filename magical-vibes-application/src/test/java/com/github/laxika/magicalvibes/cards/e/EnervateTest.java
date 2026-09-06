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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Enervate.class, BalduvianBears.class, Forest.class, IcyManipulator.class, MysticRemora.class})
class EnervateTest extends BaseCardTest {

    @Test
    @DisplayName("Taps target creature and schedules a draw at the next upkeep")
    void tapsCreatureAndSchedulesDraw() {
        harness.addToBattlefield(player2, new BalduvianBears());
        harness.setHand(player1, List.of(new Enervate()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID bearsId = harness.getPermanentId(player2, "Balduvian Bears");
        harness.castAndResolveInstant(player1, 0, bearsId);

        Permanent bears = findPermanent(player2, "Balduvian Bears");
        assertThat(bears.isTapped()).isTrue();

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can tap a target land")
    void tapsLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Enervate()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID forestId = harness.getPermanentId(player2, "Forest");
        harness.castAndResolveInstant(player1, 0, forestId);

        Permanent forest = findPermanent(player2, "Forest");
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can tap a noncreature artifact")
    void tapsArtifact() {
        harness.addToBattlefield(player2, new IcyManipulator());
        harness.setHand(player1, List.of(new Enervate()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID manipulatorId = harness.getPermanentId(player2, "Icy Manipulator");
        harness.castAndResolveInstant(player1, 0, manipulatorId);

        Permanent manipulator = findPermanent(player2, "Icy Manipulator");
        assertThat(manipulator.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Draws a card at the beginning of the next turn's upkeep")
    void drawsCardAtNextUpkeep() {
        BalduvianBears drawnCard = new BalduvianBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addToBattlefield(player2, new BalduvianBears());
        harness.setHand(player1, List.of(new Enervate()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID bearsId = harness.getPermanentId(player2, "Balduvian Bears");
        harness.castAndResolveInstant(player1, 0, bearsId);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        harness.addToBattlefield(player2, new MysticRemora());
        harness.setHand(player1, List.of(new Enervate()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID remoraId = harness.getPermanentId(player2, "Mystic Remora");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, remoraId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact, creature, or land");
    }
}
