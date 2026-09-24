package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SterlingGrove;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Rout.class, RazorfootGriffin.class, SterlingGrove.class})
class RoutTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures and leaves noncreatures untouched")
    void destroysAllCreaturesAndLeavesNoncreaturesUntouched() {
        harness.addToBattlefield(player1, new RazorfootGriffin());
        harness.addToBattlefield(player2, new RazorfootGriffin());
        harness.addToBattlefield(player1, new SterlingGrove());

        harness.setHand(player1, List.of(new Rout()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Razorfoot Griffin");
        harness.assertNotOnBattlefield(player2, "Razorfoot Griffin");
        harness.assertOnBattlefield(player1, "Sterling Grove");
    }

    @Test
    @DisplayName("Destroyed creatures can't be regenerated")
    void destroyedCreaturesCannotRegenerate() {
        Permanent bears = new Permanent(new RazorfootGriffin());
        bears.setRegenerationShield(1);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new Rout()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Razorfoot Griffin");
        harness.assertInGraveyard(player2, "Razorfoot Griffin");
    }

    @Test
    @DisplayName("Does not destroy indestructible creatures")
    void doesNotDestroyIndestructibleCreatures() {
        Permanent indestructibleGriffin = harness.addToBattlefieldAndReturn(player2, new RazorfootGriffin());
        TestCards.mutableCard(indestructibleGriffin)
                .setKeywords(EnumSet.of(Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.INDESTRUCTIBLE));

        harness.setHand(player1, List.of(new Rout()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(indestructibleGriffin);
        harness.assertNotInGraveyard(player2, "Razorfoot Griffin");
    }

    @Test
    @DisplayName("Can be cast at instant speed by paying two more")
    void canBeCastAtInstantSpeedForTwoMore() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new RazorfootGriffin());
        harness.setHand(player1, List.of(new Rout()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Razorfoot Griffin");
    }

    @Test
    @DisplayName("Cannot be cast at instant speed without paying the surcharge")
    void cannotBeCastAtInstantSpeedWithoutSurcharge() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Rout()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
