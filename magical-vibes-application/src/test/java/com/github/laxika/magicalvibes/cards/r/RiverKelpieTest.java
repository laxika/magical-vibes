package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AncientGrudge;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RiverKelpieTest extends BaseCardTest {

    private void resolveStack() {
        for (int i = 0; i < 8 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }
    }

    @Test
    @DisplayName("Drawing a card when a permanent enters from a graveyard")
    void drawsWhenPermanentEntersFromGraveyard() {
        harness.addToBattlefield(player1, new RiverKelpie());
        harness.setGraveyard(player1, List.of(new ReassemblingSkeleton()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        // Reassembling Skeleton returns itself from the graveyard to the battlefield.
        harness.activateGraveyardAbility(player1, 0);
        resolveStack();

        harness.assertOnBattlefield(player1, "Reassembling Skeleton");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Drawing a card when a spell is cast from a graveyard")
    void drawsWhenSpellCastFromGraveyard() {
        harness.addToBattlefield(player1, new RiverKelpie());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setGraveyard(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castFlashback(player1, 0, targetId);
        resolveStack();

        // The cast-from-graveyard trigger drew a card (net +1, independent of the spell's effect).
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("No draw when a spell is cast from hand")
    void noDrawWhenSpellCastFromHand() {
        harness.addToBattlefield(player1, new RiverKelpie());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.RED, 2);

        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castInstant(player1, 0, targetId);
        resolveStack();

        // Casting from hand does not trigger the graveyard-cast draw — the library is untouched.
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }
}
