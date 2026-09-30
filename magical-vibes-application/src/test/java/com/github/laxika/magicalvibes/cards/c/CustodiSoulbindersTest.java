package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CustodiSoulbinders.class, GrizzlyBears.class})
class CustodiSoulbindersTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter for each other creature on the battlefield")
    void entersWithCountersForOtherCreaturesOnBattlefield() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CustodiSoulbinders()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent soulbinders = findPermanent(player1, "Custodi Soulbinders");
        assertThat(soulbinders.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removing a +1/+1 counter creates a white Spirit token with flying")
    void removesCounterAndCreatesSpirit() {
        Permanent soulbinders = harness.addToBattlefieldAndReturn(player1, new CustodiSoulbinders());
        soulbinders.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(soulbinders.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        Permanent spirit = findPermanents(player1, "Spirit").getFirst();
        assertThat(spirit.getCard().isToken()).isTrue();
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(gqs.hasKeyword(gd, spirit, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The token-making ability cannot be activated without a +1/+1 counter")
    void cannotActivateWithoutCounter() {
        harness.addToBattlefield(player1, new CustodiSoulbinders());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
