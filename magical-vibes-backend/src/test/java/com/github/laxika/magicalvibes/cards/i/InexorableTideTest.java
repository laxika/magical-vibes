package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InexorableTideTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has proliferate on spell cast trigger")
    void hasProliferateOnSpellCastEffect() {
        InexorableTide card = new InexorableTide();

        assertThat(card.getEffects(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)).singleElement()
                .isInstanceOf(ProliferateEffect.class);
    }

    // ===== Triggered ability: proliferate on own spell cast =====

    @Test
    @DisplayName("Casting a spell triggers proliferate")
    void castingSpellTriggersProliferate() {
        harness.addToBattlefield(player1, new InexorableTide());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setPlusOnePlusOneCounters(1);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        // Triggered ability should be on the stack
        long triggeredOnStack = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Inexorable Tide"))
                .count();
        assertThat(triggeredOnStack).isEqualTo(1);

        harness.passBothPriorities(); // resolve triggered ability (proliferate)

        // Choose the bears for proliferate
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent casting a spell does not trigger proliferate")
    void opponentCastingSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new InexorableTide());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setPlusOnePlusOneCounters(1);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Spellbook()));

        harness.castArtifact(player2, 0);

        // Only the Spellbook spell should be on the stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isNotEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(bears.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Proliferate adds -1/-1 counters to chosen permanents")
    void proliferateAddsMinusCounters() {
        harness.addToBattlefield(player1, new InexorableTide());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setMinusOneMinusOneCounters(1);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve triggered ability

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getMinusOneMinusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Proliferate can choose no permanents")
    void proliferateCanChooseNone() {
        harness.addToBattlefield(player1, new InexorableTide());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setPlusOnePlusOneCounters(1);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        // Counter unchanged
        assertThat(bears.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Proliferate does nothing when no permanents have counters")
    void proliferateNoEligiblePermanents() {
        harness.addToBattlefield(player1, new InexorableTide());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        // No MULTI_PERMANENT_CHOICE — no eligible permanents
        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getPlusOnePlusOneCounters()).isZero();
        assertThat(bears.getMinusOneMinusOneCounters()).isZero();
    }

    @Test
    @DisplayName("Triggers proliferate for each spell cast")
    void triggersForEachSpellCast() {
        harness.addToBattlefield(player1, new InexorableTide());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setPlusOnePlusOneCounters(1);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Spellbook(), new Spellbook()));

        // Cast first spell
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve proliferate trigger
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        assertThat(bears.getPlusOnePlusOneCounters()).isEqualTo(2);

        harness.passBothPriorities(); // resolve Spellbook spell

        // Cast second spell
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve proliferate trigger
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        assertThat(bears.getPlusOnePlusOneCounters()).isEqualTo(3);
    }
}
