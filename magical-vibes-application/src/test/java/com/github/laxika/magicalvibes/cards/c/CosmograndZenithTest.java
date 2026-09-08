package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CosmograndZenith.class, GrizzlyBears.class, Shock.class})
class CosmograndZenithTest extends BaseCardTest {

    private static final String TOKEN_MODE = "Create two 1/1 white Human Soldier creature tokens.";
    private static final String COUNTER_MODE = "Put a +1/+1 counter on each creature you control.";

    @Test
    @DisplayName("The second spell can create two Human Soldier tokens")
    void createsHumanSoldierTokens() {
        harness.addToBattlefield(player1, new CosmograndZenith());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        castTwoSpellsAndChoose(TOKEN_MODE);

        List<Permanent> tokens = findPermanents(player1, "Human Soldier");
        assertThat(tokens).hasSize(2);
        for (Permanent token : tokens) {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getSubtypes())
                    .containsExactly(CardSubtype.HUMAN, CardSubtype.SOLDIER);
        }
    }

    @Test
    @DisplayName("The second spell can put a +1/+1 counter on each creature you control")
    void putsCountersOnControlledCreatures() {
        Permanent zenith = harness.addToBattlefieldAndReturn(player1, new CosmograndZenith());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        castTwoSpellsAndChoose(COUNTER_MODE);

        assertThat(zenith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castTwoSpellsAndChoose(String mode) {
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
