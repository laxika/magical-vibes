package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DarkthicketWolfTest extends BaseCardTest {

    @Test
    @DisplayName("Darkthicket Wolf has correct activated ability")
    void hasCorrectProperties() {
        DarkthicketWolf card = new DarkthicketWolf();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{2}{G}");
        assertThat(card.getActivatedAbilities().getFirst().getMaxActivationsPerTurn()).isEqualTo(1);
        assertThat(card.getActivatedAbilities().getFirst().getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().getFirst()).isInstanceOf(BoostSelfEffect.class);
    }

    @Test
    @DisplayName("Casting Darkthicket Wolf puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new DarkthicketWolf()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(harness.getGameData().stack).hasSize(1);
        assertThat(harness.getGameData().stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(harness.getGameData().stack.getFirst().getCard().getName()).isEqualTo("Darkthicket Wolf");
    }

    @Test
    @DisplayName("Ability gives +2/+2 until end of turn")
    void abilityGivesBoost() {
        Permanent wolf = addReadyWolf(player1);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wolf.getEffectivePower()).isEqualTo(4);
        assertThat(wolf.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Second activation in same turn is rejected")
    void secondActivationInSameTurnIsRejected() {
        addReadyWolf(player1);
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no more than 1 times each turn");
    }

    @Test
    @DisplayName("Activation limit resets on a new turn")
    void activationLimitResetsOnNewTurn() {
        addReadyWolf(player1);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.activateAbility(player1, 0, null, null);

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    private Permanent addReadyWolf(Player player) {
        DarkthicketWolf card = new DarkthicketWolf();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
