package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.r.RootMaze;
import com.github.laxika.magicalvibes.cards.r.RampantGrowth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchelosLagoonMystic.class, Forest.class, GrizzlyBears.class, RootMaze.class,
        Humble.class, RampantGrowth.class})
class ArchelosLagoonMysticTest extends BaseCardTest {

    @Test
    void otherPermanentsEnterTappedWhileArchelosIsTapped() {
        Permanent archelos = harness.addToBattlefieldAndReturn(player1, new ArchelosLagoonMystic());
        archelos.tap();
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isTrue();
    }

    @Test
    void controllerChoosesEntryStateWhenUntappedArchelosConflictsWithRootMaze() {
        harness.addToBattlefield(player1, new RootMaze());
        harness.addToBattlefield(player1, new ArchelosLagoonMystic());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handleListChoice(player1, "Tapped");
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
    }

    @Test
    void tappedArchelosAffectsPermanentsEnteringUnderAnOpponentsControl() {
        Permanent archelos = harness.addToBattlefieldAndReturn(player1, new ArchelosLagoonMystic());
        archelos.tap();
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player2, 0);

        assertThat(findPermanent(player2, "Forest").isTapped()).isTrue();
    }

    @Test
    void enteringArchelosDoesNotApplyItsOwnUntappedEffect() {
        Permanent existingArchelos = harness.addToBattlefieldAndReturn(player1, new ArchelosLagoonMystic());
        existingArchelos.tap();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new ArchelosLagoonMystic(), "{1}{B}{G}{U}");
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Archelos, Lagoon Mystic").isTapped()).isTrue();
    }

    @Test
    void opposingTappedAndUntappedArchelosAllowControllerToChooseEntryState() {
        Permanent tappedArchelos = harness.addToBattlefieldAndReturn(player1, new ArchelosLagoonMystic());
        tappedArchelos.tap();
        harness.addToBattlefield(player2, new ArchelosLagoonMystic());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handleListChoice(player1, "Untapped");
        assertThat(findPermanent(player1, "Forest").isTapped()).isFalse();
    }

    @Test
    void untappingArchelosChangesHowLaterPermanentsEnter() {
        Permanent archelos = harness.addToBattlefieldAndReturn(player1, new ArchelosLagoonMystic());
        archelos.tap();
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        archelos.untap();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears"))
                .extracting(Permanent::isTapped).containsExactly(true, false);
    }

    @Test
    void tappedArchelosStopsAffectingEntryAfterLosingItsAbilities() {
        Permanent archelos = harness.addToBattlefieldAndReturn(player1, new ArchelosLagoonMystic());
        archelos.tap();
        harness.setHand(player1, List.of(new Humble(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, archelos.getId());
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isFalse();
    }

    @Test
    void untappedArchelosStopsOverridingRootMazeAfterLosingItsAbilities() {
        harness.addToBattlefield(player1, new RootMaze());
        Permanent archelos = harness.addToBattlefieldAndReturn(player1, new ArchelosLagoonMystic());
        harness.setHand(player1, List.of(new Humble(), new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, archelos.getId());
        harness.passBothPriorities();
        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
    }

    @Test
    void untappedArchelosOverridesAnInstructionToPutALandOntoBattlefieldTapped() {
        harness.addToBattlefield(player1, new ArchelosLagoonMystic());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new RampantGrowth(), "{1}{G}");

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isFalse();
    }
}
