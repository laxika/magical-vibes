package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.d.Dwindle;
import com.github.laxika.magicalvibes.cards.f.FrilledSeaSerpent;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.t.TitanicGrowth;
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

@CardUsed({GoreclawTerrorOfQalSisma.class, AirElemental.class, GrizzlyBears.class,
        CentaurCourser.class, Divination.class, Dwindle.class, FrilledSeaSerpent.class,
        LightningStrike.class, TitanicGrowth.class})
class GoreclawTerrorOfQalSismaTest extends BaseCardTest {

    @Test
    @DisplayName("Creature spells with power 4 or greater cost {2} less")
    void reducesHighPowerCreatureSpells() {
        harness.addToBattlefield(player1, new GoreclawTerrorOfQalSisma());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Air Elemental");
    }

    @Test
    @DisplayName("Creature spells with power less than 4 are not reduced")
    void doesNotReduceLowPowerCreatureSpells() {
        harness.addToBattlefield(player1, new GoreclawTerrorOfQalSisma());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attacking boosts and grants trample to qualifying creatures you control")
    void attackBoostsQualifyingCreatures() {
        Permanent goreclaw = addCreatureReady(player1, new GoreclawTerrorOfQalSisma());
        Permanent airElemental = addCreatureReady(player1, new AirElemental());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent enemy = addCreatureReady(player2, new AirElemental());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, goreclaw)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, goreclaw)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, goreclaw, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, airElemental)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, enemy)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, enemy, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Attack boost and trample wear off at end of turn")
    void attackBoostAndTrampleWearOffAtEndOfTurn() {
        addCreatureReady(player1, new GoreclawTerrorOfQalSisma());
        Permanent airElemental = addCreatureReady(player1, new AirElemental());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, airElemental)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Power-three creatures receive neither bonus")
    void powerThreeCreatureDoesNotGainTrample() {
        addCreatureReady(player1, new GoreclawTerrorOfQalSisma());
        Permanent courser = addCreatureReady(player1, new CentaurCourser());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, courser)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, courser)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, courser, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Generic cost reduction does not pay colored mana requirements")
    void reductionDoesNotReplaceColoredMana() {
        harness.addToBattlefield(player1, new GoreclawTerrorOfQalSisma());
        harness.setHand(player1, List.of(new FrilledSeaSerpent()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Power-four creature costs exactly two generic mana less")
    void reductionStillRequiresRemainingGenericMana() {
        harness.addToBattlefield(player1, new GoreclawTerrorOfQalSisma());
        harness.setHand(player1, List.of(new FrilledSeaSerpent()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's Goreclaw does not reduce your spells")
    void opponentDoesNotReceiveCostReduction() {
        harness.addToBattlefield(player2, new GoreclawTerrorOfQalSisma());
        harness.setHand(player1, List.of(new FrilledSeaSerpent()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Noncreature spells are not discounted")
    void doesNotReduceNoncreatureSpells() {
        harness.addToBattlefield(player1, new GoreclawTerrorOfQalSisma());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attacking with another creature does not trigger Goreclaw")
    void onlyGoreclawAttackingTriggersAbility() {
        Permanent goreclaw = addCreatureReady(player1, new GoreclawTerrorOfQalSisma());
        Permanent serpent = addCreatureReady(player1, new FrilledSeaSerpent());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, goreclaw)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, serpent)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, goreclaw, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, serpent, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Power is checked when the attack trigger resolves")
    void creaturePumpedInResponseQualifies() {
        addCreatureReady(player1, new GoreclawTerrorOfQalSisma());
        Permanent courser = addCreatureReady(player1, new CentaurCourser());
        harness.setHand(player1, List.of(new TitanicGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.castInstant(player1, 0, courser.getId());
            resolveAllTriggers();
        });

        assertThat(gqs.getEffectivePower(gd, courser)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, courser)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, courser, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The attack trigger still resolves after Goreclaw leaves")
    void attackTriggerResolvesWithoutSource() {
        Permanent goreclaw = addCreatureReady(player1, new GoreclawTerrorOfQalSisma());
        Permanent serpent = addCreatureReady(player1, new FrilledSeaSerpent());
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.castInstant(player1, 0, goreclaw.getId());
            resolveAllTriggers();
        });

        harness.assertInGraveyard(player1, "Goreclaw, Terror of Qal Sisma");
        assertThat(gqs.getEffectivePower(gd, serpent)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, serpent)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, serpent, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Bonuses persist after power decreases and do not affect later creatures")
    void recipientsAreFixedAtResolution() {
        addCreatureReady(player1, new GoreclawTerrorOfQalSisma());
        Permanent serpent = addCreatureReady(player1, new FrilledSeaSerpent());
        harness.setHand(player1, List.of(new Dwindle()));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, () -> {
            harness.castEnchantment(player1, 0, serpent.getId());
            resolveAllTriggers();
        });
        Permanent lateSerpent = harness.addToBattlefieldAndReturn(player1, new FrilledSeaSerpent());

        assertThat(gqs.getEffectivePower(gd, serpent)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, serpent)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, serpent, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, lateSerpent)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, lateSerpent)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, lateSerpent, Keyword.TRAMPLE)).isFalse();
    }
}
