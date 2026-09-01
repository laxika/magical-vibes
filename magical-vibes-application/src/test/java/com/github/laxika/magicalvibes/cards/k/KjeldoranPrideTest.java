package com.github.laxika.magicalvibes.cards.k;

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

@CardUsed({KjeldoranPride.class, KjeldoranEscort.class})
class KjeldoranPrideTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving attaches to the target creature and gives it +1/+2")
    void resolvingBoostsEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KjeldoranEscort());
        harness.setHand(player1, List.of(new KjeldoranPride()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("{2}{U} moves the Aura onto another creature, taking the boost with it")
    void activatedAbilityMovesAura() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new KjeldoranEscort());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new KjeldoranEscort());
        Permanent aura = attachAura(player1, first);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        prepareAbilityActivation();

        harness.activateAbility(player1, indexOf(player1, aura), null, second.getId());
        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(5);
    }

    @Test
    @DisplayName("The currently enchanted creature is not a legal target for the ability")
    void enchantedCreatureIsIllegalTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KjeldoranEscort());
        Permanent aura = attachAura(player1, creature);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        prepareAbilityActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, aura), null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void nonCreaturePermanentIsIllegalTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KjeldoranEscort());
        Permanent aura = attachAura(player1, creature);
        Permanent otherAura = attachAura(player1, creature);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        prepareAbilityActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, aura), null, otherAura.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reattachment ability requires two generic mana and one blue mana")
    void reattachmentAbilityRequiresTwoGenericAndBlueMana() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new KjeldoranEscort());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new KjeldoranEscort());
        Permanent aura = attachAura(player1, first);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        prepareAbilityActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, aura), null, second.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent attachAura(Player controller, Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new KjeldoranPride());
        aura.setAttachedTo(host.getId());
        return aura;
    }

    private void prepareAbilityActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
