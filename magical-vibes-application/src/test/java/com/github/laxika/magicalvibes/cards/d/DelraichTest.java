package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BlackMarket;
import com.github.laxika.magicalvibes.cards.b.BogSmugglers;
import com.github.laxika.magicalvibes.cards.c.CateranPersuader;
import com.github.laxika.magicalvibes.cards.d.DeepwoodGhoul;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Delraich.class, BlackMarket.class, BogSmugglers.class, CateranPersuader.class,
        DeepwoodGhoul.class, FreshVolunteers.class})
class DelraichTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast using alternate cost: sacrifice 3 black creatures")
    void castWithAlternateCost() {
        UUID smugglers = harness.addToBattlefieldAndReturn(player1, new BogSmugglers()).getId();
        UUID ghoul = harness.addToBattlefieldAndReturn(player1, new DeepwoodGhoul()).getId();
        UUID persuader = harness.addToBattlefieldAndReturn(player1, new CateranPersuader()).getId();

        harness.setHand(player1, List.of(new Delraich()));
        harness.castCreatureWithAlternateCost(player1, 0, List.of(smugglers, ghoul, persuader));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Delraich");
        harness.assertInGraveyard(player1, "Bog Smugglers");
        harness.assertInGraveyard(player1, "Deepwood Ghoul");
        harness.assertInGraveyard(player1, "Cateran Persuader");
    }

    @Test
    @DisplayName("Can be cast normally with mana")
    void castWithManaCost() {
        harness.setHand(player1, List.of(new Delraich()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Delraich");
    }

    @Test
    @DisplayName("Alternate cost fails if fewer than 3 creatures are sacrificed")
    void alternateCostFailsWithFewerCreatures() {
        UUID smugglers = harness.addToBattlefieldAndReturn(player1, new BogSmugglers()).getId();
        UUID ghoul = harness.addToBattlefieldAndReturn(player1, new DeepwoodGhoul()).getId();

        harness.setHand(player1, List.of(new Delraich()));

        assertThatThrownBy(() ->
                harness.castCreatureWithAlternateCost(player1, 0, List.of(smugglers, ghoul)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice exactly 3");
    }

    @Test
    @DisplayName("Alternate cost fails if a non-black creature is sacrificed")
    void alternateCostFailsWithNonBlackCreature() {
        UUID smugglers = harness.addToBattlefieldAndReturn(player1, new BogSmugglers()).getId();
        UUID ghoul = harness.addToBattlefieldAndReturn(player1, new DeepwoodGhoul()).getId();
        UUID volunteers = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers()).getId();

        harness.setHand(player1, List.of(new Delraich()));

        assertThatThrownBy(() ->
                harness.castCreatureWithAlternateCost(player1, 0, List.of(smugglers, ghoul, volunteers)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not match");
    }

    @Test
    @DisplayName("Alternate cost fails if a black noncreature is sacrificed")
    void alternateCostFailsWithBlackNonCreature() {
        UUID smugglers = harness.addToBattlefieldAndReturn(player1, new BogSmugglers()).getId();
        UUID ghoul = harness.addToBattlefieldAndReturn(player1, new DeepwoodGhoul()).getId();
        UUID blackMarket = harness.addToBattlefieldAndReturn(player1, new BlackMarket()).getId();

        harness.setHand(player1, List.of(new Delraich()));

        assertThatThrownBy(() ->
                harness.castCreatureWithAlternateCost(player1, 0, List.of(smugglers, ghoul, blackMarket)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not match");
    }

    @Test
    @DisplayName("Alternate cost fails if the same permanent is selected more than once")
    void alternateCostFailsWithDuplicatePermanent() {
        UUID smugglers = harness.addToBattlefieldAndReturn(player1, new BogSmugglers()).getId();
        UUID ghoul = harness.addToBattlefieldAndReturn(player1, new DeepwoodGhoul()).getId();
        harness.addToBattlefield(player1, new CateranPersuader());

        harness.setHand(player1, List.of(new Delraich()));

        assertThatThrownBy(() ->
                harness.castCreatureWithAlternateCost(player1, 0, List.of(smugglers, ghoul, ghoul)))
                .isInstanceOf(IllegalStateException.class);
    }
}
