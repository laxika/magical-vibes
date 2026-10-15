package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CarvenCaryatid.class, Forest.class})
class CarvenCaryatidTest extends BaseCardTest {

    @Test
    @DisplayName("Defender prevents it from attacking")
    void defenderPreventsAttacking() {
        Permanent caryatid = addCreatureReady(player1, new CarvenCaryatid());

        assertThat(als.canAttack(gd, caryatid, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("ETB ability draws one card")
    void etbDrawsOneCard() {
        harness.setHand(player1, List.of(new CarvenCaryatid()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Carven Caryatid");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("ETB draw waits for its trigger to resolve and draws exactly one card for its controller")
    void etbDrawUsesStackAndOnlyDrawsOneCardForController() {
        Forest topCard = new Forest();
        Forest nextCard = new Forest();
        Forest opponentCard = new Forest();
        harness.setHand(player1, List.of(new CarvenCaryatid()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setLibrary(player2, List.of(opponentCard));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Carven Caryatid");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.stack).isEmpty();
    }
}
