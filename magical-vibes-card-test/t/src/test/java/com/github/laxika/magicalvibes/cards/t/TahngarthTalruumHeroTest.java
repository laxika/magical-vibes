package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AngrathsMarauders;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TahngarthTalruumHero.class, CrawWurm.class, Forest.class, GrizzlyBears.class,
        AngrathsMarauders.class})
class TahngarthTalruumHeroTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to its power and receives damage equal to the target's power")
    void dealsReciprocalPowerDamage() {
        Permanent tahngarth = addReadyTahngarth(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(tahngarth.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Both creatures are destroyed when reciprocal damage is lethal")
    void bothCreaturesDieFromReciprocalDamage() {
        Permanent tahngarth = addReadyTahngarth(player1);
        Permanent target = addCreatureReady(player2, new CrawWurm());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tahngarth);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Still deals first damage using its last known power if it leaves before resolution")
    void sourceLeavingBeforeResolutionStillDealsDamage() {
        Permanent tahngarth = addReadyTahngarth(player1);
        Permanent target = addCreatureReady(player2, new CrawWurm());
        target.setToughnessModifier(1); // Keep the four damage nonlethal so it can be inspected.
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(tahngarth);
        harness.withAutoStop(gd.currentStep, harness::passBothPriorities);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addReadyTahngarth(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate the tap ability while tapped")
    void cannotActivateWhileTapped() {
        addReadyTahngarth(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        Permanent secondTarget = addCreatureReady(player2, new GrizzlyBears());
        addAbilityMana();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, secondTarget.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tahngarth's controller's damage doubler does not double the opponent's return damage")
    void controllerDamageDoublerOnlyDoublesTahngarthDamage() {
        Permanent tahngarth = addReadyTahngarth(player1);
        harness.addToBattlefield(player1, new AngrathsMarauders());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.withAutoStop(gd.currentStep, harness::passBothPriorities);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Tahngarth, Talruum Hero");
        assertThat(tahngarth.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("The target's controller's damage doubler doubles the target's return damage")
    void opponentDamageDoublerDoublesReturnDamage() {
        addReadyTahngarth(player1);
        harness.addToBattlefield(player2, new AngrathsMarauders());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.withAutoStop(gd.currentStep, harness::passBothPriorities);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Tahngarth, Talruum Hero");
    }

    @Test
    @DisplayName("Can target a creature controlled by Tahngarth's controller")
    void canTargetOwnCreature() {
        Permanent tahngarth = addReadyTahngarth(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.withAutoStop(gd.currentStep, harness::passBothPriorities);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(tahngarth.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target itself and deals both damage instructions to itself")
    void canTargetItself() {
        Permanent tahngarth = addReadyTahngarth(player1);
        tahngarth.setToughnessModifier(5);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, tahngarth.getId());
        harness.withAutoStop(gd.currentStep, harness::passBothPriorities);

        harness.assertOnBattlefield(player1, "Tahngarth, Talruum Hero");
        assertThat(tahngarth.getMarkedDamage()).isEqualTo(8);
    }

    @Test
    @DisplayName("An absent target causes the ability to resolve without dealing damage")
    void targetLeavingBeforeResolutionPreventsAllDamage() {
        Permanent tahngarth = addReadyTahngarth(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.withAutoStop(gd.currentStep, harness::passBothPriorities);

        assertThat(tahngarth.getMarkedDamage()).isZero();
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Uses Tahngarth's current power when the ability resolves")
    void usesCurrentPowerAtResolution() {
        Permanent tahngarth = addReadyTahngarth(player1);
        Permanent target = addCreatureReady(player2, new CrawWurm());
        target.setToughnessModifier(5);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        tahngarth.setPowerModifier(2);
        harness.withAutoStop(gd.currentStep, harness::passBothPriorities);

        assertThat(target.getMarkedDamage()).isEqualTo(6);
        harness.assertInGraveyard(player1, "Tahngarth, Talruum Hero");
    }

    @Test
    @DisplayName("Summoning sickness prevents activation of the tap ability")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new TahngarthTalruumHero());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Generic mana alone cannot pay the red part of the activation cost")
    void cannotActivateWithoutRedMana() {
        Permanent tahngarth = addReadyTahngarth(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(tahngarth.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Vigilance allows Tahngarth to attack and then activate its tap ability")
    void canActivateAfterAttacking() {
        Permanent tahngarth = addReadyTahngarth(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addAbilityMana();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(tahngarth.isTapped()).isFalse();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.withAutoStop(gd.currentStep, harness::passBothPriorities);

        assertThat(tahngarth.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(tahngarth.getMarkedDamage()).isEqualTo(2);
    }

    private Permanent addReadyTahngarth(Player player) {
        return addCreatureReady(player, new TahngarthTalruumHero());
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
