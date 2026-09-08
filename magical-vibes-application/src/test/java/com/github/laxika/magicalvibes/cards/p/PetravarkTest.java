package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({Petravark.class, Forest.class, GrizzlyBears.class})
class PetravarkTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles a target land until Petravark leaves the battlefield")
    void etbExilesTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        castAndResolvePetravark(forest.getId());

        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Forest"));
    }

    @Test
    @DisplayName("The exiled land returns under its owner's control when Petravark leaves")
    void exiledLandReturnsWhenPetravarkLeaves() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        castAndResolvePetravark(forest.getId());
        Permanent petravark = findPermanent(player1, "Petravark");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, petravark));

        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Petravark cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Petravark()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, bears.getId()))
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
