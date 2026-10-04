package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.PalladiumMyr;
import com.github.laxika.magicalvibes.cards.s.SwiftResponse;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChromeReplicator.class, Island.class, PalladiumMyr.class, SwiftResponse.class})
class ChromeReplicatorTest extends BaseCardTest {

    @Test
    @DisplayName("creates a Construct token when you control another matching nonland nontoken permanent")
    void createsTokenForMatchingPermanents() {
        harness.addToBattlefield(player1, new ChromeReplicator());
        castChromeReplicator();

        resolveAllTriggers();

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

    @Test
    void matchingPairNeedNotShareChromeReplicatorsName() {
        harness.addToBattlefield(player1, new PalladiumMyr());
        harness.addToBattlefield(player1, new PalladiumMyr());
        castChromeReplicator();
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Construct")).hasSize(1);
    }

    @Test
    void matchingLandsDoNotCount() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        castChromeReplicator();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Construct")).isEmpty();
    }

    @Test
    void differentlyNamedNonlandPermanentsDoNotCount() {
        harness.addToBattlefield(player1, new PalladiumMyr());
        castChromeReplicator();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Construct")).isEmpty();
    }

    @Test
    void multipleMatchingPairsCreateOnlyOneToken() {
        harness.addToBattlefield(player1, new PalladiumMyr());
        harness.addToBattlefield(player1, new PalladiumMyr());
        harness.addToBattlefield(player1, new ChromeReplicator());
        castChromeReplicator();
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Construct")).hasSize(1);
    }

    @Test
    void losingMatchingPairBeforeResolutionPreventsToken() {
        Permanent matching = harness.addToBattlefieldAndReturn(player1, new ChromeReplicator());
        matching.tap();
        castChromeReplicator();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        destroyTappedCreature(matching);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Chrome Replicator");
        assertThat(findPermanents(player1, "Construct")).isEmpty();
    }

    @Test
    void removingSourceDoesNotPreventTokenWhenAnotherPairRemains() {
        harness.addToBattlefield(player1, new PalladiumMyr());
        harness.addToBattlefield(player1, new PalladiumMyr());
        castChromeReplicator();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        Permanent source = findPermanents(player1, "Chrome Replicator").getFirst();
        source.tap();

        destroyTappedCreature(source);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Chrome Replicator");
        assertThat(findPermanents(player1, "Construct")).hasSize(1);
    }

    private void destroyTappedCreature(Permanent target) {
        harness.setHand(player1, List.of(new SwiftResponse()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
    }

    private void castChromeReplicator() {
        harness.castFromHand(player1, new ChromeReplicator(), "{5}");
    }
}
