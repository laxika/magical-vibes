package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.UntapSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TraxosScourgeOfKroogTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Traxos has EntersTappedEffect and DoesntUntapDuringUntapStepEffect as static effects")
    void hasCorrectStaticEffects() {
        TraxosScourgeOfKroog card = new TraxosScourgeOfKroog();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.STATIC))
                .anyMatch(e -> e instanceof EntersTappedEffect);
        assertThat(card.getEffects(EffectSlot.STATIC))
                .anyMatch(e -> e instanceof DoesntUntapDuringUntapStepEffect);
    }

    @Test
    @DisplayName("Traxos has historic spell-cast trigger with untap self")
    void hasHistoricTrigger() {
        TraxosScourgeOfKroog card = new TraxosScourgeOfKroog();

        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst())
                .isInstanceOf(SpellCastTriggerEffect.class);

        SpellCastTriggerEffect trigger = (SpellCastTriggerEffect) card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();
        assertThat(trigger.spellFilter()).isInstanceOf(CardIsHistoricPredicate.class);
        assertThat(trigger.resolvedEffects()).hasSize(1);
        assertThat(trigger.resolvedEffects().getFirst()).isInstanceOf(UntapSelfEffect.class);
    }

    // ===== Enters tapped =====

    @Test
    @DisplayName("Traxos enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new TraxosScourgeOfKroog()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent traxos = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Traxos, Scourge of Kroog"))
                .findFirst().orElseThrow();

        assertThat(traxos.isTapped()).isTrue();
    }

    // ===== Doesn't untap during untap step =====

    @Test
    @DisplayName("Tapped Traxos does not untap during controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent traxosPerm = addTraxosReady(player1);
        traxosPerm.tap();

        advanceToNextTurn(player2);

        assertThat(traxosPerm.isTapped()).isTrue();
    }

    // ===== Historic spell trigger untaps Traxos =====

    @Test
    @DisplayName("Casting an artifact triggers untap Traxos")
    void artifactSpellTriggersUntap() {
        Permanent traxosPerm = addTraxosReady(player1);
        traxosPerm.tap();
        assertThat(traxosPerm.isTapped()).isTrue();

        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        // Spellbook on stack + triggered ability
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Traxos, Scourge of Kroog"));
    }

    @Test
    @DisplayName("Resolving artifact-triggered ability untaps Traxos")
    void artifactTriggerUntapsTraxos() {
        Permanent traxosPerm = addTraxosReady(player1);
        traxosPerm.tap();
        assertThat(traxosPerm.isTapped()).isTrue();

        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        // Resolve the triggered ability (LIFO — trigger on top)
        harness.passBothPriorities();

        assertThat(traxosPerm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting a non-historic creature does not trigger untap")
    void nonHistoricDoesNotTrigger() {
        addTraxosReady(player1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        // Only the creature spell on the stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Opponent casting an artifact does not trigger controller's Traxos")
    void opponentHistoricDoesNotTrigger() {
        addTraxosReady(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Spellbook()));

        harness.castArtifact(player2, 0);

        GameData gd = harness.getGameData();
        // Only the artifact spell on stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    // ===== Helpers =====

    private Permanent addTraxosReady(Player player) {
        Permanent perm = new Permanent(new TraxosScourgeOfKroog());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn
    }
}
