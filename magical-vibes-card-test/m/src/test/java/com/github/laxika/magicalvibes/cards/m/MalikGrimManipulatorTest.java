package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.f.ForbiddingWatchtower;
import com.github.laxika.magicalvibes.cards.n.NantukoHusk;
import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Malik, Grim Manipulator")
@CardUsed({MalikGrimManipulator.class, GrizzlyBears.class, HillGiant.class,
        NantukoHusk.class, ForbiddingWatchtower.class, CruelEdict.class})
class MalikGrimManipulatorTest extends BaseCardTest {

    @Test
    @DisplayName("Both players secretly choose opposing creatures and each sacrifice creates a Treasure")
    void bothPlayersChooseBeforeSacrifice() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent controllerChosen = addCreatureReady(player2, new HillGiant());
        Permanent opponentChosen = addCreatureReady(player2, new GrizzlyBears());

        castMalik(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.context())
                .isInstanceOf(MultiPermanentChoiceContext.ControllerAndTargetPlayerChooseCreaturesThenSacrifice.class);

        assertThat(firstChoice.validIds()).containsExactlyInAnyOrder(
                controllerChosen.getId(), opponentChosen.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(controllerChosen.getId()));
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        assertThat(secondChoice.validIds()).containsExactlyInAnyOrder(
                controllerChosen.getId(), opponentChosen.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(opponentChosen.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Malik, Grim Manipulator");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
    }

    @Test
    void choosingTheSameCreatureSacrificesItOnlyOnce() {
        Permanent chosen = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new HillGiant());

        castMalik(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(chosen.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Malik, Grim Manipulator");
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void opponentWithNoCreaturesDoesNotCauseControllerToSacrifice() {
        addCreatureReady(player1, new GrizzlyBears());

        castMalik(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Malik, Grim Manipulator");
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    void opponentWithOneCreatureSacrificesItOnceAndControllerKeepsMalik() {
        addCreatureReady(player2, new GrizzlyBears());

        castMalik(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Malik, Grim Manipulator");
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void opponentSacrificingAnimatedLandCreatesTreasure() {
        harness.addToBattlefield(player1, new MalikGrimManipulator());
        addCreatureReady(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new ForbiddingWatchtower());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.activateAbility(player2, 1, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, land.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Forbidding Watchtower");
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void opponentSacrificingCreatureAsCostCreatesTreasure() {
        harness.addToBattlefield(player1, new MalikGrimManipulator());
        addCreatureReady(player2, new NantukoHusk());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player2, 0, null, null);
        harness.handlePermanentChosen(player2, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void controllerSacrificingCreatureDoesNotCreateTreasure() {
        harness.addToBattlefield(player1, new MalikGrimManipulator());
        addCreatureReady(player1, new NantukoHusk());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    @DisplayName("The enter-the-battlefield ability cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new MalikGrimManipulator()));
        addMalikMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private void castMalik(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new MalikGrimManipulator()));
        addMalikMana();
        harness.castCreature(player1, 0, targetId);
    }

    private void addMalikMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
