package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DarksteelAxe;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.DestroyNonlandPermanentsWithManaValueEqualToChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RatchetBombTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Ratchet Bomb has two activated abilities with correct structure")
    void hasCorrectAbilityStructure() {
        RatchetBomb card = new RatchetBomb();

        assertThat(card.getActivatedAbilities()).hasSize(2);

        // Ability 0: {T}: Put a charge counter on Ratchet Bomb.
        var ability0 = card.getActivatedAbilities().get(0);
        assertThat(ability0.isRequiresTap()).isTrue();
        assertThat(ability0.getManaCost()).isNull();
        assertThat(ability0.getEffects()).hasSize(1);
        assertThat(ability0.getEffects().getFirst()).isInstanceOf(PutChargeCounterOnSelfEffect.class);

        // Ability 1: {T}, Sacrifice Ratchet Bomb: Destroy each nonland permanent with mana value
        // equal to the number of charge counters on Ratchet Bomb.
        var ability1 = card.getActivatedAbilities().get(1);
        assertThat(ability1.isRequiresTap()).isTrue();
        assertThat(ability1.getManaCost()).isNull();
        assertThat(ability1.getEffects()).hasSize(2);
        assertThat(ability1.getEffects().get(0)).isInstanceOf(SacrificeSelfCost.class);
        assertThat(ability1.getEffects().get(1)).isInstanceOf(DestroyNonlandPermanentsWithManaValueEqualToChargeCountersEffect.class);
    }

    // ===== Ability 0: Put a charge counter =====

    @Test
    @DisplayName("Tapping Ratchet Bomb puts a charge counter on it")
    void tappingAddsChargeCounter() {
        Permanent bomb = addReadyBomb(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bomb.getChargeCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple activations accumulate charge counters")
    void multipleActivationsAccumulateCounters() {
        Permanent bomb = addReadyBomb(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(bomb.getChargeCounters()).isEqualTo(1);

        bomb.untap();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(bomb.getChargeCounters()).isEqualTo(2);

        bomb.untap();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(bomb.getChargeCounters()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate ability 0 when already tapped")
    void cannotActivateAbility0WhenTapped() {
        Permanent bomb = addReadyBomb(player1);
        bomb.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Ability 1: Sacrifice and destroy by mana value =====

    @Test
    @DisplayName("Sacrificing with 2 counters destroys MV 2 creatures")
    void destroysManaValue2Permanents() {
        Permanent bomb = addReadyBomb(player1);
        bomb.setChargeCounters(2);

        // MV 2 creature on opponent's battlefield
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Ratchet Bomb should be in the graveyard (sacrificed as cost)
        harness.assertInGraveyard(player1, "Ratchet Bomb");
        harness.assertNotOnBattlefield(player1, "Ratchet Bomb");

        // Grizzly Bears (MV 2) should be destroyed
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificing with 0 counters destroys MV 0 nonland permanents (tokens, Memnite)")
    void destroysManaValue0Permanents() {
        Permanent bomb = addReadyBomb(player1);
        // 0 charge counters — targets MV 0

        // MV 0 artifact creature
        harness.addToBattlefield(player2, new Memnite());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Memnite (MV 0) should be destroyed
        harness.assertNotOnBattlefield(player2, "Memnite");
        harness.assertInGraveyard(player2, "Memnite");
    }

    @Test
    @DisplayName("Does not destroy permanents with different mana value")
    void doesNotDestroyDifferentManaValue() {
        Permanent bomb = addReadyBomb(player1);
        bomb.setChargeCounters(2);

        // MV 1 creature (Llanowar Elves = {G})
        harness.addToBattlefield(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Llanowar Elves (MV 1) should survive
        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Does not destroy lands even if mana value matches")
    void doesNotDestroyLands() {
        Permanent bomb = addReadyBomb(player1);
        // 0 charge counters — lands have MV 0 but should be excluded

        harness.addToBattlefield(player2, new Forest());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Forest should survive (lands are excluded)
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Destroys permanents on both sides of the battlefield")
    void destroysPermanentsOnBothSides() {
        Permanent bomb = addReadyBomb(player1);
        bomb.setChargeCounters(2);

        // MV 2 creatures on both sides
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Both should be destroyed
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroys enchantments with matching mana value")
    void destroysEnchantments() {
        Permanent bomb = addReadyBomb(player1);
        bomb.setChargeCounters(2);

        // Pacifism is an enchantment with MV 2 ({1}{W})
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        Pacifism pacifism = new Pacifism();
        Permanent pacifismPerm = new Permanent(pacifism);
        pacifismPerm.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player2.getId()).add(pacifismPerm);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Both Pacifism (MV 2) and Grizzly Bears (MV 2) should be destroyed
        harness.assertNotOnBattlefield(player2, "Pacifism");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not destroy indestructible permanents")
    void doesNotDestroyIndestructible() {
        Permanent bomb = addReadyBomb(player1);
        bomb.setChargeCounters(1);

        // Darksteel Axe (MV 1, indestructible)
        harness.addToBattlefield(player2, new DarksteelAxe());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Darksteel Axe should survive (indestructible)
        harness.assertOnBattlefield(player2, "Darksteel Axe");
    }

    @Test
    @DisplayName("Ratchet Bomb is sacrificed as a cost (goes to graveyard immediately)")
    void sacrificedAsCost() {
        Permanent bomb = addReadyBomb(player1);
        bomb.setChargeCounters(2);

        harness.activateAbility(player1, 0, 1, null, null);

        // Should be in graveyard immediately (sacrifice is a cost)
        harness.assertNotOnBattlefield(player1, "Ratchet Bomb");
        harness.assertInGraveyard(player1, "Ratchet Bomb");
    }

    @Test
    @DisplayName("Cannot use both abilities in same turn (both require tap)")
    void cannotUseBothAbilitiesInSameTurn() {
        Permanent bomb = addReadyBomb(player1);

        // Use ability 0 first (tap to add counter)
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Bomb is now tapped, cannot use ability 1
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Charge counter add then sacrifice on separate turns works correctly")
    void addCounterThenSacrificeOnSeparateTurns() {
        Permanent bomb = addReadyBomb(player1);

        // Turn 1: add a counter
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(bomb.getChargeCounters()).isEqualTo(1);

        // Simulate next turn: untap
        bomb.untap();

        // Add another counter
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(bomb.getChargeCounters()).isEqualTo(2);

        // Simulate next turn: untap
        bomb.untap();

        // Put a MV 2 creature on opponent's side
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Sacrifice to destroy MV 2 permanents
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Ratchet Bomb");
    }

    // ===== Helper methods =====

    private Permanent addReadyBomb(Player player) {
        RatchetBomb card = new RatchetBomb();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
