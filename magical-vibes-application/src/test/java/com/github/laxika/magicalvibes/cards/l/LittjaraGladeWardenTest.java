package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PoisonTheCup;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LittjaraGladeWarden.class, GrizzlyBears.class, Forest.class, PoisonTheCup.class})
class LittjaraGladeWardenTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling a creature card puts two +1/+1 counters on the target creature")
    void exilesCreatureAndPutsCountersOnTarget() {
        Permanent warden = addCreatureReady(player1, new LittjaraGladeWarden());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears graveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        addMana();

        harness.activateAbility(player1, 0, 0, null, bear.getId());
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.GraveyardExileCostChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(warden.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(graveyardCreature);
    }

    @Test
    @DisplayName("Cannot activate outside a main phase")
    void onlyActivatesAtSorcerySpeed() {
        Permanent warden = addCreatureReady(player1, new LittjaraGladeWarden());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        addMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, warden.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new LittjaraGladeWarden());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void canTargetItselfAndPaysCostsBeforeResolution() {
        Permanent warden = addCreatureReady(player1, new LittjaraGladeWarden());
        LittjaraGladeWarden costCard = new LittjaraGladeWarden();
        harness.setGraveyard(player1, List.of(costCard));
        addMana();

        harness.activateAbility(player1, 0, 0, null, warden.getId());
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(warden.isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(costCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(warden.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(warden.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void canTargetAnOpponentsCreature() {
        addCreatureReady(player1, new LittjaraGladeWarden());
        Permanent target = addCreatureReady(player2, new LittjaraGladeWarden());
        harness.setGraveyard(player1, List.of(new LittjaraGladeWarden()));
        addMana();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void cannotUseAnOpponentsGraveyardToPayTheCost() {
        Permanent warden = addCreatureReady(player1, new LittjaraGladeWarden());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(new LittjaraGladeWarden()));
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, warden.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard");

        assertThat(warden.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    void cannotExileANoncreatureWhenChoosingTheCost() {
        Permanent warden = addCreatureReady(player1, new LittjaraGladeWarden());
        Forest land = new Forest();
        LittjaraGladeWarden creature = new LittjaraGladeWarden();
        harness.setGraveyard(player1, List.of(land, creature));
        addMana();

        harness.activateAbility(player1, 0, 0, null, warden.getId());
        PendingInteraction.GraveyardExileCostChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.GraveyardExileCostChoice.class);
        assertThat(choice.validIndices()).containsExactly(1);
        harness.handleGraveyardCardChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature).doesNotContain(land);
        assertThat(warden.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void cannotActivateDuringAnOpponentsMainPhase() {
        Permanent warden = addCreatureReady(player1, new LittjaraGladeWarden());
        harness.setGraveyard(player1, List.of(new LittjaraGladeWarden()));
        addMana();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, warden.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(warden.isTapped()).isFalse();
    }

    @Test
    void cannotActivateWhileTheStackIsNonempty() {
        Permanent warden = addCreatureReady(player1, new LittjaraGladeWarden());
        harness.setGraveyard(player1, List.of(new LittjaraGladeWarden(), new LittjaraGladeWarden()));
        addMana();
        addMana();
        harness.activateAbility(player1, 0, 0, null, warden.getId());
        harness.handleGraveyardCardChosen(player1, 0);
        warden.untap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, warden.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        harness.passBothPriorities();
        assertThat(warden.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotActivateWithSummoningSickness() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new LittjaraGladeWarden());
        harness.setGraveyard(player1, List.of(new LittjaraGladeWarden()));
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, warden.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    void losingTheTargetDoesNotRefundTheExileCost() {
        Permanent warden = addCreatureReady(player1, new LittjaraGladeWarden());
        Permanent target = addCreatureReady(player1, new LittjaraGladeWarden());
        LittjaraGladeWarden costCard = new LittjaraGladeWarden();
        harness.setGraveyard(player1, List.of(costCard));
        addMana();
        harness.setHand(player2, List.of(new PoisonTheCup()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handleGraveyardCardChosen(player1, 0);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(warden).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(costCard);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(warden.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
