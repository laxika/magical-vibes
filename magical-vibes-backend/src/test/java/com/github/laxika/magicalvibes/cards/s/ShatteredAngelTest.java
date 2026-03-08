package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShatteredAngelTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ON_OPPONENT_LAND_ENTERS_BATTLEFIELD MayEffect wrapping GainLifeEffect(3)")
    void hasCorrectEffect() {
        ShatteredAngel card = new ShatteredAngel();

        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD).getFirst();
        assertThat(may.wrapped()).isInstanceOf(GainLifeEffect.class);
        assertThat(((GainLifeEffect) may.wrapped()).amount()).isEqualTo(3);
    }

    // ===== Opponent plays land — accept may =====

    @Test
    @DisplayName("Gains 3 life when accepting may after opponent plays a land")
    void gainsLifeWhenOpponentPlaysLandAccept() {
        harness.addToBattlefield(player1, new ShatteredAngel());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Forest()));
        harness.castCreature(player2, 0);

        // Trigger on stack
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities(); // Resolve MayEffect → prompts player

        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // Resolve GainLifeEffect

        harness.assertLife(player1, 23);
    }

    // ===== Opponent plays land — decline may =====

    @Test
    @DisplayName("No life gain when declining may after opponent plays a land")
    void noLifeGainWhenOpponentPlaysLandDecline() {
        harness.addToBattlefield(player1, new ShatteredAngel());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Forest()));
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
    }

    // ===== Does not trigger for controller's own lands =====

    @Test
    @DisplayName("Does not trigger when controller plays a land")
    void doesNotTriggerForControllerLands() {
        harness.addToBattlefield(player1, new ShatteredAngel());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Forest()));
        harness.castCreature(player1, 0);

        // No trigger — only cares about opponents
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();
        harness.assertLife(player1, 20);
    }

    // ===== Two Shattered Angels trigger separately =====

    @Test
    @DisplayName("Two Shattered Angels each trigger separately when opponent plays a land")
    void twoAngelsEachTrigger() {
        harness.addToBattlefield(player1, new ShatteredAngel());
        harness.addToBattlefield(player1, new ShatteredAngel());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Forest()));
        harness.castCreature(player2, 0);

        // Two triggers (one per Shattered Angel)
        assertThat(gd.stack).hasSize(2);

        // Resolve first MayEffect
        harness.passBothPriorities();
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        // Resolve second MayEffect
        harness.passBothPriorities();
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 26);
    }

    // ===== Triggers on each land separately =====

    @Test
    @DisplayName("Triggers each time opponent plays a land on separate turns")
    void triggersOnEachLand() {
        harness.addToBattlefield(player1, new ShatteredAngel());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // First land
        harness.setHand(player2, List.of(new Forest()));
        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.assertLife(player1, 23);

        // Reset for new turn
        gd.landsPlayedThisTurn.put(player2.getId(), 0);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Second land
        harness.setHand(player2, List.of(new Mountain()));
        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.assertLife(player1, 26);
    }
}
