package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CorpseDance.class, GrizzlyBears.class, SerraAngel.class, LightningBolt.class})
class CorpseDanceTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the topmost creature card of your graveyard with haste")
    void returnsTopmostCreatureWithHaste() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new SerraAngel()));
        harness.castFromHand(player1, new CorpseDance(), "{2}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Serra Angel");
        harness.assertInGraveyard(player1, "Grizzly Bears");

        Permanent returned = findPermanent(player1, "Serra Angel");
        assertThat(returned.getGrantedKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("Skips noncreature cards above the topmost creature card")
    void skipsNonCreatureCardsAboveTopmostCreature() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LightningBolt()));
        harness.castFromHand(player1, new CorpseDance(), "{2}{B}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Lightning Bolt");
    }

    @Test
    @DisplayName("The returned creature is exiled at the beginning of the next end step")
    void returnedCreatureExiledAtEndStep() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new CorpseDance(), "{2}{B}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("The delayed exile waits until its trigger resolves")
    void delayedExileWaitsForResolution() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new CorpseDance(), "{2}{B}");

        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A returned creature that dies before the end step stays in its graveyard")
    void returnedCreatureDyingBeforeEndStepStaysInGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new CorpseDance(), "{2}{B}");

        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, returned.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Only your graveyard is searched")
    void ignoresOpponentGraveyard() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new CorpseDance(), "{2}{B}");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Corpse Dance");
    }

    @Test
    @DisplayName("Without buyback the spell goes to the graveyard")
    void withoutBuybackGoesToGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new CorpseDance(), "{2}{B}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Corpse Dance");
        harness.assertNotInHand(player1, "Corpse Dance");
    }

    @Test
    @DisplayName("With buyback paid the spell returns to hand as it resolves")
    void withBuybackReturnsToHand() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new CorpseDance()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstantWithBuyback(player1, 0, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Corpse Dance");
        harness.assertNotInGraveyard(player1, "Corpse Dance");
    }

    @Test
    @DisplayName("Buyback returns the spell even when no creature is returned")
    void buybackReturnsToHandWithoutCreature() {
        harness.setGraveyard(player1, List.of(new LightningBolt()));
        harness.setHand(player1, List.of(new CorpseDance()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstantWithBuyback(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Corpse Dance");
        harness.assertNotInGraveyard(player1, "Corpse Dance");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does nothing when the graveyard holds no creature card")
    void doesNothingWithoutCreatureCard() {
        harness.setGraveyard(player1, List.of(new LightningBolt()));
        harness.setHand(player1, List.of(new CorpseDance()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Corpse Dance");
    }
}
