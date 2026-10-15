package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CustodiSoulbinders.class, GrizzlyBears.class, SolRing.class})
class CustodiSoulbindersTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter for each other creature on the battlefield")
    void entersWithCountersForOtherCreaturesOnBattlefield() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castFromHand(player1, new CustodiSoulbinders(), "{3}{W}");
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

    @Test
    @DisplayName("Enters with no counters and dies when only noncreature permanents are present")
    void diesWithoutOtherCreatures() {
        harness.addToBattlefield(player1, new SolRing());
        harness.addToBattlefield(player2, new SolRing());

        harness.castFromHand(player1, new CustodiSoulbinders(), "{3}{W}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Custodi Soulbinders")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof CustodiSoulbinders);
    }

    @Test
    @DisplayName("The last counter is paid immediately and the Spirit resolves after Soulbinders dies")
    void lastCounterPaymentDoesNotPreventTokenCreation() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new CustodiSoulbinders(), "{3}{W}");
        harness.passBothPriorities();
        Permanent soulbinders = findPermanent(player1, "Custodi Soulbinders");
        assertThat(soulbinders.isSummoningSick()).isTrue();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(soulbinders.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(findPermanents(player1, "Custodi Soulbinders")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof CustodiSoulbinders);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("A tapped Soulbinders can activate repeatedly while it has counters and mana")
    void tappedSourceCanActivateRepeatedly() {
        Permanent soulbinders = harness.addToBattlefieldAndReturn(player1, new CustodiSoulbinders());
        soulbinders.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        soulbinders.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(soulbinders.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(soulbinders.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
    }

    @Test
    @DisplayName("Without white mana the ability cannot be paid and no counter is removed")
    void cannotPayWithOnlyColorlessMana() {
        Permanent soulbinders = harness.addToBattlefieldAndReturn(player1, new CustodiSoulbinders());
        soulbinders.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(soulbinders.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
