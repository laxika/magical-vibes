package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WaterServant.class})
class WaterServantTest extends BaseCardTest {

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

        harness.assertOnBattlefield(player1, "Water Servant");
    }

    @Test
    @DisplayName("Activating +1/-1 ability puts BoostSelf on the stack")
    void activatingFirstAbilityPutsOnStack() {
        Permanent servantPerm = addCreatureReady(player1, new WaterServant());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Water Servant");
        assertThat(entry.getTargetId()).isEqualTo(servantPerm.getId());
    }

    @Test
    @DisplayName("Resolving +1/-1 ability gives +1/-1 to Water Servant")
    void resolvingFirstAbilityBoosts() {
        addCreatureReady(player1, new WaterServant());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent servant = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(servant.getEffectivePower()).isEqualTo(4);
        assertThat(servant.getEffectiveToughness()).isEqualTo(3);
        assertThat(servant.getPowerModifier()).isEqualTo(1);
        assertThat(servant.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Resolving -1/+1 ability gives -1/+1 to Water Servant")
    void resolvingSecondAbilityBoosts() {
        addCreatureReady(player1, new WaterServant());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent servant = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(servant.getEffectivePower()).isEqualTo(2);
        assertThat(servant.getEffectiveToughness()).isEqualTo(5);
        assertThat(servant.getPowerModifier()).isEqualTo(-1);
        assertThat(servant.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate both abilities to shift power/toughness")
    void canActivateBothAbilities() {
        addCreatureReady(player1, new WaterServant());
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
        addCreatureReady(player1, new WaterServant());
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
        addCreatureReady(player1, new WaterServant());
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

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addCreatureReady(player1, new WaterServant());
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

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new WaterServant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Both abilities can be activated while tapped and summoning sick")
    void abilitiesWorkWhileTappedAndSummoningSick() {
        Permanent servant = harness.addToBattlefieldAndReturn(player1, new WaterServant());
        servant.setSummoningSick(true);
        servant.setTapped(true);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(servant.getEffectivePower()).isEqualTo(4);
        assertThat(servant.getEffectiveToughness()).isEqualTo(3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(servant.getEffectivePower()).isEqualTo(3);
        assertThat(servant.getEffectiveToughness()).isEqualTo(4);
        assertThat(servant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Repeated defensive activations allow negative power and expire at cleanup")
    void negativePowerAndDefensiveBoostExpire() {
        Permanent servant = addCreatureReady(player1, new WaterServant());
        harness.addMana(player1, ManaColor.BLUE, 4);

        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, 1, null, null);
            harness.passBothPriorities();
        }

        assertThat(servant.getEffectivePower()).isEqualTo(-1);
        assertThat(servant.getEffectiveToughness()).isEqualTo(8);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(servant.getEffectivePower()).isEqualTo(3);
        assertThat(servant.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Fourth aggressive activation puts Water Servant into the graveyard")
    void zeroToughnessCausesDeath() {
        addCreatureReady(player1, new WaterServant());
        harness.addMana(player1, ManaColor.BLUE, 4);

        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(1)
                .allMatch(card -> card instanceof WaterServant);
    }
}
