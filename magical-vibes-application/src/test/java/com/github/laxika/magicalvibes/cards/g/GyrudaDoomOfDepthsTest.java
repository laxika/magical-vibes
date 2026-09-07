package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GyrudaDoomOfDepths.class, GrizzlyBears.class, LlanowarElves.class, Forest.class,
        RestInPeace.class})
class GyrudaDoomOfDepthsTest extends BaseCardTest {

    @Test
    @DisplayName("mills each player and returns one even-mana-value creature from all milled cards")
    void millsEachPlayerAndReturnsChosenCreature() {
        Card opponentBears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new LlanowarElves(), new Forest(), opponentBears, new Forest()));

        castGyruda();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(opponentBears.getId());
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1, List.of(opponentBears.getId()));

        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(opponentBears);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == opponentBears);
    }

    @Test
    @DisplayName("does not ask for a creature when no eligible card was milled")
    void doesNotAskWhenNoEligibleCreatureWasMilled() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new LlanowarElves(), new LlanowarElves(),
                new LlanowarElves(), new LlanowarElves()));

        castGyruda();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("can choose a creature diverted to exile by a replacement effect")
    void canChooseCreatureDivertedToExile() {
        Card opponentBears = new GrizzlyBears();
        harness.addToBattlefield(player1, new RestInPeace());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new LlanowarElves(), new Forest(), opponentBears, new Forest()));

        castGyruda();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(opponentBears.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opponentBears);

        harness.handleMultipleCardsChosen(player1, List.of(opponentBears.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(opponentBears);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == opponentBears);
    }

    private void castGyruda() {
        harness.setHand(player1, List.of(new GyrudaDoomOfDepths()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
