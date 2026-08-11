package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TempleOfMysteryTest extends BaseCardTest {

    @Test
    void entersTappedAndTriggersScry() {
        harness.setHand(player1, List.of(new TempleOfMystery()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        Permanent temple = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(temple.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    void scryCanPutTopCardOnBottom() {
        harness.setHand(player1, List.of(new TempleOfMystery()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop = deck.getFirst();

        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(deck.getLast()).isSameAs(originalTop);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void producesGreenMana() {
        addReadyTempleOfMystery();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void producesBlueMana() {
        addReadyTempleOfMystery();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    private void addReadyTempleOfMystery() {
        Permanent temple = new Permanent(new TempleOfMystery());
        temple.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(temple);
    }
}
