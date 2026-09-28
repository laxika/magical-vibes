package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.effect.EachOpponentFacesSycoraxCommanderVillainousChoiceEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SycoraxCommander.class, Forest.class, GrizzlyBears.class})
class SycoraxCommanderTest extends BaseCardTest {

    @Test
    void opponentCanChooseDamageEqualToHandSize() {
        List<Card> hand = List.of(new Forest(), new Forest(), new Forest());
        castCommander(hand);

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.options()).containsExactly(
                EachOpponentFacesSycoraxCommanderVillainousChoiceEffect.DISCARD_OPTION,
                EachOpponentFacesSycoraxCommanderVillainousChoiceEffect.DAMAGE_OPTION);

        harness.handleListChoice(player2,
                EachOpponentFacesSycoraxCommanderVillainousChoiceEffect.DAMAGE_OPTION);

        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyElementsOf(hand);
    }

    @Test
    void opponentCanDiscardHandAndDrawOneFewerCard() {
        List<Card> discarded = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        List<Card> drawn = List.of(new Forest(), new Forest(), new Forest());
        harness.setLibrary(player2, drawn);
        castCommander(discarded);

        harness.handleListChoice(player2,
                EachOpponentFacesSycoraxCommanderVillainousChoiceEffect.DISCARD_OPTION);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(discarded);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn.get(0), drawn.get(1));
    }

    private void castCommander(List<Card> opponentHand) {
        harness.setHand(player1, List.of(new SycoraxCommander()));
        harness.setHand(player2, opponentHand);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
