package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EncampmentKeeperTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has one activated ability with tap, sacrifice cost and boost effect")
    void hasCorrectAbility() {
        EncampmentKeeper card = new EncampmentKeeper();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{7}{W}");
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getEffects()).hasSize(2);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().get(0))
                .isInstanceOf(SacrificeSelfCost.class);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().get(1))
                .isInstanceOf(BoostAllOwnCreaturesEffect.class);

        BoostAllOwnCreaturesEffect boost = (BoostAllOwnCreaturesEffect)
                card.getActivatedAbilities().getFirst().getEffects().get(1);
        assertThat(boost.powerBoost()).isEqualTo(2);
        assertThat(boost.toughnessBoost()).isEqualTo(2);
    }

    // ===== Activating ability =====

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingPutsOnStack() {
        Permanent keeper = addReadyCreature(player1, new EncampmentKeeper());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Activating ability requires {7}{W} mana")
    void requiresMana() {
        Permanent keeper = addReadyCreature(player1, new EncampmentKeeper());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate when summoning sick because ability requires tap")
    void cannotActivateWhenSummoningSick() {
        Permanent keeper = new Permanent(new EncampmentKeeper());
        keeper.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(keeper);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Resolving ability =====

    @Test
    @DisplayName("Resolving sacrifices Encampment Keeper and boosts all own creatures +2/+2")
    void resolvingSacrificesAndBoosts() {
        Permanent keeper = addReadyCreature(player1, new EncampmentKeeper());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Encampment Keeper is sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Encampment Keeper"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Encampment Keeper"));

        // Grizzly Bears (2/2) gets +2/+2 = 4/4
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        Permanent keeper = addReadyCreature(player1, new EncampmentKeeper());
        Permanent ownBears = addReadyCreature(player1, new GrizzlyBears());
        Permanent oppBears = addReadyCreature(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Own creature is boosted
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(4);

        // Opponent's creature is NOT boosted
        assertThat(gqs.getEffectivePower(gd, oppBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, oppBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at cleanup step")
    void boostWearsOffAtCleanup() {
        Permanent keeper = addReadyCreature(player1, new EncampmentKeeper());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    // ===== Helper methods =====

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
