package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LilianasTriumph.class, LilianaWakerOfTheDead.class, GiantSpider.class, GrizzlyBears.class})
class LilianasTriumphTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent sacrifices a creature")
    void eachOpponentSacrificesCreature() {
        harness.addToBattlefield(player2, new GiantSpider());

        castTriumph();

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A Liliana planeswalker also makes each opponent discard a card")
    void lilianaAlsoMakesEachOpponentDiscard() {
        Permanent liliana = harness.addToBattlefieldAndReturn(player1, new LilianaWakerOfTheDead());
        liliana.setCounterCount(CounterType.LOYALTY, 4);
        harness.addToBattlefield(player2, new GiantSpider());
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(discarded)));

        castTriumph();

        harness.assertInGraveyard(player2, "Giant Spider");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Liliana does not enable the discard")
    void opponentsLilianaDoesNotEnableDiscard() {
        Permanent liliana = harness.addToBattlefieldAndReturn(player2, new LilianaWakerOfTheDead());
        liliana.setCounterCount(CounterType.LOYALTY, 4);
        harness.addToBattlefield(player2, new GiantSpider());
        GrizzlyBears kept = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(kept)));

        castTriumph();

        harness.assertInGraveyard(player2, "Giant Spider");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent chooses the sacrificed creature before choosing a discard")
    void opponentChoosesSacrificeThenDiscard() {
        Permanent liliana = harness.addToBattlefieldAndReturn(player1, new LilianaWakerOfTheDead());
        liliana.setCounterCount(CounterType.LOYALTY, 4);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        GiantSpider kept = new GiantSpider();
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setHand(player2, List.of(kept, discarded));

        castTriumph();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept, discarded);
        harness.handleMultiplePermanentsChosen(player2, List.of(sacrificed.getId()));
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Giant Spider");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent still discards with no creature to sacrifice")
    void discardsWithoutCreature() {
        Permanent liliana = harness.addToBattlefieldAndReturn(player1, new LilianaWakerOfTheDead());
        liliana.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player2, List.of(new GrizzlyBears()));

        castTriumph();
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty hand does not prevent sacrificing a creature")
    void sacrificesWithEmptyHand() {
        Permanent liliana = harness.addToBattlefieldAndReturn(player1, new LilianaWakerOfTheDead());
        liliana.setCounterCount(CounterType.LOYALTY, 4);
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player2, List.of());

        castTriumph();

        harness.assertInGraveyard(player2, "Giant Spider");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Without Liliana, an opponent with no creatures keeps their hand")
    void noCreaturesAndNoLiliana() {
        GrizzlyBears kept = new GrizzlyBears();
        harness.setHand(player2, List.of(kept));

        castTriumph();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castTriumph() {
        harness.setHand(player1, List.of(new LilianasTriumph()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);
    }
}
