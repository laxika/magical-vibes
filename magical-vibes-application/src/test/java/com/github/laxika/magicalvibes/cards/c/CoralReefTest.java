package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RevekaWizardSavant;
import com.github.laxika.magicalvibes.cards.s.SerraPaladin;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoralReef.class, Island.class, RevekaWizardSavant.class, SerraPaladin.class})
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
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(reef.getCounterCount(CounterType.POLYP)).isEqualTo(6);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island.getCard());
    }

    @Test
    @DisplayName("Second ability puts a +0/+1 counter on target creature, taps a blue creature and removes a polyp counter")
    void secondAbilityPutsToughnessCounterOnTarget() {
        Permanent reef = addReef(player1);
        Permanent blueCreature = addCreatureReady(player1, new RevekaWizardSavant());
        Permanent target = addCreatureReady(player1, new SerraPaladin());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(blueCreature.isTapped()).isTrue();
        assertThat(reef.getCounterCount(CounterType.POLYP)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Second ability cannot be activated without an untapped blue creature")
    void secondAbilityRequiresUntappedBlueCreature() {
        addReef(player1);
        Permanent target = addCreatureReady(player1, new SerraPaladin());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Second ability cannot be activated without a polyp counter")
    void secondAbilityRequiresPolypCounter() {
        Permanent reef = addReef(player1);
        reef.setCounterCount(CounterType.POLYP, 0);
        addCreatureReady(player1, new RevekaWizardSavant());
        Permanent target = addCreatureReady(player1, new SerraPaladin());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Sacrifice ability cannot be activated without an Island")
    void sacrificeAbilityRequiresIsland() {
        addReef(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Second ability cannot be activated with a tapped blue creature")
    void secondAbilityRequiresUntappedBlueCreatureWhenBlueCreatureIsTapped() {
        addReef(player1);
        Permanent blueCreature = addCreatureReady(player1, new RevekaWizardSavant());
        blueCreature.tap();
        Permanent target = addCreatureReady(player1, new SerraPaladin());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Second ability cannot target a noncreature permanent")
    void secondAbilityRequiresCreatureTarget() {
        Permanent reef = addReef(player1);
        addCreatureReady(player1, new RevekaWizardSavant());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, reef.getId()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Second ability can tap a summoning-sick blue creature")
    void secondAbilityCanTapSummoningSickBlueCreature() {
        Permanent reef = addReef(player1);
        Permanent blueCreature = harness.addToBattlefieldAndReturn(player1, new RevekaWizardSavant());
        Permanent target = addCreatureReady(player1, new SerraPaladin());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(blueCreature.isSummoningSick()).isTrue();
        assertThat(blueCreature.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isEqualTo(1);
        assertThat(reef.getCounterCount(CounterType.POLYP)).isEqualTo(3);
    }

    private Permanent addReef(Player player) {
        return harness.enterBattlefieldAndReturn(player, new CoralReef());
    }
}
