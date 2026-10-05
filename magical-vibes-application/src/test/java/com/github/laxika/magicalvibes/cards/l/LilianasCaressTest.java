package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HypnoticSpecter;
import com.github.laxika.magicalvibes.cards.s.Sift;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LilianasCaress.class, Distress.class, GrizzlyBears.class, HypnoticSpecter.class, Sift.class})
class LilianasCaressTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Liliana's Caress puts it on the stack as enchantment spell")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new LilianasCaress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Liliana's Caress resolves onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.setHand(player1, List.of(new LilianasCaress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Liliana's Caress");
    }

    @Test
    @DisplayName("Liliana's Caress causes opponent to lose 2 life when they discard via Distress")
    void triggersOnOpponentDiscardViaDistress() {
        harness.addToBattlefield(player1, new LilianasCaress());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Player1 chooses card from player2's revealed hand
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        // Resolve the discard trigger before checking life loss.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Liliana's Caress causes opponent to lose 2 life when they discard via Sift")
    void triggersOnOpponentDiscardViaDiscardEffect() {
        harness.addToBattlefield(player1, new LilianasCaress());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.setHand(player2, List.of(new Sift()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities(); // Resolve Sift — draws 3, prompts for discard

        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Liliana's Caress does NOT trigger when its controller discards")
    void doesNotTriggerOnControllerDiscard() {
        harness.addToBattlefield(player1, new LilianasCaress());
        harness.setLife(player1, 20);

        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.setHand(player1, List.of(new Sift()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Two Liliana's Caress each trigger, causing 4 life loss total")
    void twoCaressesEachTrigger() {
        harness.addToBattlefield(player1, new LilianasCaress());
        harness.addToBattlefield(player1, new LilianasCaress());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Liliana's Caress triggers when Hypnotic Specter forces random discard")
    void triggersOnRandomDiscardFromCombat() {
        harness.addToBattlefield(player1, new LilianasCaress());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLife(player2, 20);

        Permanent specter = harness.addToBattlefieldAndReturn(player1, new HypnoticSpecter());
        specter.setSummoningSick(false);
        specter.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        resolveAllTriggers();

        // Player2 took 2 combat damage + lost 2 life from Caress = 16
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Liliana's Caress does not trigger when not on the battlefield")
    void doesNotTriggerWhenNotOnBattlefield() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Player2's Liliana's Caress triggers when player1 discards")
    void opponentsCaressTriggersOnOurDiscard() {
        harness.addToBattlefield(player2, new LilianasCaress());
        harness.setLife(player1, 20);

        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.setHand(player1, List.of(new Sift()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Liliana's Caress trigger is logged")
    void triggerIsLogged() {
        harness.addToBattlefield(player1, new LilianasCaress());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log ->
                log.contains("Liliana's Caress") && log.contains("triggers") && log.contains("loses") && log.contains("life"));
    }

    @Test
    @DisplayName("Discard queues Caress's ability without applying life loss before resolution")
    void discardTriggerUsesTheStack() {
        harness.addToBattlefield(player1, new LilianasCaress());
        harness.setHand(player1, List.of(new Distress()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.handleCardChosen(player1, 0);
            harness.assertLife(player2, 20);
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            resolveAllTriggers();
            harness.assertLife(player2, 18);
        });
    }

    @Test
    @DisplayName("Caress's life loss is recorded for life-loss interactions")
    void recordsLifeLostThisTurn() {
        harness.addToBattlefield(player1, new LilianasCaress());
        harness.setHand(player1, List.of(new Distress()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castAndResolveSorcery(player1, 0, player2.getId());
            harness.handleCardChosen(player1, 0);
            resolveAllTriggers();
            assertThat(gd.lifeLostThisTurn.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        });
    }
}
