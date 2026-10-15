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

@CardUsed({CorpseDance.class, GrizzlyBears.class, SerraAngel.class, LightningBolt.class, Counterspell.class})
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
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();
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
        harness.castFromHand(player1, new CorpseDance(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Corpse Dance");
    }

    @Test
    @DisplayName("The top creature is determined on resolution, after responding spells")
    void choosesTopCreatureAtResolution() {
        harness.setGraveyard(player1, List.of(new SerraAngel()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.castFromHand(player1, new CorpseDance(), "{2}{B}");

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Serra Angel");
        harness.assertNotOnBattlefield(player1, "Serra Angel");
    }

    @Test
    @DisplayName("Casting during the end step delays exile until the next turn and haste expires")
    void endStepReanimationWaitsUntilNextEndStep() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.castFromHand(player1, new CorpseDance(), "{2}{B}");
            harness.passBothPriorities();
            assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.HASTE)).isTrue();
        });

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isFalse();
        harness.passUntil(player2, TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Countering Corpse Dance prevents buyback from returning it to hand")
    void counteredSpellDoesNotReturnWithBuyback() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        CorpseDance dance = new CorpseDance();
        harness.setHand(player1, List.of(dance));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castInstantWithBuyback(player1, 0, null);

        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, dance.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Corpse Dance");
        harness.assertNotInHand(player1, "Corpse Dance");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }
}
