package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TempleOfPlentyTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and triggers scry 1")
    void entersTappedAndScries() {
        harness.setHand(player1, List.of(new TempleOfPlenty()));
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop = deck.getFirst();

        harness.playLand(player1, 0);

        Permanent temple = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(temple.isTapped()).isTrue();

        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(1);

        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(deck.getLast()).isSameAs(originalTop);
    }

    @ParameterizedTest
    @ValueSource(strings = {"GREEN", "WHITE"})
    @DisplayName("Mana ability adds the chosen color and taps the land")
    void manaAbilityAddsChosenColor(String color) {
        Permanent temple = addReadyTemple();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.valueOf(color))).isEqualTo(1);
        assertThat(temple.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyTemple() {
        Permanent temple = new Permanent(new TempleOfPlenty());
        temple.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(temple);
        return temple;
    }
}
