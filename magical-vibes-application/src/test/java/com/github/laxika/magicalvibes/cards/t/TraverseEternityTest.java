package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CaptainAmericaLivingLegend;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.ObeliskOfBant;
import com.github.laxika.magicalvibes.cards.r.RavenGuildInitiate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TraverseEternity.class, ObeliskOfBant.class, CaptainAmericaLivingLegend.class,
        TheCloneSaga.class, GrizzlyBears.class, RavenGuildInitiate.class})
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

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

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

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Traverse Eternity");
    }
}
