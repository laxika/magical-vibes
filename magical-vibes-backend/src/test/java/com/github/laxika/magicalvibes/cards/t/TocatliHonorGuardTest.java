package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PriestOfUrabrask;
import com.github.laxika.magicalvibes.cards.s.SuturePriest;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreatureEnteringDontCauseTriggersEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TocatliHonorGuardTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has STATIC CreatureEnteringDontCauseTriggersEffect")
    void hasCorrectStaticEffect() {
        TocatliHonorGuard card = new TocatliHonorGuard();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(CreatureEnteringDontCauseTriggersEffect.class);
    }

    // ===== Suppresses creature's own ETB triggered ability =====

    @Test
    @DisplayName("Suppresses a creature's own ETB triggered ability (Priest of Urabrask gets no mana)")
    void suppressesCreatureOwnETBTriggeredAbility() {
        harness.addToBattlefield(player1, new TocatliHonorGuard());

        harness.setHand(player1, List.of(new PriestOfUrabrask()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);

        // Resolve creature spell — Priest enters but ETB does not trigger
        harness.passBothPriorities();

        // Priest is on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Priest of Urabrask"));
        // Stack is empty — no triggered ability was placed on it
        assertThat(gd.stack).isEmpty();
        // No mana was awarded (ETB suppressed)
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    // ===== Suppresses other permanents' creature-enters triggers =====

    @Test
    @DisplayName("Suppresses Suture Priest's ally creature trigger")
    void suppressesSuturePriestAllyTrigger() {
        harness.addToBattlefield(player1, new TocatliHonorGuard());
        harness.addToBattlefield(player1, new SuturePriest());
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        // Resolve creature spell — Grizzly Bears enters, but Suture Priest does NOT trigger
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Suppresses Suture Priest's opponent creature trigger")
    void suppressesSuturePriestOpponentTrigger() {
        harness.addToBattlefield(player1, new TocatliHonorGuard());
        harness.addToBattlefield(player1, new SuturePriest());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);

        // Resolve creature spell — Grizzly Bears enters under player2, Suture Priest does NOT trigger
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    // ===== ETB triggers work normally after removal =====

    @Test
    @DisplayName("After Tocatli Honor Guard leaves the battlefield, ETB triggers work normally")
    void etbTriggersWorkAfterRemoval() {
        TocatliHonorGuard guard = new TocatliHonorGuard();
        harness.addToBattlefield(player1, guard);

        // Remove the guard from the battlefield
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Tocatli Honor Guard"));

        // Now cast a creature with ETB — it should trigger normally
        harness.setHand(player1, List.of(new PriestOfUrabrask()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);

        // Resolve creature spell — Priest enters, ETB goes on stack
        harness.passBothPriorities();
        // Resolve ETB trigger — 3 red mana is added
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Priest of Urabrask"));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    // ===== Affects both players =====

    @Test
    @DisplayName("Suppresses ETB triggers for opponent's creatures too")
    void suppressesOpponentCreatureETB() {
        harness.addToBattlefield(player1, new TocatliHonorGuard());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new PriestOfUrabrask()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castCreature(player2, 0);

        // Resolve creature spell — Priest enters but ETB does not trigger
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Priest of Urabrask"));
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(0);
    }
}
