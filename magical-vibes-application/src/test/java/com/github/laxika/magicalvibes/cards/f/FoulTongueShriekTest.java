package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mindcrank;
import com.github.laxika.magicalvibes.cards.p.PlatinumEmperion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FoulTongueShriek.class, GrizzlyBears.class, Flatten.class, Mindcrank.class,
        PlatinumEmperion.class})
class FoulTongueShriekTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent loses and you gain life for each attacking creature")
    void drainsForEachAttackingCreature() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FoulTongueShriek()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)));

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Does not count creatures you control that are not attacking")
    void doesNotCountNonAttackingCreatures() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FoulTongueShriek()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0)));

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new FoulTongueShriek()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("No attackers means neither player gains or loses life")
    void doesNothingWithoutAttackers() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FoulTongueShriek()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Foul-Tongue Shriek");
    }

    @Test
    @DisplayName("Counts attackers at resolution after one is removed in response")
    void countsAttackersAtResolution() {
        var removedAttacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FoulTongueShriek()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setHand(player2, List.of(new Flatten()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0, 1)));

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, removedAttacker.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Does not gain life when the opponent cannot lose life")
    void gainsOnlyLifeActuallyLost() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new PlatinumEmperion());
        harness.setHand(player1, List.of(new FoulTongueShriek()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Life loss triggers Mindcrank and is recorded for the turn")
    void lifeLossTriggersAndIsRecorded() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Mindcrank());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new FoulTongueShriek()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.lifeLostThisTurn.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }
}
