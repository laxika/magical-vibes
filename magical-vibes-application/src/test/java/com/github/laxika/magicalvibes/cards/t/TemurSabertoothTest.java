package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({TemurSabertooth.class, GrizzlyBears.class, Millstone.class})
class TemurSabertoothTest extends BaseCardTest {

    @Test
    @DisplayName("Returns another creature you control and gains indestructible")
    void returnsAnotherCreatureAndGainsIndestructible() {
        Permanent sabertooth = harness.addToBattlefieldAndReturn(player1, new TemurSabertooth());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Millstone());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(creature.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, sabertooth, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertOnBattlefield(player1, "Millstone");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not gain indestructible when no creature is returned")
    void doesNotGainIndestructibleWhenNoCreatureIsReturned() {
        Permanent sabertooth = harness.addToBattlefieldAndReturn(player1, new TemurSabertooth());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gqs.hasKeyword(gd, sabertooth, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Can activate with no other creature and gains no indestructible")
    void canActivateWithNoOtherCreature() {
        Permanent sabertooth = harness.addToBattlefieldAndReturn(player1, new TemurSabertooth());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.hasKeyword(gd, sabertooth, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.assertOnBattlefield(player1, "Temur Sabertooth");
    }

    @Test
    @DisplayName("Can return another Temur Sabertooth and grants the source indestructible")
    void canReturnAnotherCopy() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TemurSabertooth());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new TemurSabertooth());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(other.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(other.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(source);
        harness.assertInHand(player1, "Temur Sabertooth");
        assertThat(gqs.hasKeyword(gd, source, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Returns an opponent-owned creature to its owner's hand")
    void returnsCreatureToOwnersHand() {
        Permanent sabertooth = harness.addToBattlefieldAndReturn(player1, new TemurSabertooth());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerBattlefields.get(player1.getId()).add(creature);
        gd.stolenCreatures.put(creature.getId(), player2.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, sabertooth, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Indestructible expires at the end of the turn")
    void indestructibleExpiresAtEndOfTurn() {
        Permanent sabertooth = harness.addToBattlefieldAndReturn(player1, new TemurSabertooth());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));
        assertThat(gqs.hasKeyword(gd, sabertooth, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, sabertooth, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Chooses the creature during resolution rather than activation")
    void choosesCreatureDuringResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TemurSabertooth());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        Permanent other = harness.addToBattlefieldAndReturn(player1, new TemurSabertooth());
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(other.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(other.getId()));

        harness.assertInHand(player1, "Temur Sabertooth");
        assertThat(gqs.hasKeyword(gd, source, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
