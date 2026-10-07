package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.JungleDelver;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TishanaVoiceOfThunder.class, JungleDelver.class, Forest.class, Island.class, Humility.class})
class TishanaVoiceOfThunderTest extends BaseCardTest {

    @Test
    @DisplayName("P/T equals number of cards in controller's hand")
    void ptEqualsHandSize() {
        Permanent tishana = harness.addToBattlefieldAndReturn(player1, new TishanaVoiceOfThunder());
        harness.setHand(player1, List.of(new JungleDelver(), new JungleDelver(), new JungleDelver()));

        assertThat(gqs.getEffectivePower(gd, tishana)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, tishana)).isEqualTo(3);
    }

    @Test
    @DisplayName("P/T is 0/0 with empty hand")
    void ptZeroWithEmptyHand() {
        Permanent tishana = harness.addToBattlefieldAndReturn(player1, new TishanaVoiceOfThunder());
        harness.setHand(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, tishana)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, tishana)).isEqualTo(0);
    }

    @Test
    @DisplayName("ETB draws cards equal to controlled creature count")
    void etbDrawsCardsEqualToCreatureCount() {
        // Put creatures on the battlefield first
        harness.addToBattlefield(player1, new JungleDelver());
        harness.addToBattlefield(player1, new JungleDelver());
        harness.addToBattlefield(player1, new JungleDelver());

        // Add cards to library so there's something to draw
        harness.setLibrary(player1, IntStream.range(0, 10).mapToObj(i -> new Forest()).toList());

        // Extra card in hand so Tishana survives (hand > 0 after casting → P/T > 0)
        harness.setHand(player1, List.of(new TishanaVoiceOfThunder(), new JungleDelver()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        // Resolve the creature spell — puts ETB trigger on stack
        harness.passBothPriorities();
        // Resolve the ETB trigger — draws cards
        harness.passBothPriorities();

        // 3 Delvers + 1 Tishana = 4 creatures, draw 4 cards
        // Hand was 2, cast Tishana (1 left), drew 4 = 5
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(5);
    }

    @Test
    @DisplayName("Tishana with empty hand after casting dies to SBAs but ETB still triggers")
    void tishanaWithEmptyHandDiesToSbasButEtbTriggers() {
        // Only Tishana in hand — after casting, hand is empty → Tishana is 0/0 → dies to SBAs
        harness.addToBattlefield(player1, new JungleDelver());
        harness.addToBattlefield(player1, new JungleDelver());

        harness.setLibrary(player1, IntStream.range(0, 5).mapToObj(i -> new Forest()).toList());

        harness.castFromHand(player1, new TishanaVoiceOfThunder(), "{5}{G}{U}");
        harness.passBothPriorities(); // resolve creature spell — Tishana enters then dies to SBAs
        harness.passBothPriorities(); // resolve ETB trigger

        // Tishana died (0/0), so only 2 Delvers on battlefield when ETB resolves → draw 2
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(2);
        // Tishana should be in graveyard
        harness.assertInGraveyard(player1, "Tishana, Voice of Thunder");
    }

    @Test
    @DisplayName("ETB does not count opponent's creatures")
    void etbDoesNotCountOpponentCreatures() {
        harness.addToBattlefield(player2, new JungleDelver());
        harness.addToBattlefield(player2, new JungleDelver());

        harness.setLibrary(player1, IntStream.range(0, 5).mapToObj(i -> new Forest()).toList());

        // Extra card so Tishana survives
        harness.setHand(player1, List.of(new TishanaVoiceOfThunder(), new JungleDelver()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Only Tishana on player1's battlefield = 1 creature = draw 1
        // Hand was 2, cast Tishana (1 left), drew 1 = 2
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB does not count non-creature permanents")
    void etbDoesNotCountNonCreatures() {
        // Lands are not creatures
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());

        harness.setLibrary(player1, IntStream.range(0, 5).mapToObj(i -> new Forest()).toList());

        // Extra card so Tishana survives
        harness.setHand(player1, List.of(new TishanaVoiceOfThunder(), new JungleDelver()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Only Tishana on battlefield (lands are not creatures) = 1 creature = draw 1
        // Hand was 2, cast Tishana (1 left), drew 1 = 2
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB puts triggered ability on stack")
    void etbPutsTriggeredAbilityOnStack() {
        // Extra card so Tishana survives on the battlefield
        harness.setHand(player1, List.of(new TishanaVoiceOfThunder(), new JungleDelver()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Tishana, Voice of Thunder");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("P/T updates after ETB draw increases hand size")
    void ptUpdatesAfterEtbDraw() {
        harness.addToBattlefield(player1, new JungleDelver());
        harness.addToBattlefield(player1, new JungleDelver());

        harness.setLibrary(player1, IntStream.range(0, 10).mapToObj(i -> new Forest()).toList());

        // Extra card so Tishana survives
        harness.setHand(player1, List.of(new TishanaVoiceOfThunder(), new JungleDelver()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // 2 Delvers + 1 Tishana = 3 creatures → draw 3 cards
        // Hand was 2, cast Tishana (1 left), drew 3 = 4 cards in hand
        int handSize = gd.playerHands.get(player1.getId()).size();
        assertThat(handSize).isEqualTo(4);

        Permanent tishana = findPermanent(player1, "Tishana, Voice of Thunder");
        assertThat(gqs.getEffectivePower(gd, tishana)).isEqualTo(handSize);
        assertThat(gqs.getEffectiveToughness(gd, tishana)).isEqualTo(handSize);
    }

    @Test
    @DisplayName("P/T counts only controller's hand, not opponent's")
    void ptCountsOnlyControllerHand() {
        Permanent tishana = harness.addToBattlefieldAndReturn(player1, new TishanaVoiceOfThunder());
        harness.setHand(player1, List.of(new JungleDelver()));
        harness.setHand(player2, List.of(new JungleDelver(), new JungleDelver(), new JungleDelver()));

        assertThat(gqs.getEffectivePower(gd, tishana)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, tishana)).isEqualTo(1);
    }
    @Test
    void controllerKeepsMoreThanSevenCardsDuringCleanup() {
        harness.setHand(player1, IntStream.range(0, 9).mapToObj(i -> (Card) new Forest()).toList());
        harness.addToBattlefield(player1, new TishanaVoiceOfThunder());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
    }

    @Test
    void opponentStillDiscardsDuringCleanup() {
        harness.setHand(player1, IntStream.range(0, 9).mapToObj(i -> (Card) new Forest()).toList());
        harness.setHand(player2, List.of(new Forest()));
        harness.addToBattlefield(player2, new TishanaVoiceOfThunder());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);
    }

    @Test
    @CardUsed({TishanaVoiceOfThunder.class, Forest.class, Humility.class})
    void losingAbilitiesRestoresMaximumHandSize() {
        harness.setHand(player1, IntStream.range(0, 9).mapToObj(i -> (Card) new Forest()).toList());
        Permanent tishana = harness.addToBattlefieldAndReturn(player1, new TishanaVoiceOfThunder());
        harness.addToBattlefield(player2, new Humility());
        assertThat(gqs.getEffectivePower(gd, tishana)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, tishana)).isEqualTo(1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);
    }

    @Test
    void creatureCountIsDeterminedWhenTriggerResolves() {
        harness.setLibrary(player1, IntStream.range(0, 5).mapToObj(i -> new Forest()).toList());
        harness.setHand(player1, List.of(new TishanaVoiceOfThunder(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new JungleDelver());
        harness.addToBattlefield(player1, new JungleDelver());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    void emptyHandAndNoOtherCreaturesDrawsZeroAfterTishanaDies() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new TishanaVoiceOfThunder(), "{5}{G}{U}");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Tishana, Voice of Thunder");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void characteristicSizeWorksInGraveyardAndUpdatesWithHandSize() {
        TishanaVoiceOfThunder tishana = new TishanaVoiceOfThunder();
        harness.setGraveyard(player1, List.of(tishana));
        harness.setHand(player1, List.of(new Forest(), new Island(), new JungleDelver()));

        assertThat(gqs.getEffectiveCardPower(gd, tishana)).isEqualTo(3);
        assertThat(gqs.getEffectiveCardToughness(gd, tishana)).isEqualTo(3);

        harness.setHand(player1, List.of(new Forest()));

        assertThat(gqs.getEffectiveCardPower(gd, tishana)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, tishana)).isEqualTo(1);
    }
}
