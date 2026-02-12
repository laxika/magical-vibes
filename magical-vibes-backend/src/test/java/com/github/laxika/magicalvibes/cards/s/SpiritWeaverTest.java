package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpiritWeaverTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Spirit Weaver has correct card properties")
    void hasCorrectProperties() {
        SpiritWeaver card = new SpiritWeaver();

        assertThat(card.getName()).isEqualTo("Spirit Weaver");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{1}{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(1);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.HUMAN, CardSubtype.WIZARD);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.MANA_ACTIVATED_ABILITY)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.MANA_ACTIVATED_ABILITY).getFirst())
                .isInstanceOf(BoostTargetCreatureEffect.class);
        BoostTargetCreatureEffect effect = (BoostTargetCreatureEffect) card.getEffects(EffectSlot.MANA_ACTIVATED_ABILITY).getFirst();
        assertThat(effect.powerBoost()).isEqualTo(0);
        assertThat(effect.toughnessBoost()).isEqualTo(1);
        assertThat(card.getManaActivatedAbilityCost()).isEqualTo("{2}");
    }

    // ===== Activation =====

    @Test
    @DisplayName("Activating ability puts it on the stack with target")
    void activatingPutsOnStackWithTarget() {
        addReadyWeaver(player1);
        Permanent target = addReadyBears(player1);
        harness.addMana(player1, "W", 2);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Spirit Weaver");
        assertThat(entry.getTargetPermanentId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Activating ability does not tap Spirit Weaver")
    void activatingDoesNotTap() {
        Permanent weaver = addReadyWeaver(player1);
        addReadyBears(player1);
        harness.addMana(player1, "W", 2);

        harness.activateAbility(player1, 0, null, weaver.getId());

        assertThat(weaver.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addReadyWeaver(player1);
        Permanent target = addReadyBears(player1);
        harness.addMana(player1, "W", 3);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Resolving ability gives target creature +0/+1")
    void resolvingBoostsTargetToughness() {
        addReadyWeaver(player1);
        Permanent target = addReadyBears(player1);
        harness.addMana(player1, "W", 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
        assertThat(target.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can activate ability multiple times on same target")
    void canActivateMultipleTimes() {
        addReadyWeaver(player1);
        Permanent target = addReadyBears(player1);
        harness.addMana(player1, "W", 6);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
    }

    // ===== End of turn cleanup =====

    @Test
    @DisplayName("Boost resets at end of turn")
    void boostResetsAtEndOfTurn() {
        addReadyWeaver(player1);
        Permanent target = addReadyBears(player1);
        harness.addMana(player1, "W", 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectiveToughness()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getToughnessModifier()).isEqualTo(0);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyWeaver(player1);
        Permanent target = addReadyBears(player1);
        harness.addMana(player1, "W", 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== Helpers =====

    private Permanent addReadyWeaver(Player player) {
        SpiritWeaver card = new SpiritWeaver();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyBears(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
