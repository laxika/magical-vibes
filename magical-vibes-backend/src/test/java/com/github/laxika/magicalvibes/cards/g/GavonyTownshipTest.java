package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnEachOwnCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GavonyTownshipTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has two activated abilities")
    void hasTwoActivatedAbilities() {
        GavonyTownship card = new GavonyTownship();

        assertThat(card.getActivatedAbilities()).hasSize(2);
    }

    @Test
    @DisplayName("First ability is a mana ability producing colorless")
    void firstAbilityIsColorlessMana() {
        GavonyTownship card = new GavonyTownship();
        var ability = card.getActivatedAbilities().get(0);

        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(AwardManaEffect.class);
    }

    @Test
    @DisplayName("Second ability puts +1/+1 counters on each creature you control")
    void secondAbilityPutsCounters() {
        GavonyTownship card = new GavonyTownship();
        var ability = card.getActivatedAbilities().get(1);

        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{2}{G}{W}");
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(PutPlusOnePlusOneCounterOnEachOwnCreatureEffect.class);
    }

    // ===== Mana ability =====

    @Test
    @DisplayName("Tapping for mana adds colorless mana")
    void tapForColorlessMana() {
        harness.addToBattlefield(player1, new GavonyTownship());

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isGreaterThanOrEqualTo(1);
    }

    // ===== Counter ability =====

    @Test
    @DisplayName("Counter ability puts +1/+1 on each creature you control")
    void counterAbilityPutsCountersOnOwnCreatures() {
        harness.addToBattlefield(player1, new GavonyTownship());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Permanent> bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .toList();

        assertThat(bears).hasSize(2);
        for (Permanent bear : bears) {
            assertThat(bear.getPlusOnePlusOneCounters()).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("Counter ability does not affect opponent's creatures")
    void counterAbilityDoesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player1, new GavonyTownship());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent opponentBear = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(opponentBear.getPlusOnePlusOneCounters()).isEqualTo(0);
    }

    @Test
    @DisplayName("Counter ability stacks on multiple activations")
    void counterAbilityStacksOnMultipleActivations() {
        harness.addToBattlefield(player1, new GavonyTownship());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // First activation
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Add more mana for second activation (next turn)
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Untap the land for second activation
        Permanent township = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Gavony Township"))
                .findFirst().orElseThrow();
        township.untap();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(bear.getPlusOnePlusOneCounters()).isEqualTo(2);
        assertThat(bear.getEffectivePower()).isEqualTo(4);   // 2 base + 2 counters
        assertThat(bear.getEffectiveToughness()).isEqualTo(4); // 2 base + 2 counters
    }
}
