package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.Disentomb;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Regrowth;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OasisOfRenewal.class, Forest.class, GrizzlyBears.class, Regrowth.class, Disentomb.class})
class OasisOfRenewalTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by seeking one land and one nonland card")
    void entersBySeekingLandAndNonland() {
        Forest land = new Forest();
        GrizzlyBears nonland = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(land, nonland));

        harness.enterBattlefieldAndReturn(player1, new OasisOfRenewal());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(land, nonland);
    }

    @Test
    @DisplayName("Seeks for a land card only once each turn")
    void seeksForLandOnlyOnceEachTurn() {
        Forest firstGraveyardLand = new Forest();
        Forest secondGraveyardLand = new Forest();
        Forest soughtLand = new Forest();
        GrizzlyBears unrelatedNonland = new GrizzlyBears();
        harness.setHand(player1, List.of(new Regrowth(), new Regrowth()));
        harness.setGraveyard(player1, List.of(firstGraveyardLand, secondGraveyardLand));
        harness.setLibrary(player1, List.of(soughtLand, unrelatedNonland));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addToBattlefield(player1, new OasisOfRenewal());

        harness.castSorcery(player1, 0, firstGraveyardLand.getId());
        resolveAllTriggers();
        harness.castSorcery(player1, 0, secondGraveyardLand.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(soughtLand);
        assertThat(gd.playerDecks.get(player1.getId())).contains(unrelatedNonland);
        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.hasType(CardType.LAND))).hasSize(3);
    }

    @Test
    @DisplayName("Seeks for a nonland card only once each turn")
    void seeksForNonlandOnlyOnceEachTurn() {
        GrizzlyBears firstGraveyardCreature = new GrizzlyBears();
        GrizzlyBears secondGraveyardCreature = new GrizzlyBears();
        GrizzlyBears soughtCreature = new GrizzlyBears();
        Forest unrelatedLand = new Forest();
        harness.setHand(player1, List.of(new Disentomb(), new Disentomb()));
        harness.setGraveyard(player1, List.of(firstGraveyardCreature, secondGraveyardCreature));
        harness.setLibrary(player1, List.of(soughtCreature, unrelatedLand));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addToBattlefield(player1, new OasisOfRenewal());

        harness.castSorcery(player1, 0, firstGraveyardCreature.getId());
        resolveAllTriggers();
        harness.castSorcery(player1, 0, secondGraveyardCreature.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(soughtCreature);
        assertThat(gd.playerDecks.get(player1.getId())).contains(unrelatedLand);
        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(card -> !card.hasType(CardType.LAND))).hasSize(3);
    }
}
