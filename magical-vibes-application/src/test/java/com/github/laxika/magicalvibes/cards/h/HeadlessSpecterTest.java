package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeadlessSpecter.class, GrizzlyBears.class})
class HeadlessSpecterTest extends BaseCardTest {

    @Test
    @DisplayName("With an empty hand, combat damage makes the damaged player discard at random")
    void emptyHandTriggersRandomDiscard() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GrizzlyBears()));

        Permanent specter = addCreatureReady(player1, new HeadlessSpecter());
        specter.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("With a card in hand, combat damage does not trigger the hellbent ability")
    void cardsInHandPreventTrigger() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        Permanent specter = addCreatureReady(player1, new HeadlessSpecter());
        specter.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }
}
