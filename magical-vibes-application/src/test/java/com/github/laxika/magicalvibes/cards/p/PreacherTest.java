package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CarnivorousPlant;
import com.github.laxika.magicalvibes.cards.i.IvoryMask;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Preacher.class, CarnivorousPlant.class, IvoryMask.class})
class PreacherTest extends BaseCardTest {

    @Test
    @DisplayName("The chosen opponent chooses a creature they control")
    void opponentChoosesCreatureTheyControl() {
        Permanent preacher = addCreatureReady(player1, new Preacher());
        Permanent ownCreature = addCreatureReady(player1, new CarnivorousPlant());
        Permanent opponentCreature = addCreatureReady(player2, new CarnivorousPlant());

        harness.activateAbility(player1, 0, null, player2.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validPermanentIds()).contains(opponentCreature.getId())
                .doesNotContain(ownCreature.getId());

        harness.handlePermanentChosen(player2, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getId().equals(opponentCreature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(
                permanent -> permanent.getId().equals(opponentCreature.getId()));
        assertThat(preacher.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target its controller as the chosen opponent")
    void chosenPlayerMustBeAnOpponent() {
        addCreatureReady(player1, new Preacher());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability has no effect if Preacher untaps before resolution")
    void noEffectIfPreacherUntapsBeforeResolution() {
        Permanent preacher = addCreatureReady(player1, new Preacher());
        Permanent plant = addCreatureReady(player2, new CarnivorousPlant());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player2, plant.getId());
        preacher.untap();
        preacher.tap();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(
                permanent -> permanent.getId().equals(plant.getId()));
    }

    @Test
    @DisplayName("Control lasts while Preacher remains tapped")
    void controlEndsWhenPreacherUntaps() {
        Permanent preacher = addCreatureReady(player1, new Preacher());
        Permanent plant = addCreatureReady(player2, new CarnivorousPlant());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player2, plant.getId());
        harness.passBothPriorities();

        harness.performUntapStep(player1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(preacher.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(
                permanent -> permanent.getId().equals(plant.getId()));
    }

    @Test
    void mayChooseNotToUntap() {
        Permanent preacher = addCreatureReady(player1, new Preacher());
        Permanent plant = addCreatureReady(player2, new CarnivorousPlant());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player2, plant.getId());
        harness.passBothPriorities();

        harness.performUntapStep(player1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(preacher.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getId().equals(plant.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(
                permanent -> permanent.getId().equals(plant.getId()));
    }

    @Test
    void opponentWithShroudCanStillChooseTheCreatureTarget() {
        addCreatureReady(player1, new Preacher());
        Permanent plant = addCreatureReady(player2, new CarnivorousPlant());
        harness.addToBattlefield(player2, new IvoryMask());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player2, plant.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(plant);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(plant);
    }

    @Test
    void noControlIsGainedIfPreacherLeavesBeforeResolution() {
        Permanent preacher = addCreatureReady(player1, new Preacher());
        Permanent plant = addCreatureReady(player2, new CarnivorousPlant());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player2, plant.getId());
        preacher.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(preacher, plant);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(plant);
    }

    @Test
    void controlEndsWhenPreacherLeavesTheBattlefield() {
        Permanent preacher = addCreatureReady(player1, new Preacher());
        Permanent plant = addCreatureReady(player2, new CarnivorousPlant());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player2, plant.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(plant);

        preacher.setMarkedDamage(1);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(preacher, plant);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(plant);
    }
}
