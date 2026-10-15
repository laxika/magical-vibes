package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlacidRottentail.class, GrizzlyBears.class, Mountain.class})
class PlacidRottentailTest extends BaseCardTest {

    private void readyAbility() {
        harness.setGraveyard(player1, List.of(new PlacidRottentail()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    void putsTwoPlusOnePlusOneCountersOnTargetCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        readyAbility();

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    void exilesItselfAsAnActivationCost() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        readyAbility();

        harness.activateGraveyardAbility(player1, 0, bears.getId());

        harness.assertNotInGraveyard(player1, "Placid Rottentail");
    }

    @Test
    void requiresACreatureTarget() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        readyAbility();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canOnlyBeActivatedAsASorcery() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new PlacidRottentail()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canPutCountersOnAnOpponentsCreature() {
        Permanent target = addCreatureReady(player2, new PlacidRottentail());
        readyAbility();

        harness.activateGraveyardAbility(player1, 0, target.getId());
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void canActivateDuringPostcombatMainPhase() {
        Permanent target = addCreatureReady(player1, new PlacidRottentail());
        readyAbility();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void cannotActivateDuringCombat() {
        Permanent target = addCreatureReady(player1, new PlacidRottentail());
        readyAbility();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Placid Rottentail");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithASpellOnTheStack() {
        Permanent target = addCreatureReady(player1, new PlacidRottentail());
        readyAbility();
        harness.castFromHand(player1, new PlacidRottentail(), "{G}");

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Placid Rottentail");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void cannotActivateWithoutGreenMana() {
        Permanent target = addCreatureReady(player1, new PlacidRottentail());
        harness.setGraveyard(player1, List.of(new PlacidRottentail()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Placid Rottentail");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exilesTheSourceBeforeTheCountersResolve() {
        Permanent target = addCreatureReady(player1, new PlacidRottentail());
        readyAbility();
        var sourceId = gd.playerGraveyards.get(player1.getId()).getFirst().getId();

        harness.activateGraveyardAbility(player1, 0, target.getId());

        assertThat(gd.exiledCards).anySatisfy(entry -> {
            assertThat(entry.card().getId()).isEqualTo(sourceId);
            assertThat(entry.ownerId()).isEqualTo(player1.getId());
            assertThat(entry.faceDown()).isFalse();
        });
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Placid Rottentail");
    }

    @Test
    void cannotActivateWithOnlyTwoMana() {
        Permanent target = addCreatureReady(player1, new PlacidRottentail());
        harness.setGraveyard(player1, List.of(new PlacidRottentail()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Placid Rottentail");
        assertThat(gd.stack).isEmpty();
    }
}
