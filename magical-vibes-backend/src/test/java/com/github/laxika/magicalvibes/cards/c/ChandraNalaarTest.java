package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetAndTheirCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChandraNalaarTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has three loyalty abilities")
    void hasThreeLoyaltyAbilities() {
        ChandraNalaar card = new ChandraNalaar();
        assertThat(card.getActivatedAbilities()).hasSize(3);
    }

    @Test
    @DisplayName("+1 ability has DealDamageToAnyTargetEffect(1)")
    void plusOneAbilityHasCorrectEffect() {
        ChandraNalaar card = new ChandraNalaar();
        var ability = card.getActivatedAbilities().get(0);

        assertThat(ability.getLoyaltyCost()).isEqualTo(1);
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(DealDamageToAnyTargetEffect.class);
        assertThat(((DealDamageToAnyTargetEffect) ability.getEffects().getFirst()).damage()).isEqualTo(1);
    }

    @Test
    @DisplayName("-X ability has DealXDamageToTargetCreatureEffect and is variable loyalty cost")
    void minusXAbilityHasCorrectEffect() {
        ChandraNalaar card = new ChandraNalaar();
        var ability = card.getActivatedAbilities().get(1);

        assertThat(ability.isVariableLoyaltyCost()).isTrue();
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(DealXDamageToTargetCreatureEffect.class);
    }

    @Test
    @DisplayName("-8 ability has DealDamageToTargetAndTheirCreaturesEffect(10)")
    void minusEightAbilityHasCorrectEffect() {
        ChandraNalaar card = new ChandraNalaar();
        var ability = card.getActivatedAbilities().get(2);

        assertThat(ability.getLoyaltyCost()).isEqualTo(-8);
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(DealDamageToTargetAndTheirCreaturesEffect.class);
        assertThat(((DealDamageToTargetAndTheirCreaturesEffect) ability.getEffects().getFirst()).damage()).isEqualTo(10);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts planeswalker spell on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new ChandraNalaar()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castPlaneswalker(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.PLANESWALKER_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Chandra Nalaar");
    }

    @Test
    @DisplayName("Resolving puts planeswalker on battlefield with initial loyalty 6")
    void resolvingEntersBattlefieldWithLoyalty() {
        harness.setHand(player1, List.of(new ChandraNalaar()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf).anyMatch(p -> p.getCard().getName().equals("Chandra Nalaar"));
        Permanent chandra = bf.stream().filter(p -> p.getCard().getName().equals("Chandra Nalaar")).findFirst().orElseThrow();
        assertThat(chandra.getLoyaltyCounters()).isEqualTo(6);
        assertThat(chandra.isSummoningSick()).isFalse();
    }

    // ===== +1 ability: 1 damage to target player or planeswalker =====

    @Test
    @DisplayName("+1 ability deals 1 damage to target player and increases loyalty")
    void plusOneDeals1DamageToPlayer() {
        Permanent chandra = addReadyChandra(player1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(chandra.getLoyaltyCounters()).isEqualTo(7); // 6 + 1
        int lifeAfter = gd.playerLifeTotals.get(player2.getId());
        assertThat(lifeAfter).isEqualTo(lifeBefore - 1);
    }

    // ===== -X ability: X damage to target creature =====

    @Test
    @DisplayName("-X ability deals X damage to target creature and removes X loyalty")
    void minusXDealsXDamageToCreature() {
        Permanent chandra = addReadyChandra(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bear = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // Use X=3 to deal 3 damage (kills 2/2 bear)
        harness.activateAbility(player1, 0, 1, 3, bear.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(chandra.getLoyaltyCounters()).isEqualTo(3); // 6 - 3
        // Bear should be dead (3 damage to a 2/2)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("-X ability with X=0 deals 0 damage")
    void minusXWithZeroDealsZeroDamage() {
        Permanent chandra = addReadyChandra(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bear = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        harness.activateAbility(player1, 0, 1, 0, bear.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(chandra.getLoyaltyCounters()).isEqualTo(6); // 6 - 0
        // Bear should still be alive (0 damage)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("-X ability cannot use more loyalty than available")
    void minusXCannotExceedLoyalty() {
        Permanent chandra = addReadyChandra(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bear = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // X=7 but Chandra only has 6 loyalty
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 7, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    // ===== -8 ability: 10 damage to player/PW and their creatures =====

    @Test
    @DisplayName("-8 ability deals 10 damage to target player and their creatures")
    void minusEightDeals10DamageToPlayerAndCreatures() {
        Permanent chandra = addReadyChandra(player1);
        chandra.setLoyaltyCounters(8);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Chandra should have 0 loyalty (8 - 8) and be in graveyard
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Chandra Nalaar"));

        // Player 2 takes 10 damage
        int lifeAfter = gd.playerLifeTotals.get(player2.getId());
        assertThat(lifeAfter).isEqualTo(10); // 20 - 10

        // Both bears should be dead (10 damage to 2/2)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears"))
                .count()).isEqualTo(2);
    }

    @Test
    @DisplayName("-8 ability does not damage controller's own creatures")
    void minusEightDoesNotDamageOwnCreatures() {
        Permanent chandra = addReadyChandra(player1);
        chandra.setLoyaltyCounters(8);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Player 1's bear should be unharmed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot use -8 when loyalty is only 6")
    void cannotActivateMinusEightWithInsufficientLoyalty() {
        Permanent chandra = addReadyChandra(player1);
        assertThat(chandra.getLoyaltyCounters()).isEqualTo(6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    // ===== Loyalty ability restrictions =====

    @Test
    @DisplayName("Cannot activate loyalty ability during opponent's turn")
    void cannotActivateOnOpponentsTurn() {
        addReadyChandra(player1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your turn");
    }

    @Test
    @DisplayName("Cannot activate two loyalty abilities on same planeswalker in one turn")
    void cannotActivateTwicePerTurn() {
        addReadyChandra(player1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one loyalty ability");
    }

    // ===== Helpers =====

    private Permanent addReadyChandra(Player player) {
        ChandraNalaar card = new ChandraNalaar();
        Permanent perm = new Permanent(card);
        perm.setLoyaltyCounters(6);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
