package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.Duress;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WitsEnd.class, WalkingCorpse.class, Duress.class})
class WitsEndTest extends BaseCardTest {

    private void castWitsEndOn(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new WitsEnd()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castAndResolveSorcery(player1, 0, targetId);
    }

    @Test
    @DisplayName("Target player discards their entire hand")
    void discardsWholeHand() {
        harness.setHand(player2, new ArrayList<>(List.of(new WalkingCorpse(), new Duress())));

        castWitsEndOn(player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Walking Corpse");
        harness.assertInGraveyard(player2, "Duress");
    }

    @Test
    @DisplayName("An empty hand simply discards nothing")
    void emptyHandDoesNothing() {
        harness.setHand(player2, new ArrayList<>());

        castWitsEndOn(player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target its own controller")
    void canTargetController() {
        harness.setHand(player1, new ArrayList<>(List.of(new WitsEnd(), new WalkingCorpse())));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Walking Corpse");
    }

    @Test
    @DisplayName("Discards the hand at resolution and leaves the other player's hand untouched")
    void discardsCurrentHandOnly() {
        WalkingCorpse retainedCard = new WalkingCorpse();
        Duress discardedCard = new Duress();
        harness.setHand(player1, List.of(new WitsEnd(), retainedCard));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, player2.getId());
        harness.setHand(player2, List.of(discardedCard));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discardedCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retainedCard);
        harness.assertInGraveyard(player1, "Wit's End");
    }
}
