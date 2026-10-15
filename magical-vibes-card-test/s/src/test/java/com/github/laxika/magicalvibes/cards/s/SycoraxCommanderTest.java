package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.effect.EachOpponentFacesSycoraxCommanderVillainousChoiceEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SycoraxCommander.class, Forest.class, SwordsToPlowshares.class})
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
        List<Card> discarded = List.of(new SycoraxCommander(), new SycoraxCommander(), new SycoraxCommander());
        List<Card> drawn = List.of(new Forest(), new Forest(), new Forest());
        harness.setLibrary(player2, drawn);
        castCommander(discarded);

        harness.handleListChoice(player2,
                EachOpponentFacesSycoraxCommanderVillainousChoiceEffect.DISCARD_OPTION);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(discarded);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn.get(0), drawn.get(1));
    }

    @Test
    void emptyHandCanChooseDiscardWithoutDrawing() {
        Card topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard));
        castCommander(List.of());

        harness.handleListChoice(player2,
                EachOpponentFacesSycoraxCommanderVillainousChoiceEffect.DISCARD_OPTION);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyHandCanChooseDamageWithoutLosingLife() {
        castCommander(List.of());

        harness.handleListChoice(player2,
                EachOpponentFacesSycoraxCommanderVillainousChoiceEffect.DAMAGE_OPTION);

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void oneCardHandIsDiscardedWithoutDrawing() {
        Card discarded = new SycoraxCommander();
        Card topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard));
        castCommander(List.of(discarded));

        harness.handleListChoice(player2,
                EachOpponentFacesSycoraxCommanderVillainousChoiceEffect.DISCARD_OPTION);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
        harness.assertLife(player2, 20);
    }

    @Test
    void damageUsesCurrentHandSizeEvenAfterCommanderIsExiled() {
        List<Card> remainingHand = List.of(new Forest(), new Forest());
        harness.setHand(player2, List.of(new SwordsToPlowshares(),
                remainingHand.get(0), remainingHand.get(1)));
        harness.castFromHand(player1, new SycoraxCommander(), "{2}{B}{R}");
        harness.passBothPriorities();

        var commander = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, commander.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Sycorax Commander");
        harness.passBothPriorities();
        harness.handleListChoice(player2,
                EachOpponentFacesSycoraxCommanderVillainousChoiceEffect.DAMAGE_OPTION);

        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyElementsOf(remainingHand);
        assertThat(gd.stack).isEmpty();
    }

    private void castCommander(List<Card> opponentHand) {
        harness.setHand(player2, opponentHand);
        harness.castFromHand(player1, new SycoraxCommander(), "{2}{B}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
