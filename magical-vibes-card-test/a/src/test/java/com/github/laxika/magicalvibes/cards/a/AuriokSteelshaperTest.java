package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Frogmite;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LeoninSkyhunter;
import com.github.laxika.magicalvibes.cards.n.NeurokHoversail;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AuriokSteelshaper.class, Frogmite.class, LeoninScimitar.class,
        LeoninSkyhunter.class, NeurokHoversail.class})
class AuriokSteelshaperTest extends BaseCardTest {

    @Test
    @DisplayName("Soldiers and Knights you control get +1/+1 only while Auriok Steelshaper is equipped")
    void buffsControlledSoldiersAndKnightsWhileEquipped() {
        Permanent steelshaper = harness.addToBattlefieldAndReturn(player1, new AuriokSteelshaper());
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new LeoninSkyhunter());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new Frogmite());
        Permanent opponentKnight = harness.addToBattlefieldAndReturn(player2, new LeoninSkyhunter());

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);

        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        scimitar.setAttachedTo(steelshaper.getId());

        assertThat(gqs.getEffectivePower(gd, steelshaper)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, steelshaper)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentKnight)).isEqualTo(2);
    }

    @Test
    @DisplayName("The equipped Steelshaper itself gets +1/+1 when it is a Soldier")
    void buffsItselfWhileEquipped() {
        Permanent steelshaper = harness.addToBattlefieldAndReturn(player1, new AuriokSteelshaper());
        Permanent hoversail = harness.addToBattlefieldAndReturn(player1, new NeurokHoversail());

        assertThat(gqs.getEffectivePower(gd, steelshaper)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, steelshaper)).isEqualTo(1);

        hoversail.setAttachedTo(steelshaper.getId());

        assertThat(gqs.getEffectivePower(gd, steelshaper)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, steelshaper)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip abilities of Equipment you control cost {1} less")
    void reducesEquipCost() {
        harness.addToBattlefield(player1, new AuriokSteelshaper());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Frogmite());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(scimitar), null, creature.getId());
        harness.passBothPriorities();

        assertThat(scimitar.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void reducesTwoManaEquipCostToOne() {
        harness.addToBattlefield(player1, new AuriokSteelshaper());
        Permanent hoversail = harness.addToBattlefieldAndReturn(player1, new NeurokHoversail());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Frogmite());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, creature.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();
        assertThat(hoversail.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void multipleSteelshapersReduceEquipCostToZero() {
        harness.addToBattlefield(player1, new AuriokSteelshaper());
        harness.addToBattlefield(player1, new AuriokSteelshaper());
        Permanent hoversail = harness.addToBattlefieldAndReturn(player1, new NeurokHoversail());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Frogmite());

        harness.activateAbility(player1, 2, null, creature.getId());
        harness.passBothPriorities();

        assertThat(hoversail.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void opponentSteelshaperDoesNotReduceEquipCost() {
        harness.addToBattlefield(player2, new AuriokSteelshaper());
        Permanent hoversail = harness.addToBattlefieldAndReturn(player1, new NeurokHoversail());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Frogmite());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(hoversail.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void movingEquipmentAwayRemovesSoldierAndKnightBonus() {
        Permanent steelshaper = harness.addToBattlefieldAndReturn(player1, new AuriokSteelshaper());
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new LeoninSkyhunter());
        Permanent hoversail = harness.addToBattlefieldAndReturn(player1, new NeurokHoversail());
        hoversail.setAttachedTo(steelshaper.getId());
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 2, null, knight.getId());
        harness.passBothPriorities();

        assertThat(hoversail.getAttachedTo()).isEqualTo(knight.getId());
        assertThat(gqs.getEffectivePower(gd, steelshaper)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, steelshaper)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
    }

    @Test
    void equipmentControlledByOpponentStillEnablesBonus() {
        Permanent steelshaper = harness.addToBattlefieldAndReturn(player1, new AuriokSteelshaper());
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new LeoninSkyhunter());
        Permanent hoversail = harness.addToBattlefieldAndReturn(player2, new NeurokHoversail());
        hoversail.setAttachedTo(steelshaper.getId());

        assertThat(gqs.getEffectivePower(gd, steelshaper)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, steelshaper)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(3);
    }

    @Test
    void equippedSteelshapersBonusesStackButMultipleEquipmentDoesNotMultiplyBonus() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AuriokSteelshaper());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AuriokSteelshaper());
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new LeoninSkyhunter());
        Permanent firstHoversail = harness.addToBattlefieldAndReturn(player1, new NeurokHoversail());
        Permanent secondHoversail = harness.addToBattlefieldAndReturn(player1, new NeurokHoversail());
        Permanent extraHoversail = harness.addToBattlefieldAndReturn(player1, new NeurokHoversail());
        firstHoversail.setAttachedTo(first.getId());
        secondHoversail.setAttachedTo(second.getId());
        extraHoversail.setAttachedTo(first.getId());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(4);
    }
}
