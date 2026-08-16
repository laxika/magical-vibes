package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChromeReplicatorTest extends BaseCardTest {

    @Test
    @DisplayName("creates a Construct token when you control another matching nonland nontoken permanent")
    void createsTokenForMatchingPermanents() {
        harness.addToBattlefield(player1, new ChromeReplicator());
        castChromeReplicator();

        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Construct");
        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0).getCard().isToken()).isTrue();
        assertThat(tokens.get(0).getCard().getPower()).isEqualTo(4);
        assertThat(tokens.get(0).getCard().getToughness()).isEqualTo(4);
        assertThat(tokens.get(0).getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(tokens.get(0).getCard().hasType(CardType.ARTIFACT)).isTrue();
    }

    @Test
    @DisplayName("does not count a matching permanent controlled by an opponent")
    void opponentPermanentDoesNotCount() {
        harness.addToBattlefield(player2, new ChromeReplicator());
        castChromeReplicator();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Construct")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("does not count a matching token")
    void matchingTokenDoesNotCount() {
        Permanent token = harness.addToBattlefieldAndReturn(player1, new ChromeReplicator());
        TestCards.mutableCard(token).setToken(true);
        castChromeReplicator();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Construct")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void castChromeReplicator() {
        harness.setHand(player1, List.of(new ChromeReplicator()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
    }
}
