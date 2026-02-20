package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShivanDragonTest {

    private GameTestHarness harness;
    private Player player1;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("Shivan Dragon has correct card properties")
    void hasCorrectProperties() {
        ShivanDragon card = new ShivanDragon();

        assertThat(card.getName()).isEqualTo("Shivan Dragon");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{4}{R}{R}");
        assertThat(card.getColor()).isEqualTo(CardColor.RED);
        assertThat(card.getPower()).isEqualTo(5);
        assertThat(card.getToughness()).isEqualTo(5);
        assertThat(card.getKeywords()).containsExactly(Keyword.FLYING);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.DRAGON);
        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{R}");
        assertThat(card.getActivatedAbilities().getFirst().isNeedsTarget()).isFalse();
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().getFirst().getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().getFirst()).isInstanceOf(BoostSelfEffect.class);
        BoostSelfEffect effect = (BoostSelfEffect) card.getActivatedAbilities().getFirst().getEffects().getFirst();
        assertThat(effect.powerBoost()).isEqualTo(1);
        assertThat(effect.toughnessBoost()).isEqualTo(0);
    }

    @Test
    @DisplayName("Casting Shivan Dragon puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new ShivanDragon()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shivan Dragon");
    }

    @Test
    @DisplayName("Activating ability puts BoostSelf on the stack with self as target")
    void activatingAbilityPutsOnStack() {
        Permanent dragon = addReadyShivanDragon(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Shivan Dragon");
        assertThat(entry.getTargetPermanentId()).isEqualTo(dragon.getId());
    }

    @Test
    @DisplayName("Resolving ability gives +1/+0 to Shivan Dragon")
    void resolvingAbilityBoostsPower() {
        Permanent dragon = addReadyShivanDragon(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dragon.getEffectivePower()).isEqualTo(6);
        assertThat(dragon.getEffectiveToughness()).isEqualTo(5);
        assertThat(dragon.getPowerModifier()).isEqualTo(1);
        assertThat(dragon.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can activate ability multiple times if mana allows")
    void canActivateMultipleTimes() {
        Permanent dragon = addReadyShivanDragon(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dragon.getEffectivePower()).isEqualTo(7);
        assertThat(dragon.getEffectiveToughness()).isEqualTo(5);
        assertThat(dragon.getPowerModifier()).isEqualTo(2);
        assertThat(dragon.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent dragon = addReadyShivanDragon(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dragon.getEffectivePower()).isEqualTo(7);
        assertThat(dragon.getEffectiveToughness()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isEqualTo(0);
        assertThat(dragon.getToughnessModifier()).isEqualTo(0);
        assertThat(dragon.getEffectivePower()).isEqualTo(5);
        assertThat(dragon.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyShivanDragon(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadyShivanDragon(Player player) {
        ShivanDragon card = new ShivanDragon();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}

