package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TowerOfCalamitiesTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has tap ability that costs {8} and deals 12 damage to target creature")
    void hasCorrectAbility() {
        TowerOfCalamities card = new TowerOfCalamities();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{8}");
        assertThat(card.getActivatedAbilities().getFirst().getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().getFirst())
                .isInstanceOf(DealDamageToTargetCreatureEffect.class);

        DealDamageToTargetCreatureEffect effect =
                (DealDamageToTargetCreatureEffect) card.getActivatedAbilities().getFirst().getEffects().getFirst();
        assertThat(effect.damage()).isEqualTo(12);
    }

    // ===== Ability resolves correctly =====

    @Test
    @DisplayName("Deals 12 damage to target creature when ability resolves")
    void deals12DamageToTargetCreature() {
        harness.addToBattlefield(player1, new TowerOfCalamities());
        harness.addToBattlefield(player2, new AirElemental());
        harness.forceActivePlayer(player1);
        clearSummoningSickness(player1, "Tower of Calamities");

        harness.addMana(player1, ManaColor.COLORLESS, 8);
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        // Air Elemental is 4/4, takes 12 damage → dies
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Deals 12 damage but creature survives if toughness is high enough")
    void creatureSurvivesWithHighToughness() {
        harness.addToBattlefield(player1, new TowerOfCalamities());
        harness.addToBattlefield(player2, new AirElemental());
        harness.forceActivePlayer(player1);
        clearSummoningSickness(player1, "Tower of Calamities");

        // Boost Air Elemental toughness with +1/+1 counters so it survives 12 damage (4/4 + 9 = 4/13)
        Permanent airElemental = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Air Elemental"))
                .findFirst().orElseThrow();
        airElemental.setPlusOnePlusOneCounters(9);

        harness.addMana(player1, ManaColor.COLORLESS, 8);
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        // Air Elemental is 13/13 with counters, takes 12 damage → survives with 1 toughness
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    // ===== Cost enforcement =====

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new TowerOfCalamities());
        harness.addToBattlefield(player2, new AirElemental());
        harness.forceActivePlayer(player1);
        clearSummoningSickness(player1, "Tower of Calamities");

        harness.addMana(player1, ManaColor.COLORLESS, 7); // 1 short
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Taps when ability is activated")
    void tapsOnActivation() {
        harness.addToBattlefield(player1, new TowerOfCalamities());
        harness.addToBattlefield(player2, new AirElemental());
        harness.forceActivePlayer(player1);
        clearSummoningSickness(player1, "Tower of Calamities");

        harness.addMana(player1, ManaColor.COLORLESS, 8);
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        harness.activateAbility(player1, 0, null, targetId);

        Permanent tower = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Tower of Calamities"))
                .findFirst().orElseThrow();
        assertThat(tower.isTapped()).isTrue();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles when target creature is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player1, new TowerOfCalamities());
        harness.addToBattlefield(player2, new AirElemental());
        harness.forceActivePlayer(player1);
        clearSummoningSickness(player1, "Tower of Calamities");

        harness.addMana(player1, ManaColor.COLORLESS, 8);
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        harness.activateAbility(player1, 0, null, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Helpers =====

    private void clearSummoningSickness(com.github.laxika.magicalvibes.model.Player player, String cardName) {
        Permanent perm = gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst().orElseThrow();
        perm.setSummoningSick(false);
    }
}
