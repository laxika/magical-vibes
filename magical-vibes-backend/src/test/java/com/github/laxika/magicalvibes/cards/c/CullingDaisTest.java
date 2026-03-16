package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardsEqualToChargeCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CullingDaisTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Culling Dais has two activated abilities with correct effect types")
    void hasCorrectAbilityStructure() {
        CullingDais card = new CullingDais();

        assertThat(card.getActivatedAbilities()).hasSize(2);

        // Ability 0: {T}, Sacrifice a creature: Put a charge counter on Culling Dais.
        var ability0 = card.getActivatedAbilities().get(0);
        assertThat(ability0.isRequiresTap()).isTrue();
        assertThat(ability0.getManaCost()).isNull();
        assertThat(ability0.getEffects()).hasSize(2);
        assertThat(ability0.getEffects().get(0)).isInstanceOf(SacrificeCreatureCost.class);
        assertThat(ability0.getEffects().get(1)).isInstanceOf(PutChargeCounterOnSelfEffect.class);

        // Ability 1: {1}, Sacrifice Culling Dais: Draw a card for each charge counter on Culling Dais.
        var ability1 = card.getActivatedAbilities().get(1);
        assertThat(ability1.isRequiresTap()).isFalse();
        assertThat(ability1.getManaCost()).isEqualTo("{1}");
        assertThat(ability1.getEffects()).hasSize(2);
        assertThat(ability1.getEffects().get(0)).isInstanceOf(SacrificeSelfCost.class);
        assertThat(ability1.getEffects().get(1)).isInstanceOf(DrawCardsEqualToChargeCountersOnSourceEffect.class);
    }

    // ===== Ability 0: Sacrifice creature to add charge counter =====

    @Test
    @DisplayName("Sacrificing a creature adds a charge counter to Culling Dais")
    void sacrificeCreatureAddsChargeCounter() {
        Permanent dais = addReadyDais(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve ability

        GameData gd = harness.getGameData();

        // Grizzly Bears should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Culling Dais should have 1 charge counter
        assertThat(dais.getChargeCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability 0 requires tap")
    void ability0RequiresTap() {
        Permanent dais = addReadyDais(player1);
        dais.tap();
        harness.addToBattlefield(player1, new GrizzlyBears());

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.activateAbility(player1, 0, null, null)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multiple sacrifices accumulate charge counters")
    void multipleSacrificesAccumulateCounters() {
        Permanent dais = addReadyDais(player1);

        // First sacrifice
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dais.getChargeCounters()).isEqualTo(1);

        // Untap Culling Dais for second activation
        dais.untap();

        // Second sacrifice
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dais.getChargeCounters()).isEqualTo(2);
    }

    // ===== Ability 1: Sacrifice self to draw =====

    @Test
    @DisplayName("Sacrificing Culling Dais draws cards equal to charge counters")
    void sacrificeSelfDrawsCardsEqualToChargeCounters() {
        Permanent dais = addReadyDais(player1);
        dais.setChargeCounters(3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities(); // resolve ability

        GameData gd = harness.getGameData();

        // Culling Dais should be in the graveyard
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Culling Dais"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Culling Dais"));

        // Should have drawn 3 cards
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 3);
    }

    @Test
    @DisplayName("Sacrificing Culling Dais with 0 counters draws 0 cards")
    void sacrificeSelfWithZeroCountersDrawsNothing() {
        Permanent dais = addReadyDais(player1);
        // No charge counters
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Culling Dais should be in the graveyard
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Culling Dais"));

        // Should not have drawn any cards
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    @Test
    @DisplayName("Culling Dais goes to graveyard after sacrifice")
    void cullingDaisGoesToGraveyardAfterSacrifice() {
        addReadyDais(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        GameData gd = harness.getGameData();

        // Should be in graveyard immediately (sacrifice is a cost)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Culling Dais"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Culling Dais"));
    }

    // ===== Helper methods =====

    private Permanent addReadyDais(Player player) {
        CullingDais card = new CullingDais();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
