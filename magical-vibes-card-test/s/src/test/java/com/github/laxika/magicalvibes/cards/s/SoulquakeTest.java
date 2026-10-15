package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Soulquake.class, GrizzlyBears.class, SerraAngel.class, GloriousAnthem.class,
        Mountain.class, DarksteelRelic.class})
class SoulquakeTest extends BaseCardTest {

    private void castSoulquake() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Soulquake(), "{3}{U}{U}{B}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Returns all creatures on the battlefield to their owners' hands")
    void returnsAllCreaturesFromBattlefield() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SerraAngel());

        castSoulquake();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.CREATURE));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.CREATURE));

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName).contains("Serra Angel");
    }

    @Test
    @DisplayName("Returns all creature cards in graveyards to their owners' hands")
    void returnsCreatureCardsFromGraveyards() {
        Card myCreature = new GrizzlyBears();
        Card theirCreature = new SerraAngel();
        harness.setGraveyard(player1, List.of(myCreature));
        harness.setGraveyard(player2, List.of(theirCreature));

        castSoulquake();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName).contains("Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName).contains("Serra Angel");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(myCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(theirCreature);
    }

    @Test
    @DisplayName("Returns both battlefield creatures and graveyard creature cards at once")
    void returnsBattlefieldAndGraveyardTogether() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card graveyardCreature = new SerraAngel();
        harness.setGraveyard(player2, List.of(graveyardCreature));

        castSoulquake();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName).contains("Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName).contains("Serra Angel");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.CREATURE));
    }

    @Test
    @DisplayName("Leaves non-creature permanents and non-creature graveyard cards untouched")
    void leavesNonCreaturesAlone() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        Card land = new Mountain();
        Card artifact = new DarksteelRelic();
        harness.setGraveyard(player1, List.of(land, artifact));

        castSoulquake();

        harness.assertOnBattlefield(player1, "Glorious Anthem");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land, artifact);
    }

    @Test
    @DisplayName("Works with empty battlefields and graveyards (no crash)")
    void worksWithEmptyState() {
        castSoulquake();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Soulquake");
    }

    @Test
    @DisplayName("Returns stolen creatures to their owners rather than their controllers")
    void returnsStolenCreatureToOwner() {
        Card stolenCard = new GrizzlyBears();
        Permanent stolen = harness.addToBattlefieldAndReturn(player1, stolenCard);
        gd.stolenCreatures.put(stolen.getId(), player2.getId());

        castSoulquake();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(stolenCard);
    }

    @Test
    @DisplayName("Returns every creature card from both graveyards while leaving other cards")
    void returnsMultipleCreaturesFromEachGraveyard() {
        Card myBears = new GrizzlyBears();
        Card myAngel = new SerraAngel();
        Card theirBears = new GrizzlyBears();
        Card theirAngel = new SerraAngel();
        Card myLand = new Mountain();
        Card theirArtifact = new DarksteelRelic();
        harness.setGraveyard(player1, List.of(myBears, myLand, myAngel));
        harness.setGraveyard(player2, List.of(theirBears, theirArtifact, theirAngel));

        castSoulquake();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(myBears, myAngel);
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrder(theirBears, theirAngel);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2).contains(myLand);
        harness.assertInGraveyard(player1, "Soulquake");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(theirArtifact);
    }

    @Test
    @DisplayName("Does not return creature cards from exile")
    void leavesExiledCreaturesAlone() {
        Card exiledCreature = new GrizzlyBears();
        harness.setExile(player2, List.of(exiledCreature));

        castSoulquake();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.findExiledCard(exiledCreature.getId())).isNotNull();
    }
}
