package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Sift;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SangromancerTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ON_OPPONENT_CREATURE_DIES MayEffect wrapping GainLifeEffect(3)")
    void hasCorrectDeathTrigger() {
        Sangromancer card = new Sangromancer();

        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_CREATURE_DIES)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_CREATURE_DIES).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_OPPONENT_CREATURE_DIES).getFirst();
        assertThat(may.wrapped()).isInstanceOf(GainLifeEffect.class);
        assertThat(((GainLifeEffect) may.wrapped()).amount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Has ON_OPPONENT_DISCARDS MayEffect wrapping GainLifeEffect(3)")
    void hasCorrectDiscardTrigger() {
        Sangromancer card = new Sangromancer();

        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_DISCARDS)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_DISCARDS).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_OPPONENT_DISCARDS).getFirst();
        assertThat(may.wrapped()).isInstanceOf(GainLifeEffect.class);
        assertThat(((GainLifeEffect) may.wrapped()).amount()).isEqualTo(3);
    }

    // ===== Opponent creature dies — accept may =====

    @Test
    @DisplayName("Gains 3 life when accepting may after opponent's creature dies")
    void gainsLifeWhenOpponentCreatureDiesAccept() {
        harness.addToBattlefield(player1, new Sangromancer());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);

        // Kill opponent's creature with Shock
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock → bears die → MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt

        // May ability prompt for Sangromancer's controller
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 23);
    }

    // ===== Opponent creature dies — decline may =====

    @Test
    @DisplayName("No life gain when declining may after opponent's creature dies")
    void noLifeGainWhenOpponentCreatureDiesDecline() {
        harness.addToBattlefield(player1, new Sangromancer());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock → bears die → MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt

        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
    }

    // ===== Does not trigger when own creature dies =====

    @Test
    @DisplayName("Does not trigger when controller's own creature dies")
    void doesNotTriggerWhenOwnCreatureDies() {
        harness.addToBattlefield(player1, new Sangromancer());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        // Player2 kills player1's creature
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock → player1's bears die

        // No may prompt — Sangromancer doesn't trigger for own creatures
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();
        harness.assertLife(player1, 20);
    }

    // ===== Opponent discards — accept may =====

    @Test
    @DisplayName("Gains 3 life when accepting may after opponent discards via Distress")
    void gainsLifeWhenOpponentDiscardsAccept() {
        harness.addToBattlefield(player1, new Sangromancer());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Distress → reveals hand

        // Player1 chooses card from player2's revealed hand
        harness.handleCardChosen(player1, 0);

        // MayEffect goes on stack after discard trigger
        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt

        // May ability prompt for Sangromancer's controller
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 23);
    }

    // ===== Opponent discards — decline may =====

    @Test
    @DisplayName("No life gain when declining may after opponent discards")
    void noLifeGainWhenOpponentDiscardsDecline() {
        harness.addToBattlefield(player1, new Sangromancer());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
    }

    // ===== Does not trigger when controller discards =====

    @Test
    @DisplayName("Does not trigger when controller discards")
    void doesNotTriggerWhenControllerDiscards() {
        harness.addToBattlefield(player2, new Sangromancer());
        harness.setLife(player2, 20);

        // Player2 discards via their own Distress effect cast by player1
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        // Player2's Sangromancer should NOT trigger when player2 discards
        // Use Distress from player2 targeting player1 — player1 discards (opponent of Sangromancer's controller)
        // Actually, to test that controller's OWN discard doesn't trigger:
        // player1 casts Distress targeting player1 is not valid. Let's use Sift on player2.
        gd.playerDecks.get(player2.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).add(new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Sift()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities(); // Resolve Sift — draws 3, prompts for discard

        harness.handleCardChosen(player2, 0);

        // Sangromancer does NOT trigger for controller's own discard
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();
        harness.assertLife(player2, 20);
    }
}
