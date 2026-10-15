package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TempleOfDeceit.class, Forest.class})
class TempleOfDeceitTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and triggers scry 1")
    void entersTappedAndScries() {
        harness.setHand(player1, List.of(new TempleOfDeceit()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        Permanent temple = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(temple.isTapped()).isTrue();
        assertThat(gd.stack).singleElement()
                .satisfies(entry -> {
                    assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
                    assertThat(entry.getCard().getName()).isEqualTo("Temple of Deceit");
                });

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Can tap for blue or black mana")
    void tapsForBlueOrBlackMana() {
        harness.addToBattlefield(player1, new TempleOfDeceit());
        harness.addToBattlefield(player1, new TempleOfDeceit());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Scry can keep the top card without changing either library")
    void scryCanKeepTopCard() {
        Forest top = new Forest();
        Forest next = new Forest();
        Forest opponentsTop = new Forest();
        harness.setHand(player1, List.of(new TempleOfDeceit()));
        harness.setLibrary(player1, List.of(top, next));
        harness.setLibrary(player2, List.of(opponentsTop));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, next);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsTop);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Scry can put the top card on the bottom")
    void scryCanBottomTopCard() {
        Forest top = new Forest();
        Forest next = new Forest();
        harness.setHand(player1, List.of(new TempleOfDeceit()));
        harness.setLibrary(player1, List.of(top, next));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Scry resolves with an empty library without requesting a choice")
    void scryWithEmptyLibrary() {
        harness.setHand(player1, List.of(new TempleOfDeceit()));
        harness.setLibrary(player1, List.of());

        harness.playLand(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
