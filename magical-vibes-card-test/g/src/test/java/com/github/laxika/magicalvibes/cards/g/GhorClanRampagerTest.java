package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DiscipleOfTheOldWays;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhorClanRampager.class, DiscipleOfTheOldWays.class})
class GhorClanRampagerTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodrush gives an attacking creature +4/+4 and trample")
    void bloodrushBoostsAttacker() {
        harness.setHand(player1, List.of(new GhorClanRampager()));
        Permanent bears = addCreatureReady(player1, new DiscipleOfTheOldWays());
        bears.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(bears.getEffectivePower()).isEqualTo(6);
        assertThat(bears.getEffectiveToughness()).isEqualTo(6);
        assertThat(bears.hasKeyword(Keyword.TRAMPLE)).isTrue();
        harness.assertInGraveyard(player1, "Ghor-Clan Rampager");
    }

    @Test
    @DisplayName("Bloodrush boost and trample wear off at end of turn")
    void bloodrushWearsOff() {
        harness.setHand(player1, List.of(new GhorClanRampager()));
        Permanent bears = addCreatureReady(player1, new DiscipleOfTheOldWays());
        bears.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Bloodrush cannot target a non-attacking creature; no cost is paid")
    void bloodrushRejectsNonAttackingCreature() {
        harness.setHand(player1, List.of(new GhorClanRampager()));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DiscipleOfTheOldWays());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Ghor-Clan Rampager");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Bloodrush can boost an opponent's attacking creature")
    void bloodrushCanTargetOpponentsAttacker() {
        harness.setHand(player1, List.of(new GhorClanRampager()));
        Permanent attacker = addCreatureReady(player2, new DiscipleOfTheOldWays());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, attacker.getId());

        harness.assertInGraveyard(player1, "Ghor-Clan Rampager");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isFalse();

        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(6);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(6);
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Bloodrush does not resolve if its target stops attacking")
    void bloodrushRechecksAttackingRestriction() {
        harness.setHand(player1, List.of(new GhorClanRampager()));
        Permanent attacker = addCreatureReady(player1, new DiscipleOfTheOldWays());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isFalse();
        harness.assertInGraveyard(player1, "Ghor-Clan Rampager");
    }

    @Test
    @DisplayName("Bloodrush cannot be activated without its green mana cost")
    void bloodrushRequiresBothColors() {
        harness.setHand(player1, List.of(new GhorClanRampager()));
        Permanent attacker = addCreatureReady(player1, new DiscipleOfTheOldWays());
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Ghor-Clan Rampager");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
