package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinGangLeader.class, Shock.class})
class GoblinGangLeaderTest extends BaseCardTest {

    @Test
    void enteringCreatesTwoOneOneRedGoblinTokens() {
        harness.setHand(player1, List.of(new GoblinGangLeader()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Goblin").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN);
        });
    }

    @Test
    void tokensAreCreatedOnlyWhenEnterTriggerResolves() {
        harness.setHand(player1, List.of(new GoblinGangLeader()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);

        harness.assertNotOnBattlefield(player1, "Goblin Gang Leader");
        assertThat(findPermanents(player1, "Goblin")).isEmpty();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goblin Gang Leader");
        assertThat(findPermanents(player1, "Goblin")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goblin")).hasSize(2);
        assertThat(findPermanents(player2, "Goblin")).isEmpty();
    }

    @Test
    void enteringWithoutCastingCreatesTokensForItsController() {
        harness.enterBattlefieldAndReturn(player2, new GoblinGangLeader());

        resolveAllTriggers();

        assertThat(findPermanents(player2, "Goblin")).hasSize(2).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.isTapped()).isFalse();
            assertThat(token.isSummoningSick()).isTrue();
        });
        assertThat(findPermanents(player1, "Goblin")).isEmpty();
    }

    @Test
    void triggerStillCreatesTokensAfterLeaderDies() {
        harness.setHand(player1, List.of(new GoblinGangLeader(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent leader = findPermanent(player1, "Goblin Gang Leader");
        harness.castInstant(player1, 0, leader.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goblin Gang Leader");
        harness.assertInGraveyard(player1, "Goblin Gang Leader");
        assertThat(findPermanents(player1, "Goblin")).isEmpty();

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goblin")).hasSize(2);
        assertThat(findPermanents(player2, "Goblin")).isEmpty();
    }
}
