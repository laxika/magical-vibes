package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BoggartBrute;
import com.github.laxika.magicalvibes.cards.f.FaerieMiscreant;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkyclavePlunder.class, BoggartBrute.class, FaerieMiscreant.class,
        FugitiveWizard.class, GrizzlyBears.class, SoulWarden.class})
class SkyclavePlunderTest extends BaseCardTest {

    @Test
    @DisplayName("Looks at three plus the size of your party and puts three cards into your hand")
    void looksAtCardsBasedOnPartySize() {
        Card c0 = new GrizzlyBears();
        Card c1 = new GrizzlyBears();
        Card c2 = new GrizzlyBears();
        Card c3 = new GrizzlyBears();
        Card c4 = new GrizzlyBears();
        Card c5 = new GrizzlyBears();
        Card c6 = new GrizzlyBears();
        Card untouched = new GrizzlyBears();
        harness.setLibrary(player1, List.of(c0, c1, c2, c3, c4, c5, c6, untouched));
        addFullParty();

        castPlunder();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(c0, c1, c2, c3, c4, c5, c6);

        harness.handleMultipleCardsChosen(player1, List.of(c0.getId(), c2.getId(), c5.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(c0, c2, c5);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 5))
                .containsExactlyInAnyOrder(c1, c3, c4, c6);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no party, looks at three cards and keeps all three in a small library")
    void noPartyLooksAtThreeCards() {
        Card c0 = new GrizzlyBears();
        Card c1 = new GrizzlyBears();
        Card c2 = new GrizzlyBears();
        Card untouched = new GrizzlyBears();
        harness.setLibrary(player1, List.of(c0, c1, c2, untouched));

        castPlunder();

        assertThat(gd.playerHands.get(player1.getId())).contains(c0, c1, c2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addFullParty() {
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new FaerieMiscreant());
        harness.addToBattlefield(player1, new BoggartBrute());
        harness.addToBattlefield(player1, new FugitiveWizard());
    }

    private void castPlunder() {
        harness.setHand(player1, List.of(new SkyclavePlunder()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
