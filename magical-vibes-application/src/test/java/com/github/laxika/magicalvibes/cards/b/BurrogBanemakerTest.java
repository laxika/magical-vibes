package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BurrogBanemakerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Burrog Banemaker puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new BurrogBanemaker()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Burrog Banemaker");
    }

    @Test
    @DisplayName("Resolving Burrog Banemaker puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        harness.setHand(player1, List.of(new BurrogBanemaker()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Burrog Banemaker"));
    }

    @Test
    @DisplayName("Activating ability puts BoostSelf on the stack with self as target")
    void activatingAbilityPutsOnStack() {
        Permanent banemakerPerm = addBurrogBanemakerReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Burrog Banemaker");
        assertThat(entry.getTargetId()).isEqualTo(banemakerPerm.getId());
    }

    @Test
    @DisplayName("Resolving ability gives +1/+1 to Burrog Banemaker")
    void resolvingAbilityBoostsPowerAndToughness() {
        addBurrogBanemakerReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        Permanent banemaker = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(banemaker.getEffectivePower()).isEqualTo(2);
        assertThat(banemaker.getEffectiveToughness()).isEqualTo(2);
        assertThat(banemaker.getPowerModifier()).isEqualTo(1);
        assertThat(banemaker.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate ability multiple times if mana allows")
    void canActivateMultipleTimes() {
        addBurrogBanemakerReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent banemaker = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(banemaker.getEffectivePower()).isEqualTo(4);
        assertThat(banemaker.getEffectiveToughness()).isEqualTo(4);
        assertThat(banemaker.getPowerModifier()).isEqualTo(3);
        assertThat(banemaker.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addBurrogBanemakerReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent banemaker = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(banemaker.getEffectivePower()).isEqualTo(3);
        assertThat(banemaker.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(banemaker.getPowerModifier()).isEqualTo(0);
        assertThat(banemaker.getToughnessModifier()).isEqualTo(0);
        assertThat(banemaker.getEffectivePower()).isEqualTo(1);
        assertThat(banemaker.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addBurrogBanemakerReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addBurrogBanemakerReady(Player player) {
        BurrogBanemaker card = new BurrogBanemaker();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
