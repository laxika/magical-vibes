package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GideonOfTheTrials;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OathOfGideon.class, GideonOfTheTrials.class})
class OathOfGideonTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, creates two 1/1 Kor Ally tokens")
    void createsTwoKorAllyTokens() {
        harness.setHand(player1, List.of(new OathOfGideon()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
    }

    @Test
    @DisplayName("Planeswalkers you control enter with an additional loyalty counter")
    void planeswalkersEnterWithAdditionalLoyaltyCounter() {
        harness.addToBattlefield(player1, new OathOfGideon());
        harness.setHand(player1, List.of(new GideonOfTheTrials()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        Permanent gideon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GideonOfTheTrials)
                .findFirst()
                .orElseThrow();
        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void opponentsPlaneswalkerDoesNotReceiveAdditionalLoyalty() {
        harness.addToBattlefield(player1, new OathOfGideon());

        Permanent gideon = harness.enterBattlefieldAndReturn(player2, new GideonOfTheTrials());

        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void planeswalkerEnteringWithoutBeingCastReceivesAdditionalLoyalty() {
        harness.addToBattlefield(player1, new OathOfGideon());

        Permanent gideon = harness.enterBattlefieldAndReturn(player1, new GideonOfTheTrials());

        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void oathDoesNotChangeLoyaltyOfAnExistingPlaneswalker() {
        Permanent gideon = harness.enterBattlefieldAndReturn(player1, new GideonOfTheTrials());
        harness.setHand(player1, List.of(new OathOfGideon()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void generatedTokensHaveTheSpecifiedCharacteristics() {
        harness.setHand(player1, List.of(new OathOfGideon()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2)
                .allSatisfy(token -> {
                    assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
                    assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(
                            CardSubtype.KOR,
                            CardSubtype.ALLY);
                    assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
                    assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
                });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }
}
