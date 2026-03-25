package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DrawCardsEqualToControlledCreatureCountEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToCardsInHandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TishanaVoiceOfThunderTest extends BaseCardTest {

    @Test
    @DisplayName("Tishana has correct effects registered")
    void hasCorrectEffects() {
        TishanaVoiceOfThunder card = new TishanaVoiceOfThunder();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.STATIC).get(0))
                .isInstanceOf(PowerToughnessEqualToCardsInHandEffect.class);
        assertThat(card.getEffects(EffectSlot.STATIC).get(1))
                .isInstanceOf(NoMaximumHandSizeEffect.class);

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).singleElement()
                .isInstanceOf(DrawCardsEqualToControlledCreatureCountEffect.class);
    }

    @Test
    @DisplayName("P/T equals number of cards in controller's hand")
    void ptEqualsHandSize() {
        Permanent tishana = addTishanaReady(player1);
        gd.playerHands.get(player1.getId()).clear();

        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, tishana)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, tishana)).isEqualTo(3);
    }

    @Test
    @DisplayName("P/T is 0/0 with empty hand")
    void ptZeroWithEmptyHand() {
        Permanent tishana = addTishanaReady(player1);
        gd.playerHands.get(player1.getId()).clear();

        assertThat(gqs.getEffectivePower(gd, tishana)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, tishana)).isEqualTo(0);
    }

    @Test
    @DisplayName("ETB draws cards equal to controlled creature count")
    void etbDrawsCardsEqualToCreatureCount() {
        // Put creatures on the battlefield first
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        // Add cards to library so there's something to draw
        gd.playerDecks.get(player1.getId()).clear();
        for (int i = 0; i < 10; i++) {
            gd.playerDecks.get(player1.getId()).add(new Forest());
        }

        // Extra card in hand so Tishana survives (hand > 0 after casting → P/T > 0)
        harness.setHand(player1, List.of(new TishanaVoiceOfThunder(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        // Resolve the creature spell — puts ETB trigger on stack
        harness.passBothPriorities();
        // Resolve the ETB trigger — draws cards
        harness.passBothPriorities();

        // 3 Bears + 1 Tishana = 4 creatures, draw 4 cards
        // Hand was 2, cast Tishana (1 left), drew 4 = 5
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(5);
    }

    @Test
    @DisplayName("Tishana with empty hand after casting dies to SBAs but ETB still triggers")
    void tishanaWithEmptyHandDiesToSbasButEtbTriggers() {
        // Only Tishana in hand — after casting, hand is empty → Tishana is 0/0 → dies to SBAs
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        gd.playerDecks.get(player1.getId()).clear();
        for (int i = 0; i < 5; i++) {
            gd.playerDecks.get(player1.getId()).add(new Forest());
        }

        harness.setHand(player1, List.of(new TishanaVoiceOfThunder()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell — Tishana enters then dies to SBAs
        harness.passBothPriorities(); // resolve ETB trigger

        // Tishana died (0/0), so only 2 Bears on battlefield when ETB resolves → draw 2
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(2);
        // Tishana should be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Tishana, Voice of Thunder"));
    }

    @Test
    @DisplayName("ETB does not count opponent's creatures")
    void etbDoesNotCountOpponentCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        gd.playerDecks.get(player1.getId()).clear();
        for (int i = 0; i < 5; i++) {
            gd.playerDecks.get(player1.getId()).add(new Forest());
        }

        // Extra card so Tishana survives
        harness.setHand(player1, List.of(new TishanaVoiceOfThunder(), new GrizzlyBears()));
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

        gd.playerDecks.get(player1.getId()).clear();
        for (int i = 0; i < 5; i++) {
            gd.playerDecks.get(player1.getId()).add(new Forest());
        }

        // Extra card so Tishana survives
        harness.setHand(player1, List.of(new TishanaVoiceOfThunder(), new GrizzlyBears()));
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
        harness.setHand(player1, List.of(new TishanaVoiceOfThunder(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Tishana, Voice of Thunder"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("P/T updates after ETB draw increases hand size")
    void ptUpdatesAfterEtbDraw() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        gd.playerDecks.get(player1.getId()).clear();
        for (int i = 0; i < 10; i++) {
            gd.playerDecks.get(player1.getId()).add(new Forest());
        }

        // Extra card so Tishana survives
        harness.setHand(player1, List.of(new TishanaVoiceOfThunder(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // 2 Bears + 1 Tishana = 3 creatures → draw 3 cards
        // Hand was 2, cast Tishana (1 left), drew 3 = 4 cards in hand
        int handSize = gd.playerHands.get(player1.getId()).size();
        assertThat(handSize).isEqualTo(4);

        Permanent tishana = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Tishana, Voice of Thunder"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, tishana)).isEqualTo(handSize);
        assertThat(gqs.getEffectiveToughness(gd, tishana)).isEqualTo(handSize);
    }

    @Test
    @DisplayName("P/T counts only controller's hand, not opponent's")
    void ptCountsOnlyControllerHand() {
        Permanent tishana = addTishanaReady(player1);
        gd.playerHands.get(player1.getId()).clear();
        gd.playerHands.get(player2.getId()).clear();

        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, tishana)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, tishana)).isEqualTo(1);
    }

    private Permanent addTishanaReady(com.github.laxika.magicalvibes.model.Player player) {
        TishanaVoiceOfThunder card = new TishanaVoiceOfThunder();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
