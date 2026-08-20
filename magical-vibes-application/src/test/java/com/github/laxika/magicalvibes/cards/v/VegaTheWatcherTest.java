package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AncientGrudge;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VegaTheWatcherTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell from the graveyard draws a card")
    void castingFromGraveyardDrawsCard() {
        harness.addToBattlefield(player1, new VegaTheWatcher());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setGraveyard(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFlashback(player1, 0, harness.getPermanentId(player1, "Fountain of Youth"));
        resolveStack();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("Casting a spell from hand does not draw a card")
    void castingFromHandDrawsNothing() {
        harness.addToBattlefield(player1, new VegaTheWatcher());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, new ArrayList<>(List.of(new AncientGrudge())));
        harness.addMana(player1, ManaColor.RED, 2);
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Fountain of Youth"));
        resolveStack();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    private void resolveStack() {
        for (int i = 0; i < 8 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }
    }
}
