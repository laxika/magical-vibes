package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DwarvenPony;
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

@CardUsed({TheLadyOfOtaria.class, DwarvenPony.class, Forest.class,
        GrizzlyBears.class, ZuranOrb.class})
class TheLadyOfOtariaTest extends BaseCardTest {

    @Test
    @DisplayName("can be cast by tapping three Dwarves and recruits Dwarves after a land dies")
    void alternateCostAndEndStepAbility() {
        Permanent dwarf1 = harness.addToBattlefieldAndReturn(player1, new DwarvenPony());
        Permanent dwarf2 = harness.addToBattlefieldAndReturn(player1, new DwarvenPony());
        Permanent dwarf3 = harness.addToBattlefieldAndReturn(player1, new DwarvenPony());
        Permanent orb = harness.addToBattlefieldAndReturn(player1, new ZuranOrb());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        Card libraryDwarf1 = new DwarvenPony();
        Card libraryDwarf2 = new DwarvenPony();
        Card nonDwarfCreature = new GrizzlyBears();
        Card nonDwarfLand = new Forest();
        setLibrary(libraryDwarf1, nonDwarfCreature, libraryDwarf2, nonDwarfLand);
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
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land.getCard());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(libraryDwarf1.getId(), libraryDwarf2.getId());
        assertThat(choice.maxCount()).isEqualTo(4);

        harness.handleMultipleCardsChosen(player1, List.of(libraryDwarf1.getId(), libraryDwarf2.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(libraryDwarf1, libraryDwarf2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                nonDwarfCreature, nonDwarfLand);
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
