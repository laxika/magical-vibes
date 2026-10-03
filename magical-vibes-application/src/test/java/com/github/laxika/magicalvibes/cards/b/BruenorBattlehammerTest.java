package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.Greataxe;
import com.github.laxika.magicalvibes.cards.l.LeatherArmor;
import com.github.laxika.magicalvibes.cards.s.SteadfastPaladin;
import com.github.laxika.magicalvibes.cards.s.SwiftfootBoots;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BruenorBattlehammer.class, GrizzlyBears.class, SwiftfootBoots.class,
        Greataxe.class, LeatherArmor.class, SteadfastPaladin.class})
class BruenorBattlehammerTest extends BaseCardTest {

    @Test
    void countsEquipmentAttachedToEachCreatureIndividually() {
        harness.addToBattlefield(player1, new BruenorBattlehammer());
        Permanent creatureWithTwoEquipment = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent creatureWithoutEquipment = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent firstEquipment = harness.addToBattlefieldAndReturn(player1, new SwiftfootBoots());
        firstEquipment.setAttachedTo(creatureWithTwoEquipment.getId());
        Permanent secondEquipment = harness.addToBattlefieldAndReturn(player1, new SwiftfootBoots());
        secondEquipment.setAttachedTo(creatureWithTwoEquipment.getId());

        assertThat(gqs.getEffectivePower(gd, creatureWithTwoEquipment)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creatureWithTwoEquipment)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creatureWithoutEquipment)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creatureWithoutEquipment)).isEqualTo(2);
    }

    @Test
    void firstEquipActivationIsFreeAndLaterActivationPaysNormally() {
        harness.addToBattlefield(player1, new BruenorBattlehammer());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new SwiftfootBoots());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 1, 0, null, firstCreature.getId());
        harness.passBothPriorities();
        assertThat(equipment.getAttachedTo()).isEqualTo(firstCreature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, 0, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void boostsBruenorWithOpponentsEquipmentButDoesNotBoostOpponentsCreature() {
        Permanent bruenor = harness.addToBattlefieldAndReturn(player1, new BruenorBattlehammer());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SteadfastPaladin());
        Permanent equipmentOnBruenor = harness.addToBattlefieldAndReturn(player2, new Greataxe());
        equipmentOnBruenor.setAttachedTo(bruenor.getId());
        Permanent opponentEquipment = harness.addToBattlefieldAndReturn(player2, new Greataxe());
        opponentEquipment.setAttachedTo(opponentCreature.getId());

        assertThat(gqs.getEffectivePower(gd, bruenor)).isEqualTo(11);
        assertThat(gqs.getEffectiveToughness(gd, bruenor)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    void bonusFollowsEquipmentWhenItMovesBetweenCreatures() {
        harness.addToBattlefield(player1, new BruenorBattlehammer());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Greataxe());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());

        harness.activateAbility(player1, 1, 0, null, firstCreature.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(2);

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 1, 0, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(8);
    }

    @Test
    void equipActivatedBeforeBruenorEnteredStillConsumesFirstActivation() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Greataxe());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 0, null, firstCreature.getId());
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new BruenorBattlehammer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, secondCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(equipment.getAttachedTo()).isEqualTo(firstCreature.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 0, null, secondCreature.getId());
        harness.passBothPriorities();
        assertThat(equipment.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void zeroCostEquipConsumesFreeActivationForOtherEquipment() {
        harness.addToBattlefield(player1, new BruenorBattlehammer());
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new LeatherArmor());
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new Greataxe());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());

        harness.activateAbility(player1, 1, 0, null, creature.getId());
        harness.passBothPriorities();
        assertThat(armor.getAttachedTo()).isEqualTo(creature.getId());
        assertThatThrownBy(() -> harness.activateAbility(player1, 2, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 2, 0, null, creature.getId());
        harness.passBothPriorities();
        assertThat(axe.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void freeEquipBecomesAvailableAgainOnNextTurn() {
        harness.addToBattlefield(player1, new BruenorBattlehammer());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Greataxe());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        harness.activateAbility(player1, 1, 0, null, firstCreature.getId());
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 1, 0, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
