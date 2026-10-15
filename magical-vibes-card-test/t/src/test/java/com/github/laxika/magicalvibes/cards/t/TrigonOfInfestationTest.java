package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardColor;
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
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({TrigonOfInfestation.class, Shatter.class})
class TrigonOfInfestationTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with 3 charge counters")
    void entersWithThreeChargeCounters() {
        harness.castFromHand(player1, new TrigonOfInfestation(), "{4}");
        harness.passBothPriorities();

        Permanent trigon = findPermanent(player1, "Trigon of Infestation");
        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }


    @Test
    @DisplayName("Activating first ability adds a charge counter")
    void activateFirstAbilityAddsCounter() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfInfestation());
        trigon.setCounterCount(CounterType.CHARGE, 3);

        harness.addMana(player1, ManaColor.GREEN, 2);
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        harness.activateAbility(player1, trigonIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
    }

    @Test
    @DisplayName("First ability requires green mana")
    void firstAbilityRequiresGreenMana() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfInfestation());
        trigon.setCounterCount(CounterType.CHARGE, 3);

        // Only colorless mana, should fail
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        assertThatThrownBy(() -> harness.activateAbility(player1, trigonIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    @DisplayName("Activating second ability creates a 1/1 green Phyrexian Insect token with infect")
    void activateSecondAbilityCreatesToken() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfInfestation());
        trigon.setCounterCount(CounterType.CHARGE, 3);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        harness.activateAbility(player1, trigonIndex, 1, null, null);
        harness.passBothPriorities();

        // Charge counter removed
        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(2);

        // 1/1 Phyrexian Insect token on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Phyrexian Insect")
                        && p.getCard().getPower() == 1
                        && p.getCard().getToughness() == 1
                        && p.getCard().getColor() == CardColor.GREEN
                        && p.getCard().getSubtypes().containsAll(List.of(CardSubtype.PHYREXIAN, CardSubtype.INSECT))
                        && p.getCard().getKeywords().contains(Keyword.INFECT));
    }

    @Test
    @DisplayName("Cannot activate second ability with 0 charge counters")
    void cannotActivateTokenAbilityWithNoCounters() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfInfestation());
        trigon.setCounterCount(CounterType.CHARGE, 0);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        assertThatThrownBy(() -> harness.activateAbility(player1, trigonIndex, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can create multiple tokens by activating multiple times (untapping between uses)")
    void canCreateMultipleTokens() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfInfestation());
        trigon.setCounterCount(CounterType.CHARGE, 3);

        // First activation
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        harness.activateAbility(player1, trigonIndex, 1, null, null);
        harness.passBothPriorities();
        trigon.untap();

        // Second activation
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, trigonIndex, 1, null, null);
        harness.passBothPriorities();

        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(1);

        long tokenCount = countPermanents(player1, "Phyrexian Insect");
        assertThat(tokenCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate second ability while tapped")
    void cannotActivateWhileTapped() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfInfestation());
        trigon.setCounterCount(CounterType.CHARGE, 3);

        // First activation taps it
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        harness.activateAbility(player1, trigonIndex, 1, null, null);
        harness.passBothPriorities();

        // Cannot activate again while tapped
        assertThat(trigon.isTapped()).isTrue();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, trigonIndex, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Recharging taps immediately and adds the counter only on resolution")
    void rechargeUsesStackAndTapCost() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfInfestation());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(trigon.isTapped()).isTrue();
        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Token cost is paid before resolution and the last counter can be spent")
    void lastCounterIsPaidImmediately() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfInfestation());
        trigon.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(trigon.isTapped()).isTrue();
        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(countPermanents(player1, "Phyrexian Insect")).isZero();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Phyrexian Insect")).isEqualTo(1);
    }

    @Test
    @DisplayName("Token ability requires two mana even when a charge counter is available")
    void tokenAbilityRequiresTwoMana() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfInfestation());
        trigon.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(trigon.isTapped()).isFalse();
        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Phyrexian Insect")).isZero();
    }

    @Test
    @DisplayName("Token ability resolves after the Trigon is destroyed")
    void tokenAbilitySurvivesSourceDestruction() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfInfestation());
        trigon.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, null);

        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, trigon.getId());
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Trigon of Infestation")).isZero();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Phyrexian Insect")).isEqualTo(1);
        assertThat(countPermanents(player2, "Phyrexian Insect")).isZero();
    }
}
