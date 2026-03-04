package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TitanForgeTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has two activated abilities")
    void hasTwoActivatedAbilities() {
        TitanForge card = new TitanForge();

        assertThat(card.getActivatedAbilities()).hasSize(2);
    }

    @Test
    @DisplayName("First ability puts a charge counter on self")
    void firstAbilityPutsChargeCounter() {
        TitanForge card = new TitanForge();

        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{3}");
        assertThat(card.getActivatedAbilities().get(0).getEffects())
                .anyMatch(e -> e instanceof PutChargeCounterOnSelfEffect);
    }

    @Test
    @DisplayName("Second ability removes 3 charge counters and creates a 9/9 Golem token")
    void secondAbilityCreatesToken() {
        TitanForge card = new TitanForge();

        assertThat(card.getActivatedAbilities().get(1).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(1).getManaCost()).isNull();
        assertThat(card.getActivatedAbilities().get(1).getEffects())
                .anyMatch(e -> e instanceof RemoveChargeCountersFromSourceCost rc && rc.count() == 3)
                .anyMatch(e -> e instanceof CreateCreatureTokenEffect);
    }

    // ===== First ability — charge counter =====

    @Test
    @DisplayName("Activating first ability adds a charge counter")
    void activatingFirstAbilityAddsChargeCounter() {
        harness.addToBattlefield(player1, new TitanForge());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Permanent forge = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Titan Forge"))
                .findFirst().orElseThrow();
        int forgeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(forge);

        harness.activateAbility(player1, forgeIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(forge.getChargeCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("First ability taps the artifact")
    void firstAbilityTapsArtifact() {
        harness.addToBattlefield(player1, new TitanForge());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Permanent forge = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Titan Forge"))
                .findFirst().orElseThrow();
        int forgeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(forge);

        harness.activateAbility(player1, forgeIndex, 0, null, null);

        assertThat(forge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate first ability without enough mana")
    void cannotActivateFirstAbilityWithoutMana() {
        harness.addToBattlefield(player1, new TitanForge());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Permanent forge = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Titan Forge"))
                .findFirst().orElseThrow();
        int forgeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(forge);

        assertThatThrownBy(() -> harness.activateAbility(player1, forgeIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multiple activations accumulate charge counters")
    void multipleActivationsAccumulateCounters() {
        harness.addToBattlefield(player1, new TitanForge());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        Permanent forge = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Titan Forge"))
                .findFirst().orElseThrow();
        int forgeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(forge);

        // First activation
        harness.activateAbility(player1, forgeIndex, 0, null, null);
        harness.passBothPriorities();

        // Untap for second activation
        forge.setTapped(false);
        harness.activateAbility(player1, forgeIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(forge.getChargeCounters()).isEqualTo(2);
    }

    // ===== Second ability — token creation =====

    @Test
    @DisplayName("Activating second ability with 3 charge counters creates a 9/9 Golem token")
    void activateSecondAbilityCreatesGolemToken() {
        harness.addToBattlefield(player1, new TitanForge());

        Permanent forge = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Titan Forge"))
                .findFirst().orElseThrow();
        forge.setChargeCounters(3);
        int forgeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(forge);

        harness.activateAbility(player1, forgeIndex, 1, null, null);
        harness.passBothPriorities();

        // Charge counters are removed
        assertThat(forge.getChargeCounters()).isEqualTo(0);

        // 9/9 Golem artifact creature token is on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Golem")
                        && p.getCard().getPower() == 9
                        && p.getCard().getToughness() == 9
                        && p.getCard().getAdditionalTypes().contains(CardType.ARTIFACT));
    }

    @Test
    @DisplayName("Cannot activate second ability with fewer than 3 charge counters")
    void cannotActivateSecondAbilityWithFewerThanThreeCounters() {
        harness.addToBattlefield(player1, new TitanForge());

        Permanent forge = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Titan Forge"))
                .findFirst().orElseThrow();
        forge.setChargeCounters(2);
        int forgeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(forge);

        assertThatThrownBy(() -> harness.activateAbility(player1, forgeIndex, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activating second ability with more than 3 charge counters only removes 3")
    void activateRemovesExactlyThreeCounters() {
        harness.addToBattlefield(player1, new TitanForge());

        Permanent forge = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Titan Forge"))
                .findFirst().orElseThrow();
        forge.setChargeCounters(5);
        int forgeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(forge);

        harness.activateAbility(player1, forgeIndex, 1, null, null);
        harness.passBothPriorities();

        assertThat(forge.getChargeCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Second ability taps the artifact")
    void secondAbilityTapsArtifact() {
        harness.addToBattlefield(player1, new TitanForge());

        Permanent forge = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Titan Forge"))
                .findFirst().orElseThrow();
        forge.setChargeCounters(3);
        int forgeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(forge);

        harness.activateAbility(player1, forgeIndex, 1, null, null);

        assertThat(forge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate either ability when tapped")
    void cannotActivateWhenTapped() {
        harness.addToBattlefield(player1, new TitanForge());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Permanent forge = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Titan Forge"))
                .findFirst().orElseThrow();
        forge.setChargeCounters(3);
        forge.setTapped(true);
        int forgeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(forge);

        // First ability cannot be activated when tapped
        assertThatThrownBy(() -> harness.activateAbility(player1, forgeIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        // Second ability cannot be activated when tapped
        assertThatThrownBy(() -> harness.activateAbility(player1, forgeIndex, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
