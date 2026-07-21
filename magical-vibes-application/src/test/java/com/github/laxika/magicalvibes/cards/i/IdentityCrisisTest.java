package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IdentityCrisisTest extends BaseCardTest {

    private void addCost() {
        // {2}{W}{W}{B}{B} — surplus is harmless and keeps payment order-independent.
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLACK, 4);
    }

    @Test
    @DisplayName("Exiles all cards from target player's hand and graveyard")
    void exilesHandAndGraveyard() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Shock())));
        harness.setGraveyard(player2, new ArrayList<>(List.of(new Peek(), new Island())));
        harness.setHand(player1, List.of(new IdentityCrisis()));
        addCost();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Only the targeted player's zones are affected")
    void onlyAffectsTargetPlayer() {
        harness.setHand(player1, new ArrayList<>(List.of(new IdentityCrisis(), new GrizzlyBears())));
        harness.setGraveyard(player1, new ArrayList<>(List.of(new Shock())));
        harness.setHand(player2, new ArrayList<>(List.of(new Peek())));
        harness.setGraveyard(player2, new ArrayList<>(List.of(new Island())));
        addCost();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Target player's hand and graveyard exiled.
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);

        // Caster untouched: still holds Grizzly Bears, graveyard keeps Shock, nothing exiled.
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Resolves with no error when the target has an empty hand and graveyard")
    void emptyZonesNoError() {
        harness.setHand(player2, new ArrayList<>(List.of()));
        harness.setHand(player1, List.of(new IdentityCrisis()));
        addCost();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }
}
