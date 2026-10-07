package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({TitanForge.class})
class TitanForgeTest extends BaseCardTest {

    @Test
    @DisplayName("Activating first ability adds a charge counter")
    void activatingFirstAbilityAddsChargeCounter() {
        harness.addToBattlefield(player1, new TitanForge());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Permanent forge = findPermanent(player1, "Titan Forge");
        int forgeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(forge);

        harness.activateAbility(player1, forgeIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(forge.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("First ability taps the artifact")
    void firstAbilityTapsArtifact() {
        harness.addToBattlefield(player1, new TitanForge());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Permanent forge = findPermanent(player1, "Titan Forge");
        int forgeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(forge);

        harness.activateAbility(player1, forgeIndex, 0, null, null);

        assertThat(forge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate first ability without enough mana")
    void cannotActivateFirstAbilityWithoutMana() {
        harness.addToBattlefield(player1, new TitanForge());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Permanent forge = findPermanent(player1, "Titan Forge");
        int forgeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(forge);

        assertThatThrownBy(() -> harness.activateAbility(player1, forgeIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multiple activations accumulate charge counters")
    void multipleActivationsAccumulateCounters() {
        harness.addToBattlefield(player1, new TitanForge());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        Permanent forge = findPermanent(player1, "Titan Forge");
        int forgeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(forge);

        // First activation
        harness.activateAbility(player1, forgeIndex, 0, null, null);
        harness.passBothPriorities();

        // Untap for second activation
        forge.untap();
        harness.activateAbility(player1, forgeIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(forge.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }


    @Test
    @DisplayName("Activating second ability with 3 charge counters creates a 9/9 Golem token")
    void activateSecondAbilityCreatesGolemToken() {
        harness.addToBattlefield(player1, new TitanForge());

        Permanent forge = findPermanent(player1, "Titan Forge");
        forge.setCounterCount(CounterType.CHARGE, 3);
        int forgeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(forge);

        harness.activateAbility(player1, forgeIndex, 1, null, null);
        harness.passBothPriorities();

        // Charge counters are removed
        assertThat(forge.getCounterCount(CounterType.CHARGE)).isEqualTo(0);

        // 9/9 Golem artifact creature token is on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Golem")
                        && p.getCard().getPower() == 9
                        && p.getCard().getToughness() == 9
                        && p.getCard().hasType(CardType.ARTIFACT));
    }

    @Test
    @DisplayName("Cannot activate second ability with fewer than 3 charge counters")
    void cannotActivateSecondAbilityWithFewerThanThreeCounters() {
        harness.addToBattlefield(player1, new TitanForge());

        Permanent forge = findPermanent(player1, "Titan Forge");
        forge.setCounterCount(CounterType.CHARGE, 2);
        int forgeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(forge);

        assertThatThrownBy(() -> harness.activateAbility(player1, forgeIndex, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activating second ability with more than 3 charge counters only removes 3")
    void activateRemovesExactlyThreeCounters() {
        harness.addToBattlefield(player1, new TitanForge());

        Permanent forge = findPermanent(player1, "Titan Forge");
        forge.setCounterCount(CounterType.CHARGE, 5);
        int forgeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(forge);

        harness.activateAbility(player1, forgeIndex, 1, null, null);
        harness.passBothPriorities();

        assertThat(forge.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Second ability taps the artifact")
    void secondAbilityTapsArtifact() {
        harness.addToBattlefield(player1, new TitanForge());

        Permanent forge = findPermanent(player1, "Titan Forge");
        forge.setCounterCount(CounterType.CHARGE, 3);
        int forgeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(forge);

        harness.activateAbility(player1, forgeIndex, 1, null, null);

        assertThat(forge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate either ability when tapped")
    void cannotActivateWhenTapped() {
        harness.addToBattlefield(player1, new TitanForge());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Permanent forge = findPermanent(player1, "Titan Forge");
        forge.setCounterCount(CounterType.CHARGE, 3);
        forge.tap();
        int forgeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(forge);

        // First ability cannot be activated when tapped
        assertThatThrownBy(() -> harness.activateAbility(player1, forgeIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        // Second ability cannot be activated when tapped
        assertThatThrownBy(() -> harness.activateAbility(player1, forgeIndex, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    @DisplayName("Charging pays its costs immediately and adds the counter only on resolution")
    void chargingUsesTheStack() {
        harness.addToBattlefield(player1, new TitanForge());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        Permanent forge = findPermanent(player1, "Titan Forge");

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(forge.isTapped()).isTrue();
        assertThat(forge.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(forge.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Token ability pays counters immediately and creates exactly one colorless Golem on resolution")
    void tokenAbilityPaysCountersBeforeResolution() {
        harness.addToBattlefield(player1, new TitanForge());
        Permanent forge = findPermanent(player1, "Titan Forge");
        forge.setCounterCount(CounterType.CHARGE, 4);
        forge.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(forge.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(forge.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(countPermanents(player1, "Golem")).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Golem")).isEqualTo(1);
        assertThat(countPermanents(player2, "Golem")).isZero();
        Permanent token = findPermanent(player1, "Golem");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.GOLEM);
        assertThat(token.getCard().getColors()).isEmpty();
        assertThat(token.getCard().getPower()).isEqualTo(9);
        assertThat(token.getCard().getToughness()).isEqualTo(9);
        assertThat(token.isTapped()).isFalse();
    }

}
