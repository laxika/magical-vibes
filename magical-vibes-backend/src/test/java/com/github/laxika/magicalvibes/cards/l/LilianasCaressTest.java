package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HypnoticSpecter;
import com.github.laxika.magicalvibes.cards.s.Sift;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LilianasCaressTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Liliana's Caress has correct effect")
    void hasCorrectEffect() {
        LilianasCaress card = new LilianasCaress();

        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_DISCARDS)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_DISCARDS).getFirst())
                .isInstanceOf(LoseLifeEffect.class);
        LoseLifeEffect effect =
                (LoseLifeEffect) card.getEffects(EffectSlot.ON_OPPONENT_DISCARDS).getFirst();
        assertThat(effect.amount()).isEqualTo(2);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Liliana's Caress puts it on the stack as enchantment spell")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new LilianasCaress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Liliana's Caress");
    }

    @Test
    @DisplayName("Liliana's Caress resolves onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.setHand(player1, List.of(new LilianasCaress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Liliana's Caress"));
    }

    // ===== Triggered ability: opponent discards via Distress =====

    @Test
    @DisplayName("Liliana's Caress causes opponent to lose 2 life when they discard via Distress")
    void triggersOnOpponentDiscardViaDistress() {
        harness.addToBattlefield(player1, new LilianasCaress());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Player1 chooses card from player2's revealed hand
        harness.handleCardChosen(player1, 0);

        // Liliana's Caress trigger: player2 loses 2 life
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Triggered ability: opponent discards via discard effect =====

    @Test
    @DisplayName("Liliana's Caress causes opponent to lose 2 life when they discard via Sift")
    void triggersOnOpponentDiscardViaDiscardEffect() {
        harness.addToBattlefield(player1, new LilianasCaress());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        gd.playerDecks.get(player2.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).add(new GrizzlyBears());

        harness.setHand(player2, List.of(new Sift()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities(); // Resolve Sift — draws 3, prompts for discard

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== No trigger when controller discards =====

    @Test
    @DisplayName("Liliana's Caress does NOT trigger when its controller discards")
    void doesNotTriggerOnControllerDiscard() {
        harness.addToBattlefield(player1, new LilianasCaress());
        harness.setLife(player1, 20);

        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        harness.setHand(player1, List.of(new Sift()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    // ===== Two copies each trigger =====

    @Test
    @DisplayName("Two Liliana's Caress each trigger, causing 4 life loss total")
    void twoCaressesEachTrigger() {
        harness.addToBattlefield(player1, new LilianasCaress());
        harness.addToBattlefield(player1, new LilianasCaress());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    // ===== Random discard from combat triggers =====

    @Test
    @DisplayName("Liliana's Caress triggers when Hypnotic Specter forces random discard")
    void triggersOnRandomDiscardFromCombat() {
        harness.addToBattlefield(player1, new LilianasCaress());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLife(player2, 20);

        Permanent specter = new Permanent(new HypnoticSpecter());
        specter.setSummoningSick(false);
        specter.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(specter);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Player2 took 2 combat damage + lost 2 life from Caress = 16
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    // ===== No trigger when not on the battlefield =====

    @Test
    @DisplayName("Liliana's Caress does not trigger when not on the battlefield")
    void doesNotTriggerWhenNotOnBattlefield() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Opponent's Caress triggers on our discard =====

    @Test
    @DisplayName("Player2's Liliana's Caress triggers when player1 discards")
    void opponentsCaressTriggersOnOurDiscard() {
        harness.addToBattlefield(player2, new LilianasCaress());
        harness.setLife(player1, 20);

        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        harness.setHand(player1, List.of(new Sift()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    // ===== Logging =====

    @Test
    @DisplayName("Liliana's Caress trigger is logged")
    void triggerIsLogged() {
        harness.addToBattlefield(player1, new LilianasCaress());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("Liliana's Caress") && log.contains("triggers") && log.contains("loses") && log.contains("life"));
    }
}
