package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GlimpseOfFreedom;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GeistOfRegret.class, CounselOfTheSoratami.class, Forest.class, Shock.class,
        GlimpseOfFreedom.class, GrizzlyBears.class})
class GeistOfRegretTest extends BaseCardTest {

    @Test
    void putsOneRandomInstantAndSorceryFromLibraryIntoGraveyard() {
        Forest land = new Forest();
        Shock instant = new Shock();
        CounselOfTheSoratami sorcery = new CounselOfTheSoratami();
        GeistOfRegret geist = new GeistOfRegret();
        harness.setLibrary(player1, List.of(land, instant, sorcery));
        harness.setHand(player1, List.of(geist));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(instant, sorcery);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    void copiesAnInstantOrSorceryCastFromTheGraveyard() {
        GeistOfRegret geist = new GeistOfRegret();
        GlimpseOfFreedom glimpse = new GlimpseOfFreedom();
        List<GrizzlyBears> exileCost = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears());
        List<Card> drawnCards = List.of(new GrizzlyBears(), new GrizzlyBears());
        harness.addToBattlefield(player1, geist);
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(glimpse, exileCost.get(0), exileCost.get(1),
                exileCost.get(2), exileCost.get(3), exileCost.get(4)));
        harness.setLibrary(player1, drawnCards);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 4, 5));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(drawnCards);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(glimpse);
    }
}
