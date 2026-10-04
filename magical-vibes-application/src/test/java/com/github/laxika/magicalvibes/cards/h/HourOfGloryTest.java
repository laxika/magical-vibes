package com.github.laxika.magicalvibes.cards.h;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.FeralProwler;
import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.t.TheScarabGod;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({HourOfGlory.class, GrizzlyBears.class, LlanowarElves.class, FountainOfYouth.class,
        FeralProwler.class, TheScarabGod.class, Conspiracy.class})
class HourOfGloryTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target creature; a non-God leaves the controller's hand untouched")
    void exilesTargetNonGod() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new HourOfGlory()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(1);
        // Not a God: same-name cards in hand are not touched.
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Exiles a God and all same-name cards from its controller's hand")
    void exilesGodAndSameNameFromHand() {
        Permanent god = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card godCard = TestCards.mutableCard(god);
        godCard.setSubtypes(List.of(CardSubtype.GOD));

        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new LlanowarElves()));
        harness.setHand(player1, List.of(new HourOfGlory()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // The God is gone from the battlefield.
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        // Both same-name hand cards are exiled; the non-matching card stays.
        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Llanowar Elves");
        // God itself plus the two hand copies land in exile.
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(3);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        // A legal creature exists so the spell is castable; the artifact is the illegal target.
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new HourOfGlory()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void exilesRealGodAndOnlyItsControllersMatchingHandCards() {
        Permanent god = harness.addToBattlefieldAndReturn(player2, new TheScarabGod());
        harness.setHand(player2, List.of(new TheScarabGod(), new TheScarabGod(), new FeralProwler()));
        harness.setHand(player1, List.of(new HourOfGlory(), new TheScarabGod()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, god.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "The Scarab God");
        harness.assertNotInHand(player2, "The Scarab God");
        harness.assertInHand(player2, "Feral Prowler");
        harness.assertInHand(player1, "The Scarab God");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("The Scarab God"))
                .hasSize(3);
        harness.assertNotInGraveyard(player2, "The Scarab God");
    }

    @Test
    void exilesMatchingHandCardsWhenContinuousEffectMakesCreatureAGod() {
        Permanent conspiracy = harness.addToBattlefieldAndReturn(player2, new Conspiracy());
        conspiracy.setChosenSubtype(CardSubtype.GOD);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FeralProwler());
        harness.setHand(player2, List.of(new FeralProwler()));
        harness.setHand(player1, List.of(new HourOfGlory()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Feral Prowler");
        harness.assertNotInHand(player2, "Feral Prowler");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Feral Prowler"))
                .hasSize(2);
    }

    @Test
    void leavesHandUntouchedWhenContinuousEffectRemovesGodType() {
        Permanent conspiracy = harness.addToBattlefieldAndReturn(player2, new Conspiracy());
        conspiracy.setChosenSubtype(CardSubtype.CAT);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new TheScarabGod());
        harness.setHand(player2, List.of(new TheScarabGod()));
        harness.setHand(player1, List.of(new HourOfGlory()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "The Scarab God");
        harness.assertInHand(player2, "The Scarab God");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("The Scarab God"))
                .hasSize(1);
    }

    @Test
    void leavesHandUntouchedWhenTargetLeavesBeforeResolution() {
        Permanent god = harness.addToBattlefieldAndReturn(player2, new TheScarabGod());
        harness.setHand(player2, List.of(new TheScarabGod()));
        harness.setHand(player1, List.of(new HourOfGlory()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, god.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, god));
        harness.passBothPriorities();

        harness.assertInHand(player2, "The Scarab God");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("The Scarab God"))
                .hasSize(1);
        harness.assertInGraveyard(player1, "Hour of Glory");
    }

    @Test
    void exilesGodWhenItsControllerHasAnEmptyHand() {
        Permanent god = harness.addToBattlefieldAndReturn(player2, new TheScarabGod());
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new HourOfGlory()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, god.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "The Scarab God");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("The Scarab God"))
                .hasSize(1);
    }
}
