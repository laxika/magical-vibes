package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TempleOfMystery.class, Forest.class})
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

    @Test
    void scryMovesOnlyTopCardBelowTheRestOfTheLibrary() {
        Card top = new Forest();
        Card second = new Forest();
        harness.setHand(player1, List.of(new TempleOfMystery()));
        harness.setLibrary(player1, List.of(top, second));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, top);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void scryCanKeepTopCardAboveTheRestOfTheLibrary() {
        Card top = new Forest();
        Card second = new Forest();
        harness.setHand(player1, List.of(new TempleOfMystery()));
        harness.setLibrary(player1, List.of(top, second));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void scryWithEmptyLibraryResolvesWithoutInteraction() {
        harness.setHand(player1, List.of(new TempleOfMystery()));
        harness.setLibrary(player1, List.of());

        harness.playLand(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void manaAbilityTapsLandAndResolvesImmediatelyEvenWhenSummoningSick() {
        Permanent temple = harness.addToBattlefieldAndReturn(player1, new TempleOfMystery());
        temple.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(temple.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void addReadyTempleOfMystery() {
        Permanent temple = harness.addToBattlefieldAndReturn(player1, new TempleOfMystery());
        temple.setSummoningSick(false);
    }
}
