package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.h.HadaFreeblade;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JoinTheRanks.class, HadaFreeblade.class})
class JoinTheRanksTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Join the Ranks creates two 1/1 white Soldier Ally tokens")
    void createsTwoSoldierAllyTokens() {
        harness.castFromHand(player1, new JoinTheRanks(), "{3}{W}");
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Soldier Ally");
        assertThat(tokens).hasSize(2);
        assertThat(findPermanents(player2, "Soldier Ally")).isEmpty();
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER, CardSubtype.ALLY);
        });
    }

    @Test
    @DisplayName("The nonactive player creates untapped creature tokens only when the instant resolves")
    void nonactivePlayerReceivesTokensOnResolution() {
        harness.castFromHand(player2, new JoinTheRanks(), "{3}{W}");

        assertThat(findPermanents(player1, "Soldier Ally")).isEmpty();
        assertThat(findPermanents(player2, "Soldier Ally")).isEmpty();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier Ally")).isEmpty();
        assertThat(findPermanents(player2, "Soldier Ally")).hasSize(2).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.isTapped()).isFalse();
            assertThat(token.isAttacking()).isFalse();
            assertThat(token.isSummoningSick()).isTrue();
        });
        harness.assertInGraveyard(player2, "Join the Ranks");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Both Ally tokens trigger an existing Hada Freeblade")
    void bothTokensTriggerAllyAbility() {
        Permanent freeblade = harness.addToBattlefieldAndReturn(player1, new HadaFreeblade());
        Permanent opposingFreeblade = harness.addToBattlefieldAndReturn(player2, new HadaFreeblade());

        harness.castFromHand(player1, new JoinTheRanks(), "{3}{W}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier Ally")).hasSize(2);
        for (int i = 0; i < 2; i++) {
            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(freeblade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opposingFreeblade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
