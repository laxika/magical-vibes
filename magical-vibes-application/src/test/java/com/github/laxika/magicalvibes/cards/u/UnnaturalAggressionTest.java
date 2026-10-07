package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed({UnnaturalAggression.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class, Shock.class})
class UnnaturalAggressionTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an opposing creature killed by the fight")
    void exilesOpposingCreatureKilledByFight() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new UnnaturalAggression()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertNotInGraveyard(player2, "Llanowar Elves");
        GameData gd = harness.getGameData();
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(opposingCreature.getCard().getId()));
    }

    @Test
    @DisplayName("Marks a surviving opposing creature for exile if it dies later this turn")
    void marksSurvivingOpposingCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new UnnaturalAggression(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));

        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(opposingCreature.isExileInsteadOfDieThisTurn()).isTrue();

        harness.castAndResolveInstant(player1, 0, opposingCreature.getId());

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertNotInGraveyard(player2, "Hill Giant");
        GameData gd = harness.getGameData();
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(opposingCreature.getCard().getId()));
    }

    @Test
    @DisplayName("Does not mark the creature you control for exile")
    void doesNotMarkOwnCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UnnaturalAggression()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Requires the first target to be a creature you control")
    void requiresControlledCreatureAsFirstTarget() {
        Permanent firstOpponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondOpponentCreature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new UnnaturalAggression()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(firstOpponentCreature.getId(), secondOpponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Rejects a creature you control as the second target")
    void rejectsControlledSecondTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new UnnaturalAggression()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Still exiles a later death when the controlled target is removed before resolution")
    void replacementAppliesWhenControlledTargetIsGone() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new UnnaturalAggression(), new Shock()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, List.of(own.getId(), opponent.getId()));
        harness.castAndResolveInstant(player2, 0, own.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(opponent.getMarkedDamage()).isZero();
        harness.castAndResolveInstant(player1, 0, opponent.getId());
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertNotInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(opponent.getCard().getId()));
    }

    @Test
    @DisplayName("Does not fight or exile the controlled creature when the opposing target is removed")
    void opposingTargetGonePreventsFight() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new UnnaturalAggression()));
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player1, 0, List.of(own.getId(), opponent.getId()));
        harness.castAndResolveInstant(player2, 0, opponent.getId());
        harness.passBothPriorities();

        assertThat(own.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.castAndResolveInstant(player2, 0, own.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(own.getCard().getId()));
    }

    @Test
    @DisplayName("The exile replacement expires at the end of the turn")
    void exileReplacementExpires() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new UnnaturalAggression()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, List.of(own.getId(), opponent.getId()));

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, opponent.getId());
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.castAndResolveInstant(player2, 0, opponent.getId());

        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(opponent.getCard().getId()));
    }
}
