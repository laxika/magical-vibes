package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForbiddenLore.class, Forest.class, BalduvianBears.class})
class ForbiddenLoreTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Forbidden Lore attaches it to a target land")
    void resolvesAttachedToTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new ForbiddenLore()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Forbidden Lore").getAttachedTo())
                .isEqualTo(forest.getId());
    }

    @Test
    @DisplayName("Cannot cast Forbidden Lore targeting a nonland permanent")
    void cannotTargetNonLand() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        harness.setHand(player1, List.of(new ForbiddenLore()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Enchanted land's granted ability gives target creature +2/+1")
    void grantedAbilityBoostsTargetCreature() {
        Permanent forest = attachAura(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Granted ability can target an opponent's creature")
    void grantedAbilityBoostsOpponentsCreature() {
        Permanent forest = attachAura(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        attachAura(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Aura can enchant an opponent's land")
    void grantsAbilityToOpponentControlledLand() {
        Permanent forest = attachAura(player1, player2);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player2, 0, 0, bears.getId(), null);
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Granted ability cannot target a land")
    void grantedAbilityRequiresCreatureTarget() {
        Permanent forest = attachAura(player1);
        Permanent otherForest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, otherForest.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
        assertThat(forest.isTapped()).isFalse();
    }

    private Permanent attachAura(final Player player) {
        return attachAura(player, player);
    }

    private Permanent attachAura(final Player auraController, final Player landController) {
        Permanent forest = harness.addToBattlefieldAndReturn(landController, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(auraController, new ForbiddenLore());
        aura.setAttachedTo(forest.getId());
        return forest;
    }
}
