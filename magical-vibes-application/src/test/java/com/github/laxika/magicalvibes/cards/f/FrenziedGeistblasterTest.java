package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrenziedGeistblaster.class, GrizzlyBears.class, Shock.class, Forest.class})
class FrenziedGeistblasterTest extends BaseCardTest {

    @Test
    void discardingSeeksAnInstantOrSorceryWhenThresholdIsMet() {
        FrenziedGeistblaster blaster = new FrenziedGeistblaster();
        GrizzlyBears discard = new GrizzlyBears();
        Shock sought = new Shock();
        Forest notSought = new Forest();
        setUp(blaster, discard, 10, 9, List.of(sought, notSought));

        castBlaster(blaster);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).contains(sought);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(notSought);
    }

    @Test
    void decliningDoesNotDiscardOrSeek() {
        FrenziedGeistblaster blaster = new FrenziedGeistblaster();
        GrizzlyBears discard = new GrizzlyBears();
        Shock sought = new Shock();
        Forest notSought = new Forest();
        setUp(blaster, discard, 10, 9, List.of(sought, notSought));

        castBlaster(blaster);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(11).contains(discard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sought, notSought);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(9);
    }

    @Test
    void thresholdCountsOnlyInstantAndSorceryCards() {
        FrenziedGeistblaster blaster = new FrenziedGeistblaster();
        GrizzlyBears discard = new GrizzlyBears();
        Shock sought = new Shock();
        Forest notSought = new Forest();
        setUp(blaster, discard, 9, 9, List.of(sought, notSought));

        castBlaster(blaster);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(discard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sought, notSought);
    }

    private void setUp(FrenziedGeistblaster blaster, GrizzlyBears discard, int handSpellCount,
                       int graveyardSpellCount, List<Card> library) {
        List<Card> hand = new ArrayList<>();
        hand.add(blaster);
        hand.add(discard);
        hand.addAll(shocks(handSpellCount));
        harness.setHand(player1, hand);
        harness.setGraveyard(player1, shocks(graveyardSpellCount));
        harness.setLibrary(player1, library);
    }

    private void castBlaster(FrenziedGeistblaster blaster) {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private List<Card> shocks(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Shock());
        }
        return cards;
    }
}
