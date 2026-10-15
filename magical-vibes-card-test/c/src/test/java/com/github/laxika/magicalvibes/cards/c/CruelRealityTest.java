package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AssaultSuit;
import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LilianaDeathsMajesty;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CruelReality.class, DuneBeetle.class, LilianaDeathsMajesty.class, Forest.class, AssaultSuit.class})
class CruelRealityTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted player sacrifices their only creature at upkeep and loses no life")
    void sacrificesOnlyCreature() {
        placeCurseOnPlayer(player1, player2);
        harness.addToBattlefield(player2, new DuneBeetle());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        harness.assertNotOnBattlefield(player2, "Dune Beetle");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Enchanted player sacrifices their only planeswalker at upkeep")
    void sacrificesOnlyPlaneswalker() {
        placeCurseOnPlayer(player1, player2);
        Permanent liliana = harness.addToBattlefieldAndReturn(player2, new LilianaDeathsMajesty());
        liliana.setCounterCount(CounterType.LOYALTY, 5);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        harness.assertNotOnBattlefield(player2, "Liliana, Death's Majesty");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Enchanted player with no creature or planeswalker loses 5 life")
    void losesFiveLifeWhenNothingToSacrifice() {
        placeCurseOnPlayer(player1, player2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 5);
    }

    @Test
    @DisplayName("A noncreature, nonplaneswalker permanent does not count — player loses 5 life")
    void landDoesNotSatisfySacrifice() {
        placeCurseOnPlayer(player1, player2);
        harness.addToBattlefield(player2, new Forest());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        // The land is not eligible, so nothing is sacrificed and the fallback life loss applies.
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 5);
    }

    @Test
    @DisplayName("Trigger does NOT fire during the curse controller's upkeep")
    void triggerDoesNotFireOnControllerUpkeep() {
        placeCurseOnPlayer(player1, player2);
        harness.addToBattlefield(player1, new DuneBeetle());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dune Beetle");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Enchanted player chooses a planeswalker rather than a creature")
    void choosesPlaneswalkerInsteadOfCreature() {
        placeCurseOnPlayer(player1, player2);
        harness.addToBattlefield(player2, new DuneBeetle());
        Permanent liliana = harness.addToBattlefieldAndReturn(player2, new LilianaDeathsMajesty());
        liliana.setCounterCount(CounterType.LOYALTY, 5);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, liliana.getId());

        harness.assertOnBattlefield(player2, "Dune Beetle");
        harness.assertNotOnBattlefield(player2, "Liliana, Death's Majesty");
        harness.assertInGraveyard(player2, "Liliana, Death's Majesty");
        harness.assertLife(player2, lifeBefore);
    }

    @Test
    @DisplayName("Player loses 5 life when their only creature cannot be sacrificed")
    void losesLifeWhenOnlyCreatureCannotBeSacrificed() {
        placeCurseOnPlayer(player1, player2);
        Permanent beetle = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());
        Permanent suit = harness.addToBattlefieldAndReturn(player2, new AssaultSuit());
        suit.setAttachedTo(beetle.getId());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Dune Beetle");
        harness.assertOnBattlefield(player2, "Assault Suit");
        harness.assertLife(player2, lifeBefore - 5);
    }

    @Test
    @DisplayName("A creature that cannot be sacrificed is excluded when a planeswalker is eligible")
    void sacrificesOnlyEligiblePermanent() {
        placeCurseOnPlayer(player1, player2);
        Permanent beetle = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());
        Permanent suit = harness.addToBattlefieldAndReturn(player2, new AssaultSuit());
        suit.setAttachedTo(beetle.getId());
        Permanent liliana = harness.addToBattlefieldAndReturn(player2, new LilianaDeathsMajesty());
        liliana.setCounterCount(CounterType.LOYALTY, 5);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Dune Beetle");
        harness.assertNotOnBattlefield(player2, "Liliana, Death's Majesty");
        harness.assertInGraveyard(player2, "Liliana, Death's Majesty");
        harness.assertLife(player2, lifeBefore);
    }

    @Test
    @DisplayName("Curse controller's creatures do not prevent enchanted player's life loss")
    void ignoresOtherPlayersCreatures() {
        placeCurseOnPlayer(player1, player2);
        harness.addToBattlefield(player1, new DuneBeetle());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dune Beetle");
        harness.assertLife(player2, lifeBefore - 5);
    }

    @Test
    @DisplayName("Casting the Curse enchants the opponent and makes that player lose life")
    void castingEnchantsOpponent() {
        harness.setHand(player1, List.of(new CruelReality()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Cruel Reality").getAttachedTo()).isEqualTo(player2.getId());
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 5);
    }

    @Test
    @DisplayName("The Curse can enchant its controller and triggers on that player's upkeep")
    void castingCanEnchantController() {
        harness.setHand(player1, List.of(new CruelReality()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castEnchantment(player1, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Cruel Reality").getAttachedTo()).isEqualTo(player1.getId());
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore - 5);
    }

    private void placeCurseOnPlayer(Player controller, Player enchantedPlayer) {
        Permanent cursePerm = harness.addToBattlefieldAndReturn(controller, new CruelReality());
        cursePerm.setAttachedTo(enchantedPlayer.getId());
    }
}
