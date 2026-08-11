package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MudholeTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles all land cards from the target player's graveyard")
    void exilesAllLandCardsFromTargetGraveyard() {
        harness.setGraveyard(player2, new ArrayList<>(List.of(
                new Island(), new GrizzlyBears(), new Island(), new Shock(), new Peek())));
        harness.setHand(player1, List.of(new Mudhole()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player2, "Peek");
    }

    @Test
    @DisplayName("Only the targeted player's land cards are exiled")
    void onlyAffectsTargetPlayer() {
        harness.setGraveyard(player1, new ArrayList<>(List.of(new Island())));
        harness.setGraveyard(player2, new ArrayList<>(List.of(new Island(), new Shock())));
        harness.setHand(player1, List.of(new Mudhole()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Island");
        harness.assertInGraveyard(player2, "Shock");
    }
}
