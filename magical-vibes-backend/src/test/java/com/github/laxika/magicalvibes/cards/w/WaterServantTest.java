package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WaterServantTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Water Servant has two activated abilities")
    void hasTwoAbilities() {
        WaterServant card = new WaterServant();

        assertThat(card.getActivatedAbilities()).hasSize(2);

        // First ability: +1/-1
        BoostSelfEffect firstEffect = (BoostSelfEffect) card.getActivatedAbilities().get(0).getEffects().getFirst();
        assertThat(firstEffect.powerBoost()).isEqualTo(1);
        assertThat(firstEffect.toughnessBoost()).isEqualTo(-1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{U}");

        // Second ability: -1/+1
        BoostSelfEffect secondEffect = (BoostSelfEffect) card.getActivatedAbilities().get(1).getEffects().getFirst();
        assertThat(secondEffect.powerBoost()).isEqualTo(-1);
        assertThat(secondEffect.toughnessBoost()).isEqualTo(1);
        assertThat(card.getActivatedAbilities().get(1).getManaCost()).isEqualTo("{U}");
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Water Servant puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new WaterServant()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Water Servant");
    }

    @Test
    @DisplayName("Resolving Water Servant puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        harness.setHand(player1, List.of(new WaterServant()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Water Servant"));
    }

    // ===== Activate +1/-1 ability =====

    @Test
    @DisplayName("Activating +1/-1 ability puts BoostSelf on the stack")
    void activatingFirstAbilityPutsOnStack() {
        Permanent servantPerm = addWaterServantReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Water Servant");
        assertThat(entry.getTargetPermanentId()).isEqualTo(servantPerm.getId());
    }

    @Test
    @DisplayName("Resolving +1/-1 ability gives +1/-1 to Water Servant")
    void resolvingFirstAbilityBoosts() {
        addWaterServantReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent servant = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(servant.getEffectivePower()).isEqualTo(4);
        assertThat(servant.getEffectiveToughness()).isEqualTo(3);
        assertThat(servant.getPowerModifier()).isEqualTo(1);
        assertThat(servant.getToughnessModifier()).isEqualTo(-1);
    }

    // ===== Activate -1/+1 ability =====

    @Test
    @DisplayName("Resolving -1/+1 ability gives -1/+1 to Water Servant")
    void resolvingSecondAbilityBoosts() {
        addWaterServantReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent servant = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(servant.getEffectivePower()).isEqualTo(2);
        assertThat(servant.getEffectiveToughness()).isEqualTo(5);
        assertThat(servant.getPowerModifier()).isEqualTo(-1);
        assertThat(servant.getToughnessModifier()).isEqualTo(1);
    }

    // ===== Multiple activations =====

    @Test
    @DisplayName("Can activate both abilities to shift power/toughness")
    void canActivateBothAbilities() {
        addWaterServantReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        // Activate +1/-1
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        // Activate -1/+1
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Net effect: 0/0 modifier
        Permanent servant = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(servant.getEffectivePower()).isEqualTo(3);
        assertThat(servant.getEffectiveToughness()).isEqualTo(4);
        assertThat(servant.getPowerModifier()).isEqualTo(0);
        assertThat(servant.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can activate +1/-1 multiple times to become aggressive")
    void canActivateFirstAbilityMultipleTimes() {
        addWaterServantReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // 3+3 / 4-3 = 6/1
        Permanent servant = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(servant.getEffectivePower()).isEqualTo(6);
        assertThat(servant.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate -1/+1 multiple times to become defensive")
    void canActivateSecondAbilityMultipleTimes() {
        addWaterServantReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // 3-3 / 4+3 = 0/7
        Permanent servant = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(servant.getEffectivePower()).isEqualTo(0);
        assertThat(servant.getEffectiveToughness()).isEqualTo(7);
    }

    // ===== Boost resets =====

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addWaterServantReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent servant = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(servant.getEffectivePower()).isEqualTo(5);
        assertThat(servant.getEffectiveToughness()).isEqualTo(2);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(servant.getPowerModifier()).isEqualTo(0);
        assertThat(servant.getToughnessModifier()).isEqualTo(0);
        assertThat(servant.getEffectivePower()).isEqualTo(3);
        assertThat(servant.getEffectiveToughness()).isEqualTo(4);
    }

    // ===== Mana checks =====

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addWaterServantReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== Helper methods =====

    private Permanent addWaterServantReady(Player player) {
        WaterServant card = new WaterServant();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
