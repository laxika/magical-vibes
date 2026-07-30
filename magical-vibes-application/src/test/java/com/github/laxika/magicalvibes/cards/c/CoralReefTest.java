package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CoralReefTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with four polyp counters")
    void entersWithFourPolypCounters() {
        harness.setHand(player1, List.of(new CoralReef()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).getLast()
                .getCounterCount(CounterType.POLYP)).isEqualTo(4);
    }

    @Test
    @DisplayName("Sacrificing an Island puts two more polyp counters on the enchantment")
    void sacrificeIslandAddsTwoPolypCounters() {
        Permanent reef = addReef(player1);
        harness.addToBattlefield(player1, new Island());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(reef.getCounterCount(CounterType.POLYP)).isEqualTo(6);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Island"));
    }

    @Test
    @DisplayName("Second ability puts a +0/+1 counter on target creature, taps a blue creature and removes a polyp counter")
    void secondAbilityPutsToughnessCounterOnTarget() {
        Permanent reef = addReef(player1);
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        wizard.setSummoningSick(false);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(wizard.isTapped()).isTrue();
        assertThat(reef.getCounterCount(CounterType.POLYP)).isEqualTo(3);
    }

    @Test
    @DisplayName("Second ability cannot be activated without an untapped blue creature")
    void secondAbilityRequiresUntappedBlueCreature() {
        addReef(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Second ability cannot be activated without a polyp counter")
    void secondAbilityRequiresPolypCounter() {
        Permanent reef = addReef(player1);
        reef.setCounterCount(CounterType.POLYP, 0);
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        wizard.setSummoningSick(false);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Sacrifice ability cannot be activated without an Island")
    void sacrificeAbilityRequiresIsland() {
        addReef(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(RuntimeException.class);
    }

    private Permanent addReef(Player player) {
        Permanent reef = harness.addToBattlefieldAndReturn(player, new CoralReef());
        reef.setCounterCount(CounterType.POLYP, 4);
        return reef;
    }
}
