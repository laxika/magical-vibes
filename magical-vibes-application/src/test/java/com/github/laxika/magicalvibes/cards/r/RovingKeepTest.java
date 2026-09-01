package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RovingKeep.class, GrizzlyBears.class})
class RovingKeepTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack without activating the ability")
    void cannotAttackWithDefender() {
        addKeepReady();
        beginAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Ability grants +2/+0, trample, and permission to attack")
    void abilityBoostsAndAllowsAttack() {
        Permanent keep = addKeepReady();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(keep.getPowerModifier()).isEqualTo(2);
        assertThat(keep.getToughnessModifier()).isZero();
        assertThat(keep.getGrantedKeywords()).contains(Keyword.TRAMPLE);

        beginAttackers();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(keep.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Boost, trample, and attack permission wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent keep = addKeepReady();
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(keep.getPowerModifier()).isZero();
        assertThat(keep.getToughnessModifier()).isZero();
        assertThat(keep.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);

        beginAttackers();
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutMana() {
        addKeepReady();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addKeepReady() {
        Permanent keep = new Permanent(new RovingKeep());
        keep.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(keep);
        return keep;
    }

    private void beginAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginInteraction(new PendingInteraction.AttackerDeclaration(player1.getId()));
    }
}
