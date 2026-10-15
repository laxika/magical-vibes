package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TempleOfTriumph.class, Forest.class})
class TempleOfTriumphTest extends BaseCardTest {

    @Test
    void entersTappedAndTriggersScry() {
        harness.setHand(player1, List.of(new TempleOfTriumph()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    void producesRedAndWhiteMana() {
        harness.addToBattlefield(player1, new TempleOfTriumph());
        harness.addToBattlefield(player1, new TempleOfTriumph());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    void scryCanKeepTopCardWithoutDrawing() {
        Forest top = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(top, second));
        harness.setHand(player1, List.of(new TempleOfTriumph()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, second);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void scryCanPutTopCardOnBottomWithoutAffectingOpponentLibrary() {
        Forest top = new Forest();
        Forest second = new Forest();
        Forest opponentTop = new Forest();
        harness.setLibrary(player1, List.of(top, second));
        harness.setLibrary(player2, List.of(opponentTop));
        harness.setHand(player1, List.of(new TempleOfTriumph()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, top);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void scryWithEmptyLibraryCompletesWithoutInput() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new TempleOfTriumph()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }
}
