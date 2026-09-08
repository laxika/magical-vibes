package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Hypergenesis.class, Forest.class, GrizzlyBears.class, LightningBolt.class})
class HypergenesisTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Hypergenesis with three time counters")
    void suspendExilesWithThreeTimeCounters() {
        Hypergenesis card = suspendCard(List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
    }

    @Test
    @DisplayName("Repeats after a decline and puts permanents onto the battlefield sequentially")
    void repeatsAfterDeclineAndEntersPermanentsSequentially() {
        GrizzlyBears bears = new GrizzlyBears();
        Forest firstForest = new Forest();
        Forest secondForest = new Forest();
        LightningBolt bolt = new LightningBolt();
        suspendCard(List.of(bears, firstForest, bolt));
        harness.setHand(player2, List.of(secondForest, new Forest()));

        resolveSuspendedHypergenesis();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.EachPlayerMayPutCardFromHandChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMultipleCardsChosen(player2, List.of(secondForest.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Forest");

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Grizzly Bears");
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.EachPlayerMayPutCardFromHandChoice.class);

        harness.handleMultipleCardsChosen(player2, List.of(gd.playerHands.get(player2.getId()).getFirst().getId()));
        harness.handleMultipleCardsChosen(player1, List.of(firstForest.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Grizzly Bears", "Forest");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Forest", "Forest");
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Lightning Bolt");
    }

    @Test
    @DisplayName("Does nothing when no player has an eligible permanent")
    void doesNothingWithoutEligibleCards() {
        LightningBolt bolt = new LightningBolt();
        suspendCard(List.of(bolt));
        harness.setHand(player2, List.of());

        resolveSuspendedHypergenesis();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Lightning Bolt");
        harness.assertInGraveyard(player1, "Hypergenesis");
    }

    private Hypergenesis suspendCard(List<Card> additionalHandCards) {
        Hypergenesis card = new Hypergenesis();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        harness.setHand(player1, additionalHandCards);
        return card;
    }

    private void resolveSuspendedHypergenesis() {
        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
    }
}
