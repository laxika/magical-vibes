package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.cards.a.AvenSmokeweaver;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mudhole.class, Island.class, AvenFisher.class, AvenSmokeweaver.class, Peek.class})
class MudholeTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles all land cards from the target player's graveyard")
    void exilesAllLandCardsFromTargetGraveyard() {
        harness.setGraveyard(player2, List.of(
                new Island(), new AvenFisher(), new Island(), new AvenSmokeweaver(), new Peek()));
        harness.setHand(player1, List.of(new Mudhole()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        harness.assertInGraveyard(player2, "Aven Fisher");
        harness.assertInGraveyard(player2, "Aven Smokeweaver");
        harness.assertInGraveyard(player2, "Peek");
    }

    @Test
    @DisplayName("Only the targeted player's land cards are exiled")
    void onlyAffectsTargetPlayer() {
        harness.setGraveyard(player1, List.of(new Island()));
        harness.setGraveyard(player2, List.of(new Island(), new AvenFisher()));
        harness.setHand(player1, List.of(new Mudhole()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Island");
        harness.assertInGraveyard(player2, "Aven Fisher");
    }

    @Test
    @DisplayName("Can target its controller's graveyard")
    void canTargetController() {
        harness.setGraveyard(player1, List.of(new Island(), new AvenFisher()));
        harness.setGraveyard(player2, List.of(new Island()));
        harness.setHand(player1, List.of(new Mudhole()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Aven Fisher");
        harness.assertInGraveyard(player2, "Island");
    }

    @Test
    @DisplayName("Can target a player with an empty graveyard")
    void resolvesWithEmptyGraveyard() {
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new Mudhole()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Mudhole");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Leaves a graveyard containing only nonland cards unchanged")
    void resolvesWithoutMatchingLandCards() {
        AvenFisher fisher = new AvenFisher();
        Peek peek = new Peek();
        harness.setGraveyard(player2, List.of(fisher, peek));
        harness.setHand(player1, List.of(new Mudhole()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(fisher, peek);
        harness.assertInGraveyard(player1, "Mudhole");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exiles land cards that entered the graveyard after casting")
    void checksGraveyardAtResolution() {
        AvenFisher fisher = new AvenFisher();
        Island island = new Island();
        harness.setGraveyard(player2, List.of(fisher));
        harness.setHand(player1, List.of(new Mudhole()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.setGraveyard(player2, List.of(fisher, island));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(island);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(fisher);
        harness.assertInGraveyard(player1, "Mudhole");
    }
}
