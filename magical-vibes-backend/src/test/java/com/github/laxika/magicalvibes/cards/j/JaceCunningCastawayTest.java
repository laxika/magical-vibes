package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCombatDamageLootEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JaceCunningCastawayTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has three loyalty abilities")
    void hasThreeLoyaltyAbilities() {
        JaceCunningCastaway card = new JaceCunningCastaway();
        assertThat(card.getActivatedAbilities()).hasSize(3);
    }

    @Test
    @DisplayName("+1 ability has RegisterDelayedCombatDamageLootEffect(1, 1)")
    void plusOneAbilityHasCorrectEffect() {
        JaceCunningCastaway card = new JaceCunningCastaway();
        var ability = card.getActivatedAbilities().get(0);

        assertThat(ability.getLoyaltyCost()).isEqualTo(1);
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(RegisterDelayedCombatDamageLootEffect.class);
        RegisterDelayedCombatDamageLootEffect effect =
                (RegisterDelayedCombatDamageLootEffect) ability.getEffects().getFirst();
        assertThat(effect.drawAmount()).isEqualTo(1);
        assertThat(effect.discardAmount()).isEqualTo(1);
    }

    @Test
    @DisplayName("-2 ability creates 2/2 blue Illusion token with sacrifice-on-target trigger")
    void minusTwoAbilityHasCorrectEffect() {
        JaceCunningCastaway card = new JaceCunningCastaway();
        var ability = card.getActivatedAbilities().get(1);

        assertThat(ability.getLoyaltyCost()).isEqualTo(-2);
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(CreateTokenEffect.class);
        CreateTokenEffect effect = (CreateTokenEffect) ability.getEffects().getFirst();
        assertThat(effect.tokenName()).isEqualTo("Illusion");
        assertThat(effect.power()).isEqualTo(2);
        assertThat(effect.toughness()).isEqualTo(2);
        assertThat(effect.color()).isEqualTo(CardColor.BLUE);
        assertThat(effect.subtypes()).containsExactly(CardSubtype.ILLUSION);
        assertThat(effect.tokenEffects()).containsKey(EffectSlot.ON_BECOMES_TARGET_OF_SPELL);
        assertThat(effect.tokenEffects().get(EffectSlot.ON_BECOMES_TARGET_OF_SPELL))
                .isInstanceOf(SacrificeSelfEffect.class);
    }

    @Test
    @DisplayName("-5 ability has CreateTokenCopyOfSourceEffect(removeLegendary=true, amount=2)")
    void minusFiveAbilityHasCorrectEffect() {
        JaceCunningCastaway card = new JaceCunningCastaway();
        var ability = card.getActivatedAbilities().get(2);

        assertThat(ability.getLoyaltyCost()).isEqualTo(-5);
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(CreateTokenCopyOfSourceEffect.class);
        CreateTokenCopyOfSourceEffect effect =
                (CreateTokenCopyOfSourceEffect) ability.getEffects().getFirst();
        assertThat(effect.removeLegendary()).isTrue();
        assertThat(effect.amount()).isEqualTo(2);
    }

    // ===== +1 ability: delayed combat damage loot trigger =====

    @Test
    @DisplayName("+1 ability increases loyalty and registers delayed loot trigger")
    void plusOneRegistersDelayedLootTrigger() {
        Permanent jace = addReadyJace(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(jace.getLoyaltyCounters()).isEqualTo(4); // 3 + 1
        assertThat(gd.pendingDelayedCombatDamageLoots).hasSize(1);
        assertThat(gd.pendingDelayedCombatDamageLoots.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(gd.pendingDelayedCombatDamageLoots.getFirst().drawAmount()).isEqualTo(1);
        assertThat(gd.pendingDelayedCombatDamageLoots.getFirst().discardAmount()).isEqualTo(1);
    }

    @Test
    @DisplayName("+1 delayed trigger data is correctly stored with source card reference")
    void plusOneDelayedTriggerStoresSourceCard() {
        Permanent jace = addReadyJace(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.pendingDelayedCombatDamageLoots).hasSize(1);
        GameData.DelayedCombatDamageLoot loot = gd.pendingDelayedCombatDamageLoots.getFirst();
        assertThat(loot.sourceCard()).isNotNull();
        assertThat(loot.sourceCard().getName()).isEqualTo("Jace, Cunning Castaway");
    }

    // ===== -2 ability: Create Illusion token =====

    @Test
    @DisplayName("-2 ability creates a 2/2 Illusion token and decreases loyalty")
    void minusTwoCreatesIllusionToken() {
        Permanent jace = addReadyJace(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(jace.getLoyaltyCounters()).isEqualTo(1); // 3 - 2

        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        Permanent illusionToken = bf.stream()
                .filter(p -> p.getCard().getName().equals("Illusion"))
                .findFirst()
                .orElse(null);
        assertThat(illusionToken).isNotNull();
        assertThat(illusionToken.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(illusionToken.getCard().getPower()).isEqualTo(2);
        assertThat(illusionToken.getCard().getToughness()).isEqualTo(2);
        assertThat(illusionToken.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(illusionToken.getCard().getSubtypes()).containsExactly(CardSubtype.ILLUSION);
        assertThat(illusionToken.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("-2 Illusion token has ON_BECOMES_TARGET_OF_SPELL sacrifice trigger")
    void illusionTokenHasSacrificeOnTargetTrigger() {
        addReadyJace(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        Permanent illusionToken = bf.stream()
                .filter(p -> p.getCard().getName().equals("Illusion"))
                .findFirst()
                .orElseThrow();

        assertThat(illusionToken.getCard().getEffects(EffectSlot.ON_BECOMES_TARGET_OF_SPELL)).hasSize(1);
        assertThat(illusionToken.getCard().getEffects(EffectSlot.ON_BECOMES_TARGET_OF_SPELL).getFirst())
                .isInstanceOf(SacrificeSelfEffect.class);
    }

    @Test
    @DisplayName("Cannot activate -2 when loyalty is only 1")
    void cannotActivateMinusTwoWithInsufficientLoyalty() {
        Permanent jace = addReadyJace(player1);
        jace.setLoyaltyCounters(1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    // ===== -5 ability: Create two non-legendary copies =====

    @Test
    @DisplayName("-5 ability creates two non-legendary token copies of Jace")
    void minusFiveCreatesTwoNonLegendaryCopies() {
        Permanent jace = addReadyJace(player1);
        jace.setLoyaltyCounters(5);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Original Jace should have 0 loyalty and be gone (SBA)
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> !p.getCard().isToken() && p.getCard().getName().equals("Jace, Cunning Castaway"))
                .toList()).isEmpty();

        // Two token copies should exist
        List<Permanent> jaceCopies = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Jace, Cunning Castaway"))
                .toList();
        assertThat(jaceCopies).hasSize(2);

        for (Permanent copy : jaceCopies) {
            // Tokens are NOT legendary
            assertThat(copy.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
            // Tokens should have loyalty counters
            assertThat(copy.getLoyaltyCounters()).isEqualTo(3); // initial loyalty of Jace
            // Tokens should have the same loyalty abilities
            assertThat(copy.getCard().getActivatedAbilities()).hasSize(3);
            // Tokens are planeswalkers
            assertThat(copy.getCard().getType()).isEqualTo(CardType.PLANESWALKER);
        }
    }

    @Test
    @DisplayName("Cannot activate -5 when loyalty is only 3")
    void cannotActivateMinusFiveWithInsufficientLoyalty() {
        addReadyJace(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    // ===== Loyalty ability restrictions =====

    @Test
    @DisplayName("Cannot activate loyalty ability during opponent's turn")
    void cannotActivateOnOpponentsTurn() {
        addReadyJace(player1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your turn");
    }

    @Test
    @DisplayName("Cannot activate two loyalty abilities on same planeswalker in one turn")
    void cannotActivateTwicePerTurn() {
        addReadyJace(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one loyalty ability");
    }

    // ===== Helpers =====

    private Permanent addReadyJace(Player player) {
        JaceCunningCastaway card = new JaceCunningCastaway();
        Permanent perm = new Permanent(card);
        perm.setLoyaltyCounters(3);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
