package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AjaniOutlandChaperone;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAllCreaturesAndPlaneswalkersTargetControlsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChandraBoldPyromancerTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has three loyalty abilities")
    void hasThreeLoyaltyAbilities() {
        ChandraBoldPyromancer card = new ChandraBoldPyromancer();
        assertThat(card.getActivatedAbilities()).hasSize(3);
    }

    @Test
    @DisplayName("+1 ability has AwardManaEffect and DealDamageToTargetPlayerEffect")
    void plusOneAbilityHasCorrectEffects() {
        ChandraBoldPyromancer card = new ChandraBoldPyromancer();
        var ability = card.getActivatedAbilities().get(0);

        assertThat(ability.getLoyaltyCost()).isEqualTo(1);
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(AwardManaEffect.class);
        AwardManaEffect manaEffect = (AwardManaEffect) ability.getEffects().get(0);
        assertThat(manaEffect.color()).isEqualTo(ManaColor.RED);
        assertThat(manaEffect.amount()).isEqualTo(2);
        assertThat(ability.getEffects().get(1)).isInstanceOf(DealDamageToTargetPlayerEffect.class);
        assertThat(((DealDamageToTargetPlayerEffect) ability.getEffects().get(1)).damage()).isEqualTo(2);
    }

    @Test
    @DisplayName("-3 ability has DealDamageToTargetCreatureOrPlaneswalkerEffect(3)")
    void minusThreeAbilityHasCorrectEffect() {
        ChandraBoldPyromancer card = new ChandraBoldPyromancer();
        var ability = card.getActivatedAbilities().get(1);

        assertThat(ability.getLoyaltyCost()).isEqualTo(-3);
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(DealDamageToTargetCreatureOrPlaneswalkerEffect.class);
        assertThat(((DealDamageToTargetCreatureOrPlaneswalkerEffect) ability.getEffects().getFirst()).damage()).isEqualTo(3);
    }

    @Test
    @DisplayName("-7 ability has DealDamageToTargetPlayerEffect(10) and DealDamageToAllCreaturesAndPlaneswalkersTargetControlsEffect(10)")
    void minusSevenAbilityHasCorrectEffects() {
        ChandraBoldPyromancer card = new ChandraBoldPyromancer();
        var ability = card.getActivatedAbilities().get(2);

        assertThat(ability.getLoyaltyCost()).isEqualTo(-7);
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(DealDamageToTargetPlayerEffect.class);
        assertThat(((DealDamageToTargetPlayerEffect) ability.getEffects().get(0)).damage()).isEqualTo(10);
        assertThat(ability.getEffects().get(1)).isInstanceOf(DealDamageToAllCreaturesAndPlaneswalkersTargetControlsEffect.class);
        assertThat(((DealDamageToAllCreaturesAndPlaneswalkersTargetControlsEffect) ability.getEffects().get(1)).damage()).isEqualTo(10);
    }

    // ===== +1 ability: Add {R}{R} and deal 2 damage to target player =====

    @Test
    @DisplayName("+1 ability adds {R}{R} and deals 2 damage to target player")
    void plusOneAddsManaAndDealsDamage() {
        Permanent chandra = addReadyChandra(player1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());
        int redManaBefore = harness.getGameData().playerManaPools.get(player1.getId()).get(ManaColor.RED);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(chandra.getLoyaltyCounters()).isEqualTo(6); // 5 + 1
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(redManaBefore + 2);
    }

    // ===== -3 ability: 3 damage to target creature or planeswalker =====

    @Test
    @DisplayName("-3 ability deals 3 damage to target creature")
    void minusThreeDeals3DamageToCreature() {
        Permanent chandra = addReadyChandra(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bear = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        harness.activateAbility(player1, 0, 1, null, bear.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(chandra.getLoyaltyCounters()).isEqualTo(2); // 5 - 3
        // Bear should be dead (3 damage to a 2/2)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("-3 ability deals 3 damage to target planeswalker")
    void minusThreeDeals3DamageToPlaneswalker() {
        Permanent chandra = addReadyChandra(player1);

        // Add an opponent planeswalker with 3 loyalty
        AjaniOutlandChaperone ajaniCard = new AjaniOutlandChaperone();
        Permanent ajani = new Permanent(ajaniCard);
        ajani.setLoyaltyCounters(3);
        ajani.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(ajani);

        harness.activateAbility(player1, 0, 1, null, ajani.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(chandra.getLoyaltyCounters()).isEqualTo(2); // 5 - 3
        // Ajani should be destroyed (3 damage to 3 loyalty planeswalker)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Ajani, Outland Chaperone"));
    }

    // ===== -7 ability: 10 damage to target player and their creatures/planeswalkers =====

    @Test
    @DisplayName("-7 ability deals 10 damage to target player and their creatures and planeswalkers")
    void minusSevenDeals10DamageToAll() {
        Permanent chandra = addReadyChandra(player1);
        chandra.setLoyaltyCounters(7);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Add an opponent planeswalker with 5 loyalty
        AjaniOutlandChaperone ajaniCard = new AjaniOutlandChaperone();
        Permanent ajani = new Permanent(ajaniCard);
        ajani.setLoyaltyCounters(5);
        ajani.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(ajani);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Chandra should have 0 loyalty (7 - 7) and be in graveyard
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Chandra, Bold Pyromancer"));

        // Player 2 takes 10 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10); // 20 - 10

        // Both bears should be dead
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears"))
                .count()).isEqualTo(2);

        // Ajani should be destroyed (10 damage to 5 loyalty)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Ajani, Outland Chaperone"));
    }

    @Test
    @DisplayName("-7 ability does not damage controller's own permanents")
    void minusSevenDoesNotDamageOwnPermanents() {
        Permanent chandra = addReadyChandra(player1);
        chandra.setLoyaltyCounters(7);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Player 1's bear should be unharmed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Loyalty restrictions =====

    @Test
    @DisplayName("Cannot activate -7 when loyalty is only 5")
    void cannotActivateMinusSevenWithInsufficientLoyalty() {
        addReadyChandra(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    // ===== Helpers =====

    private Permanent addReadyChandra(Player player) {
        ChandraBoldPyromancer card = new ChandraBoldPyromancer();
        Permanent perm = new Permanent(card);
        perm.setLoyaltyCounters(5);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
