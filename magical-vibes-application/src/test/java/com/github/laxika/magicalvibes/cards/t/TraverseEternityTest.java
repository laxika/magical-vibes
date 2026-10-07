package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CaptainAmericaLivingLegend;
import com.github.laxika.magicalvibes.cards.a.AdiposeOffspring;
import com.github.laxika.magicalvibes.cards.a.AnUnearthlyChild;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.ObeliskOfBant;
import com.github.laxika.magicalvibes.cards.r.RavenGuildInitiate;
import com.github.laxika.magicalvibes.cards.s.SonicScrewdriver;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TraverseEternity.class, ObeliskOfBant.class, CaptainAmericaLivingLegend.class,
        TheCloneSaga.class, GrizzlyBears.class, RavenGuildInitiate.class,
        AdiposeOffspring.class, AnUnearthlyChild.class, SonicScrewdriver.class, TheFirstDoctor.class})
class TraverseEternityTest extends BaseCardTest {

    @Test
    @DisplayName("Draws cards for the greatest mana value among your historic permanents")
    void drawsForGreatestHistoricPermanentManaValue() {
        harness.addToBattlefield(player1, new ObeliskOfBant());
        harness.addToBattlefield(player1, new CaptainAmericaLivingLegend());
        harness.addToBattlefield(player1, new TheCloneSaga());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new TheCloneSaga());
        harness.setLibrary(player1, List.of(
                new RavenGuildInitiate(), new RavenGuildInitiate(),
                new RavenGuildInitiate(), new RavenGuildInitiate()));
        harness.setHand(player1, List.of(new TraverseEternity()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Draws no cards when you control no historic permanents")
    void drawsNothingWithoutHistoricPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new TheCloneSaga());
        harness.setLibrary(player1, List.of(
                new RavenGuildInitiate(), new RavenGuildInitiate()));
        harness.setHand(player1, List.of(new TraverseEternity()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Traverse Eternity");
    }

    @Test
    void countsAnArtifactWithoutOtherHistoricPermanents() {
        assertDrawsThreeFor(new SonicScrewdriver());
    }

    @Test
    void countsALegendaryCreatureWithoutOtherHistoricPermanents() {
        assertDrawsThreeFor(new TheFirstDoctor());
    }

    @Test
    void countsASagaWithoutOtherHistoricPermanents() {
        assertDrawsThreeFor(new AnUnearthlyChild());
    }

    private void assertDrawsThreeFor(Card historicPermanent) {
        harness.addToBattlefield(player1, historicPermanent);
        harness.addToBattlefield(player1, new AdiposeOffspring());
        harness.setLibrary(player1, List.of(new AdiposeOffspring(), new AdiposeOffspring(),
                new AdiposeOffspring(), new AdiposeOffspring()));
        harness.setHand(player1, List.of(new TraverseEternity()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void usesHistoricPermanentsPresentAtResolution() {
        harness.setLibrary(player1, List.of(new AdiposeOffspring(), new AdiposeOffspring(),
                new AdiposeOffspring(), new AdiposeOffspring()));
        harness.setHand(player1, List.of(new TraverseEternity()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.addToBattlefield(player1, new SonicScrewdriver());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
