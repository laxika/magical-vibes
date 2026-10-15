package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
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

@CardUsed({Vesperlark.class, LlanowarElves.class, GrizzlyBears.class, HillGiant.class, Ornithopter.class})
class VesperlarkTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature card with power 1 or less when it leaves")
    void returnsTargetSmallCreatureWhenLeaving() {
        LlanowarElves target = new LlanowarElves();
        harness.setGraveyard(player1, List.of(target));
        Permanent vesperlark = harness.addToBattlefieldAndReturn(player1, new Vesperlark());

        removeVesperlark(vesperlark);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(target.getId());

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Vesperlark");
    }

    @Test
    @DisplayName("Does not target creatures with power greater than 1")
    void filtersByPower() {
        GrizzlyBears bears = new GrizzlyBears();
        HillGiant giant = new HillGiant();
        harness.setGraveyard(player1, List.of(bears, giant));
        Permanent vesperlark = harness.addToBattlefieldAndReturn(player1, new Vesperlark());

        removeVesperlark(vesperlark);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard().getId().equals(bears.getId())
                        || permanent.getCard().getId().equals(giant.getId()));
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Evoke sacrifices Vesperlark and its leave trigger returns a small creature")
    void evokeSacrificesAndReturnsSmallCreature() {
        LlanowarElves target = new LlanowarElves();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new Vesperlark()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vesperlark");
        harness.assertInGraveyard(player1, "Vesperlark");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Casting normally does not sacrifice Vesperlark or return a creature")
    void normalCastDoesNotSacrificeOrReturnCreature() {
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.castFromHand(player1, new Vesperlark(), "{2}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vesperlark");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Llanowar Elves");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Returning Vesperlark to hand also triggers its creature return")
    void returnsCreatureWhenBounced() {
        LlanowarElves target = new LlanowarElves();
        harness.setGraveyard(player1, List.of(target));
        Permanent vesperlark = harness.addToBattlefieldAndReturn(player1, new Vesperlark());

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToHand(gd, vesperlark));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Vesperlark");
        harness.assertNotInGraveyard(player1, "Vesperlark");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Only the controller's graveyard supplies targets and exactly one is required")
    void requiresOneTargetFromControllersGraveyard() {
        LlanowarElves ownTarget = new LlanowarElves();
        LlanowarElves opponentTarget = new LlanowarElves();
        harness.setGraveyard(player1, List.of(ownTarget));
        harness.setGraveyard(player2, List.of(opponentTarget));
        Permanent vesperlark = harness.addToBattlefieldAndReturn(player1, new Vesperlark());

        removeVesperlark(vesperlark);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownTarget.getId());
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of(ownTarget.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("A target removed before resolution is not returned and is not replaced")
    void missingTargetDoesNotReturnAnotherCreature() {
        LlanowarElves target = new LlanowarElves();
        LlanowarElves other = new LlanowarElves();
        harness.setGraveyard(player1, List.of(target, other));
        Permanent vesperlark = harness.addToBattlefieldAndReturn(player1, new Vesperlark());
        removeVesperlark(vesperlark);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removeCardFromGraveyardByIdForExile(gd, target.getId()));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
    @Test
    @DisplayName("A creature card with zero power can be returned")
    void returnsZeroPowerCreature() {
        Ornithopter target = new Ornithopter();
        harness.setGraveyard(player1, List.of(target));
        Permanent vesperlark = harness.addToBattlefieldAndReturn(player1, new Vesperlark());

        removeVesperlark(vesperlark);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertNotInGraveyard(player1, "Ornithopter");
    }
    private void removeVesperlark(Permanent vesperlark) {
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, vesperlark));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
