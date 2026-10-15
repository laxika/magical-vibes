package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.t.TaintedPeak;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Petravark.class, TaintedPeak.class, PardicCollaborator.class})
class PetravarkTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles a target land until Petravark leaves the battlefield")
    void etbExilesTargetLand() {
        Permanent peak = harness.addToBattlefieldAndReturn(player2, new TaintedPeak());

        castAndResolvePetravark(peak.getId());

        harness.assertNotOnBattlefield(player2, "Tainted Peak");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Tainted Peak"));
    }

    @Test
    @DisplayName("The exiled land returns under its owner's control when Petravark leaves")
    void exiledLandReturnsWhenPetravarkLeaves() {
        Permanent peak = harness.addToBattlefieldAndReturn(player2, new TaintedPeak());
        castAndResolvePetravark(peak.getId());
        Permanent petravark = findPermanent(player1, "Petravark");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, petravark));
        harness.assertNotOnBattlefield(player2, "Tainted Peak");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Tainted Peak");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Tainted Peak"));
    }

    @Test
    @DisplayName("The ETB trigger still exiles the land if Petravark leaves before it resolves")
    void etbStillExilesLandAfterPetravarkLeaves() {
        Permanent peak = harness.addToBattlefieldAndReturn(player2, new TaintedPeak());

        harness.setHand(player1, List.of(new Petravark()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, 0, peak.getId());
        harness.passBothPriorities();

        Permanent petravark = findPermanent(player1, "Petravark");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, petravark));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Tainted Peak");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Tainted Peak"));
    }

    @Test
    @DisplayName("Petravark cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        Permanent collaborator = harness.addToBattlefieldAndReturn(player2, new PardicCollaborator());

        harness.setHand(player1, List.of(new Petravark()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, collaborator.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returning Petravark to hand also returns the land, untapped")
    void landReturnsWhenPetravarkIsBounced() {
        Permanent peak = harness.addToBattlefieldAndReturn(player2, new TaintedPeak());
        peak.tap();
        castAndResolvePetravark(peak.getId());
        Permanent petravark = findPermanent(player1, "Petravark");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, petravark));
        harness.assertInHand(player1, "Petravark");
        harness.assertNotOnBattlefield(player2, "Tainted Peak");
        resolveAllTriggers();

        assertThat(findPermanent(player2, "Tainted Peak").isTapped()).isFalse();
    }

    @Test
    @DisplayName("A land controlled by another player returns to its owner")
    void stolenLandReturnsToOwner() {
        TaintedPeak land = new TaintedPeak();
        land.setOwnerId(player2.getId());
        Permanent peak = harness.addToBattlefieldAndReturn(player1, land);
        castAndResolvePetravark(peak.getId());
        Permanent petravark = findPermanent(player1, "Petravark");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, petravark));
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Tainted Peak");
        harness.assertNotOnBattlefield(player1, "Tainted Peak");
    }

    @Test
    @DisplayName("Each Petravark returns only the land it exiled")
    void multiplePetravarksKeepSeparateExiledLands() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player2, new TaintedPeak());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player2, new TaintedPeak());
        castAndResolvePetravark(firstLand.getId());
        Permanent firstPetravark = findPermanent(player1, "Petravark");
        castAndResolvePetravark(secondLand.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, firstPetravark));
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Tainted Peak"))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(firstLand.getCard().getId());
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getId()).containsExactly(secondLand.getCard().getId());
    }

    @Test
    @DisplayName("The enter trigger does nothing if its target land has left the battlefield")
    void targetLeavesBeforeEnterTriggerResolves() {
        Permanent peak = harness.addToBattlefieldAndReturn(player2, new TaintedPeak());
        harness.setHand(player1, List.of(new Petravark()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, 0, peak.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, peak));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Tainted Peak");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        Permanent petravark = findPermanent(player1, "Petravark");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, petravark));
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player2, "Tainted Peak");
        harness.assertInGraveyard(player2, "Tainted Peak");
    }

    private void castAndResolvePetravark(UUID targetId) {
        harness.setHand(player1, List.of(new Petravark()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 0, targetId);
        resolveAllTriggers();
    }
}
