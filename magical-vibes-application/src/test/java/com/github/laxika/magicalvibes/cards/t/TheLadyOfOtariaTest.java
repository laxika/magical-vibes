package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DwarvenGrunt;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({TheLadyOfOtaria.class, DwarvenGrunt.class, Forest.class,
        GrizzlyBears.class, ZuranOrb.class})
class TheLadyOfOtariaTest extends BaseCardTest {

    @Test
    @DisplayName("can be cast by tapping three Dwarves and recruits Dwarves after a land dies")
    void alternateCostAndEndStepAbility() {
        Permanent dwarf1 = harness.addToBattlefieldAndReturn(player1, new DwarvenGrunt());
        Permanent dwarf2 = harness.addToBattlefieldAndReturn(player1, new DwarvenGrunt());
        Permanent dwarf3 = harness.addToBattlefieldAndReturn(player1, new DwarvenGrunt());
        Permanent orb = harness.addToBattlefieldAndReturn(player1, new ZuranOrb());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        Card libraryDwarf1 = new DwarvenGrunt();
        Card libraryDwarf2 = new DwarvenGrunt();
        Card nonDwarfCreature = new GrizzlyBears();
        Card nonDwarfLand = new Forest();
        harness.setLibrary(player1, List.of(libraryDwarf1, nonDwarfCreature, libraryDwarf2, nonDwarfLand));
        harness.setHand(player1, List.of(new TheLadyOfOtaria()));

        harness.castCreatureWithAlternateCost(player1, 0,
                List.of(dwarf1.getId(), dwarf2.getId(), dwarf3.getId()));
        harness.passBothPriorities();

        assertThat(dwarf1.isTapped()).isTrue();
        assertThat(dwarf2.isTapped()).isTrue();
        assertThat(dwarf3.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "The Lady of Otaria");

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(orb), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land.getCard());

        harness.forceActivePlayer(player1);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(libraryDwarf1.getId(), libraryDwarf2.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(libraryDwarf1.getId(), libraryDwarf2.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(libraryDwarf1, libraryDwarf2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                nonDwarfCreature, nonDwarfLand);
    }

    @Test
    void canPayManaWithoutDwarves() {
        harness.castFromHand(player1, new TheLadyOfOtaria(), "{3}{R}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "The Lady of Otaria");
    }

    @Test
    void cannotTapTheSameDwarfThreeTimes() {
        Permanent dwarf = harness.addToBattlefieldAndReturn(player1, new DwarvenGrunt());
        harness.addToBattlefield(player1, new DwarvenGrunt());
        harness.addToBattlefield(player1, new DwarvenGrunt());
        harness.setHand(player1, List.of(new TheLadyOfOtaria()));

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0,
                List.of(dwarf.getId(), dwarf.getId(), dwarf.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(dwarf.isTapped()).isFalse();
        harness.assertInHand(player1, "The Lady of Otaria");
    }

    @Test
    void doesNotTriggerWithoutALandGoingToGraveyard() {
        harness.addToBattlefield(player1, new TheLadyOfOtaria());
        Card dwarf = new DwarvenGrunt();
        harness.setLibrary(player1, List.of(dwarf));
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(dwarf);
    }

    @Test
    void opponentsLandDoesNotEnableAbility() {
        harness.addToBattlefield(player1, new TheLadyOfOtaria());
        harness.addToBattlefield(player2, new ZuranOrb());
        harness.addToBattlefield(player2, new Forest());
        Card dwarf = new DwarvenGrunt();
        harness.setLibrary(player1, List.of(dwarf));
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(dwarf);
    }

    @Test
    void canDeclineDwarvesDuringOpponentsEndStepWithShortLibrary() {
        harness.addToBattlefield(player1, new TheLadyOfOtaria());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());
        Card dwarf = new DwarvenGrunt();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(dwarf, forest));
        harness.forceActivePlayer(player2);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(dwarf.getId());
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(dwarf);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(dwarf, forest);
    }

    @Test
    void canChooseOnlySomeDwarvesAndOnlyTopFourCardsAreRevealed() {
        harness.addToBattlefield(player1, new TheLadyOfOtaria());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());
        Card chosen = new DwarvenGrunt();
        Card declined = new DwarvenGrunt();
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card fifth = new DwarvenGrunt();
        harness.setLibrary(player1, List.of(chosen, declined, forest, bears, fifth));
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(chosen.getId(), declined.getId());
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen).doesNotContain(declined, fifth);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4).startsWith(fifth);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(fifth, declined, forest, bears);
    }

    @Test
    void cannotUseATappedDwarfForAlternateCost() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DwarvenGrunt());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new DwarvenGrunt());
        Permanent tapped = harness.addToBattlefieldAndReturn(player1, new DwarvenGrunt());
        tapped.tap();
        harness.setHand(player1, List.of(new TheLadyOfOtaria()));

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0,
                List.of(first.getId(), second.getId(), tapped.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        harness.assertInHand(player1, "The Lady of Otaria");
    }

    @Test
    void cannotUseOpponentsDwarfForAlternateCost() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DwarvenGrunt());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new DwarvenGrunt());
        Permanent opponentDwarf = harness.addToBattlefieldAndReturn(player2, new DwarvenGrunt());
        harness.setHand(player1, List.of(new TheLadyOfOtaria()));

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0,
                List.of(first.getId(), second.getId(), opponentDwarf.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(opponentDwarf.isTapped()).isFalse();
        harness.assertInHand(player1, "The Lady of Otaria");
    }

    @Test
    void cannotUseNonDwarfForAlternateCost() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DwarvenGrunt());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new DwarvenGrunt());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TheLadyOfOtaria()));

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0,
                List.of(first.getId(), second.getId(), bears.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bears.isTapped()).isFalse();
        harness.assertInHand(player1, "The Lady of Otaria");
    }

    @Test
    void landMayHaveDiedBeforeLadyEnteredAndNoDwarvesAreRequiredInLibrary() {
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.castFromHand(player1, new TheLadyOfOtaria(), "{3}{R}{G}");
        harness.passBothPriorities();
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, bears));
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, bears);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest, bears);
    }
}
