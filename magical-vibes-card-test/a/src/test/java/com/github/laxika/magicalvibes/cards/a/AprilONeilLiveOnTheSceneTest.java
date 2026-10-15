package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DonatelloMutantMechanic;
import com.github.laxika.magicalvibes.cards.d.DonatelloTurtleTechie;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
import com.github.laxika.magicalvibes.cards.n.NinjaOfTheDeepHours;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        AprilONeilLiveOnTheScene.class,
        DonatelloMutantMechanic.class,
        DonatelloTurtleTechie.class,
        NinjaOfTheDeepHours.class,
        GrizzlyBears.class,
        MaskwoodNexus.class
})
class AprilONeilLiveOnTheSceneTest extends BaseCardTest {

    @Test
    @DisplayName("Investigates when a Mutant, Ninja, or Turtle enters under your control")
    void investigatesForMatchingCreatureTypes() {
        harness.addToBattlefield(player1, new AprilONeilLiveOnTheScene());

        harness.enterBattlefieldAndReturn(player1, new DonatelloMutantMechanic());
        harness.enterBattlefieldAndReturn(player1, new NinjaOfTheDeepHours());
        harness.enterBattlefieldAndReturn(player1, new DonatelloTurtleTechie());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(3);
    }

    @Test
    @DisplayName("Does not investigate for a nonmatching creature or an opponent's creature")
    void ignoresNonmatchingAndOpposingCreatures() {
        harness.addToBattlefield(player1, new AprilONeilLiveOnTheScene());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player2, new DonatelloMutantMechanic());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    void investigatesForItsOwnEntryWithEveryCreatureType() {
        harness.addToBattlefield(player1, new MaskwoodNexus());

        harness.enterBattlefieldAndReturn(player1, new AprilONeilLiveOnTheScene());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void investigatesOnceForAChangelingToken() {
        harness.addToBattlefield(player1, new AprilONeilLiveOnTheScene());
        harness.addToBattlefield(player1, new MaskwoodNexus());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, 0, null, null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Shapeshifter")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void investigatedClueCanBeSacrificedToDrawACard() {
        harness.addToBattlefield(player1, new AprilONeilLiveOnTheScene());
        harness.enterBattlefieldAndReturn(player1, new NinjaOfTheDeepHours());
        resolveAllTriggers();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new AprilONeilLiveOnTheScene()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 2, 0, null, null, null);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        resolveAllTriggers();

        harness.assertInHand(player1, "April O'Neil, Live on the Scene");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
