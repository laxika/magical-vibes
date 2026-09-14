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

    private void castAndResolvePetravark(UUID targetId) {
        harness.setHand(player1, List.of(new Petravark()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
