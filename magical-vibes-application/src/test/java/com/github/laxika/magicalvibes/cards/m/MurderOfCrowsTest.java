package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawAndDiscardCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MurderOfCrowsTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Murder of Crows has ON_ANY_CREATURE_DIES MayEffect wrapping DrawAndDiscardCardEffect")
    void hasCorrectEffect() {
        MurderOfCrows card = new MurderOfCrows();

        assertThat(card.getEffects(EffectSlot.ON_ANY_CREATURE_DIES)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ANY_CREATURE_DIES).getFirst()).isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_ANY_CREATURE_DIES).getFirst();
        assertThat(may.wrapped()).isInstanceOf(DrawAndDiscardCardEffect.class);
    }

    // ===== Trigger: another creature dies, accept may =====

    @Test
    @DisplayName("When another creature dies, controller may draw then discard (accept)")
    void anotherCreatureDiesAcceptMay() {
        harness.addToBattlefield(player1, new MurderOfCrows());
        harness.addToBattlefield(player2, new GrizzlyBears());

        setDeck(player1, List.of(new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0, player2.getId());
        // Pass 1: resolve Cruel Edict → creature dies → ON_ANY_CREATURE_DIES trigger fires
        harness.passBothPriorities();
        // Pass 2: resolve triggered ability → MayEffect prompts player
        harness.passBothPriorities();

        // Creature should be dead
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // May ability prompt for Murder of Crows' controller
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Drew a card, now awaiting discard choice
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);

        // Discard a card
        harness.handleCardChosen(player1, 0);

        // Net: cast Cruel Edict (hand -1), drew 1, discarded 1 → hand size should be same as before -1
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore - 1);
    }

    // ===== Trigger: another creature dies, decline may =====

    @Test
    @DisplayName("When another creature dies, controller may draw then discard (decline)")
    void anotherCreatureDiesDeclineMay() {
        harness.addToBattlefield(player1, new MurderOfCrows());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        // No draw, no discard — hand size is just minus the spell cast
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore - 1);
    }

    // ===== Does NOT trigger when Murder of Crows itself dies =====

    @Test
    @DisplayName("Murder of Crows does not trigger when it dies itself (only 'another creature')")
    void doesNotTriggerWhenSelfDies() {
        harness.addToBattlefield(player1, new MurderOfCrows());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities(); // resolve Cruel Edict → Murder of Crows dies

        // No may ability prompt — Murder of Crows died, it uses ON_ANY_CREATURE_DIES
        // which only fires for permanents still on the battlefield
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();
    }

    // ===== Triggers on controller's own creature dying =====

    @Test
    @DisplayName("Murder of Crows triggers when controller's own creature dies")
    void triggersOnOwnCreatureDying() {
        harness.addToBattlefield(player1, new MurderOfCrows());
        harness.addToBattlefield(player1, new GrizzlyBears());

        setDeck(player1, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        // Player2 casts Cruel Edict targeting player1 — player1 sacrifices Grizzly Bears
        // (Murder of Crows remains because player chooses which creature to sacrifice)
        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities(); // resolve Cruel Edict

        // Player1 must choose which creature to sacrifice — choose Grizzly Bears (index 1)
        Permanent grizzlyBears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        harness.handlePermanentChosen(player1, grizzlyBears.getId());

        // ON_ANY_CREATURE_DIES fires → MayEffect trigger goes on stack
        harness.passBothPriorities();

        // May ability prompt for Murder of Crows' controller
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Drew a card, now awaiting discard choice
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
    }

    // ===== Helpers =====

    private void setDeck(com.github.laxika.magicalvibes.model.Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
