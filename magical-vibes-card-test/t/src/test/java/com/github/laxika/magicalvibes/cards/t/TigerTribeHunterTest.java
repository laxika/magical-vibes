package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TigerTribeHunter.class, GrizzlyBears.class, HillGiant.class})
class TigerTribeHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Declining pack tactics sacrifices nothing and creates no damage trigger")
    void mayDeclineSacrifice() {
        Permanent hunter = addCreatureReady(player1, new TigerTribeHunter());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hunter, bear);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Only other creatures controlled by the hunter's controller can be sacrificed")
    void offersOnlyOtherControlledCreaturesForSacrifice() {
        Permanent hunter = addCreatureReady(player1, new TigerTribeHunter());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new HillGiant());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactly(bear.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(hunter.getId());
    }

    @Test
    @DisplayName("Pack tactics still resolves after attacking power drops below six")
    void qualifyingAttackIsNotRecheckedAtResolution() {
        addCreatureReady(player1, new TigerTribeHunter());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        bear.setPowerModifier(-2);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
    }

    @Test
    @DisplayName("The sacrifice uses effective power and damage waits for its own trigger")
    void capturesModifiedPowerAndDealsDamageFromSeparateTrigger() {
        Permanent hunter = addCreatureReady(player1, new TigerTribeHunter());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent sacrificed = addCreatureReady(player1, new GrizzlyBears());
        sacrificed.setPowerModifier(1);

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.handlePermanentChosen(player1, hunter.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrificed);
        assertThat(hunter.getMarkedDamage()).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(hunter.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hunter);
    }

    @Test
    @DisplayName("A nonattacking hunter does not trigger even when other attackers qualify")
    void mustAttackToTrigger() {
        addCreatureReady(player1, new TigerTribeHunter());
        addCreatureReady(player1, new HillGiant());
        addCreatureReady(player1, new HillGiant());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Pack tactics sacrifices another creature and deals its power to a creature")
    void sacrificesAnotherCreatureAndDealsItsPowerToCreature() {
        addCreatureReady(player1, new TigerTribeHunter());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent sacrificed = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new HillGiant());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrificed);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Pack tactics does not trigger below total attacking power six")
    void doesNotTriggerBelowThreshold() {
        addCreatureReady(player1, new TigerTribeHunter());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The reflexive trigger offers only creature targets")
    void offersOnlyCreatureTargets() {
        addCreatureReady(player1, new TigerTribeHunter());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent sacrificed = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new HillGiant());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrificed.getId());

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).contains(target.getId());
        assertThat(choice.validPlayerIds()).isEmpty();
    }
}
