package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SterlingGrove;
import com.github.laxika.magicalvibes.cards.s.SnapcasterMage;
import com.github.laxika.magicalvibes.cards.s.SphereOfResistance;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
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

@CardUsed({Rout.class, RazorfootGriffin.class, SterlingGrove.class, SnapcasterMage.class, SphereOfResistance.class})
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

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Razorfoot Griffin");
        harness.assertNotOnBattlefield(player2, "Razorfoot Griffin");
        harness.assertOnBattlefield(player1, "Sterling Grove");
    }

    @Test
    @DisplayName("Destroyed creatures can't be regenerated")
    void destroyedCreaturesCannotRegenerate() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RazorfootGriffin());
        bears.setRegenerationShield(1);

        harness.setHand(player1, List.of(new Rout()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

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

        harness.castAndResolveSorcery(player1, 0, 0);

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

    @Test
    @DisplayName("Flash casting still pays spell cost increases")
    void flashCastingPaysCostIncreases() {
        harness.addToBattlefield(player2, new SphereOfResistance());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Rout()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Rout");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flash casting requires the full seven mana")
    void flashCastingRequiresFullCost() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Rout()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Rout");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flash casting destroys creatures before a pending creature spell resolves")
    void flashCastingRespondsToCreatureSpell() {
        harness.addToBattlefield(player1, new RazorfootGriffin());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new RazorfootGriffin()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castCreature(player2, 0);

        harness.setHand(player1, List.of(new Rout()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Razorfoot Griffin");
        harness.assertNotOnBattlefield(player2, "Razorfoot Griffin");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Razorfoot Griffin");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can pay the flash surcharge when casting with granted flashback")
    void canFlashbackAtInstantSpeedWithSurcharge() {
        Rout rout = new Rout();
        harness.setGraveyard(player1, List.of(rout));
        harness.setHand(player1, List.of(new SnapcasterMage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(rout.getId()));
        harness.passBothPriorities();

        harness.addToBattlefield(player2, new RazorfootGriffin());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveFlashback(player1, 0, null);

        harness.assertNotOnBattlefield(player1, "Snapcaster Mage");
        harness.assertNotOnBattlefield(player2, "Razorfoot Griffin");
        harness.assertNotInGraveyard(player1, "Rout");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(rout);
    }
}
