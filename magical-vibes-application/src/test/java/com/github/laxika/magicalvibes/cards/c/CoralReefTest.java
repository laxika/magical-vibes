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

    @Test
    @DisplayName("A tapped Island is sacrificed immediately, but polyp counters are added on resolution")
    void sacrificeCostIsPaidBeforeCountersAreAdded() {
        Permanent reef = addReef(player1);
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.tap();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(island);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island.getCard());
        assertThat(reef.getCounterCount(CounterType.POLYP)).isEqualTo(4);

        harness.passBothPriorities();

        assertThat(reef.getCounterCount(CounterType.POLYP)).isEqualTo(6);
    }

    @Test
    @DisplayName("The blue creature tapped as a cost may also be the target")
    void tappedCreatureCanBeTargetAndCostsArePaidBeforeResolution() {
        Permanent reef = addReef(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RevekaWizardSavant());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, creature.getId());

        assertThat(creature.isTapped()).isTrue();
        assertThat(reef.getCounterCount(CounterType.POLYP)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(creature.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The toughness counter may be placed on an opponent's nonblue creature")
    void canTargetOpponentCreature() {
        addReef(player1);
        harness.addToBattlefield(player1, new RevekaWizardSavant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraPaladin());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's blue creature cannot pay the tap cost")
    void cannotTapOpponentCreatureForCost() {
        Permanent reef = addReef(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RevekaWizardSavant());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(RuntimeException.class);

        assertThat(creature.isTapped()).isFalse();
        assertThat(reef.getCounterCount(CounterType.POLYP)).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's Island cannot pay the sacrifice cost")
    void cannotSacrificeOpponentIsland() {
        Permanent reef = addReef(player1);
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(RuntimeException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(island);
        assertThat(reef.getCounterCount(CounterType.POLYP)).isEqualTo(4);
    }

    @Test
    @DisplayName("The toughness-counter ability requires blue mana")
    void secondAbilityRequiresBlueMana() {
        Permanent reef = addReef(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RevekaWizardSavant());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(RuntimeException.class);

        assertThat(creature.isTapped()).isFalse();
        assertThat(reef.getCounterCount(CounterType.POLYP)).isEqualTo(4);
    }

    private Permanent addReef(Player player) {
        return harness.enterBattlefieldAndReturn(player, new CoralReef());
    }
}
