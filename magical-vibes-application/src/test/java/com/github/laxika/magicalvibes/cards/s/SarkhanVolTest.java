package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SarkhanVolTest extends BaseCardTest {

    // ===== +1: Creatures you control get +1/+1 and gain haste =====

    @Test
    @DisplayName("+1 gives controlled creatures +1/+1 and haste until end of turn")
    void plusOneBoostsAndGrantsHaste() {
        Permanent sarkhan = addReadySarkhan(player1);
        Permanent bear = addReadyCreature(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isEqualTo(5); // 4 + 1
        assertThat(bear.getEffectivePower()).isEqualTo(3);
        assertThat(bear.getEffectiveToughness()).isEqualTo(3);
        assertThat(bear.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("+1 does not affect opponent's creatures")
    void plusOneDoesNotAffectOpponentCreatures() {
        addReadySarkhan(player1);
        Permanent oppBear = addReadyCreature(player2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(oppBear.getEffectivePower()).isEqualTo(2);
        assertThat(oppBear.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("+1 boost and haste wear off at end of turn")
    void plusOneWearsOff() {
        addReadySarkhan(player1);
        Permanent bear = addReadyCreature(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.hasKeyword(Keyword.HASTE)).isFalse();
    }

    // ===== -2: Gain control of target creature, untap, haste =====

    @Test
    @DisplayName("-2 steals target creature, untaps it, and grants haste")
    void minusTwoStealsUntapsAndGrantsHaste() {
        Permanent sarkhan = addReadySarkhan(player1);
        Permanent target = addReadyCreature(player2);
        target.tap();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isEqualTo(2); // 4 - 2
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("-2 control and haste expire at end of turn")
    void minusTwoExpiresAtEndOfTurn() {
        addReadySarkhan(player1);
        Permanent target = addReadyCreature(player2);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("-2 cannot target a non-creature permanent")
    void minusTwoCannotTargetNonCreature() {
        addReadySarkhan(player1);
        addReadyCreature(player2); // valid target exists
        Permanent enchantment = new Permanent(new Pacifism());
        gd.playerBattlefields.get(player2.getId()).add(enchantment);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== -6: Create five 4/4 red Dragon tokens with flying =====

    @Test
    @DisplayName("-6 creates five 4/4 red Dragon tokens with flying")
    void minusSixCreatesFiveDragons() {
        Permanent sarkhan = addReadySarkhan(player1);
        sarkhan.setCounterCount(CounterType.LOYALTY, 6);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Sarkhan dies (6 - 6 = 0)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Sarkhan Vol"));

        List<Permanent> dragons = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Dragon"))
                .toList();
        assertThat(dragons).hasSize(5);
        assertThat(dragons).allSatisfy(d -> {
            assertThat(d.getCard().getPower()).isEqualTo(4);
            assertThat(d.getCard().getToughness()).isEqualTo(4);
            assertThat(d.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(d.getCard().getSubtypes()).contains(CardSubtype.DRAGON);
            assertThat(d.hasKeyword(Keyword.FLYING)).isTrue();
        });
    }

    @Test
    @DisplayName("Cannot activate -6 with only 4 loyalty")
    void cannotActivateUltimateWithInsufficientLoyalty() {
        addReadySarkhan(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    // ===== Helpers =====

    private Permanent addReadySarkhan(Player player) {
        SarkhanVol card = new SarkhanVol();
        Permanent perm = new Permanent(card);
        perm.setCounterCount(CounterType.LOYALTY, 4);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
