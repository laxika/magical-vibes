package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({SkarrgGoliath.class, GrizzlyBears.class})
class SkarrgGoliathTest extends BaseCardTest {

    private Permanent attackingBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.GREEN, 7);
        return bears;
    }

    @Test
    @DisplayName("Bloodrush gives target attacking creature +9/+9 and trample")
    void bloodrushBoostsAttackingCreature() {
        harness.setHand(player1, List.of(new SkarrgGoliath()));
        Permanent bears = attackingBears();

        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(11);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(11);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        harness.assertInGraveyard(player1, "Skarrg Goliath");
    }

    @Test
    @DisplayName("The bloodrush boost and trample wear off at end of turn")
    void bloodrushWearsOff() {
        harness.setHand(player1, List.of(new SkarrgGoliath()));
        Permanent bears = attackingBears();

        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Bloodrush cannot target a creature that isn't attacking")
    void bloodrushRejectsNonAttackingCreature() {
        harness.setHand(player1, List.of(new SkarrgGoliath()));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.GREEN, 7);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Skarrg Goliath");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(7);
    }

    @Test
    @DisplayName("Bloodrush pays mana and discards the source before resolving")
    void bloodrushPaysCostsBeforeResolution() {
        harness.setHand(player1, List.of(new SkarrgGoliath()));
        Permanent bears = attackingBears();

        harness.activateHandAbility(player1, 0, bears.getId());

        harness.assertNotInHand(player1, "Skarrg Goliath");
        harness.assertInGraveyard(player1, "Skarrg Goliath");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(11);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Bloodrush does not resolve if its target stops attacking")
    void bloodrushRejectsTargetNoLongerAttackingOnResolution() {
        harness.setHand(player1, List.of(new SkarrgGoliath()));
        Permanent bears = attackingBears();

        harness.activateHandAbility(player1, 0, bears.getId());
        bears.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
        harness.assertInGraveyard(player1, "Skarrg Goliath");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Bloodrush can boost an opponent's attacking creature")
    void bloodrushCanTargetOpponentsAttacker() {
        harness.setHand(player1, List.of(new SkarrgGoliath()));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.ensurePriority(player1);

        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(11);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(11);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Bloodrush requires two green mana even with seven total mana")
    void bloodrushRequiresTwoGreenMana() {
        harness.setHand(player1, List.of(new SkarrgGoliath()));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Skarrg Goliath");
        harness.assertNotInGraveyard(player1, "Skarrg Goliath");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(7);
        assertThat(gd.stack).isEmpty();
    }
}
