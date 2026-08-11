package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HintOfInsanityTest extends BaseCardTest {

    @Test
    @DisplayName("Discards all duplicate nonland cards and leaves unique cards and lands")
    void discardsDuplicateNonlandCards() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new Forest(), new SuntailHawk())));
        harness.setHand(player1, List.of(new HintOfInsanity()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest", "Suntail Hawk");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Grizzly Bears", "Grizzly Bears");
    }

    @Test
    @DisplayName("A hand without duplicate nonland names is unchanged")
    void noDuplicateNonlandNamesDoesNothing() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest(), new SuntailHawk())));
        harness.setHand(player1, List.of(new HintOfInsanity()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears", "Forest", "Suntail Hawk");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
