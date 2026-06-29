package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AnointedDeaconTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to BEGINNING_OF_COMBAT, triggers fire
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Anointed Deacon has beginning-of-combat triggered MayEffect wrapping BoostTargetCreatureEffect")
    void hasCorrectEffect() {
        AnointedDeacon card = new AnointedDeacon();

        assertThat(card.getEffects(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED).getFirst())
                .isInstanceOf(MayEffect.class);

        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(BoostTargetCreatureEffect.class);

        BoostTargetCreatureEffect boost = (BoostTargetCreatureEffect) mayEffect.wrapped();
        assertThat(boost.powerBoost()).isEqualTo(2);
        assertThat(boost.toughnessBoost()).isEqualTo(0);

        // Target filter is set on the card (used at resolution time), but the card itself
        // does not need a target when cast — targeting happens on the triggered ability.
        assertThat(card.getTargetFilter()).isNotNull();
    }

    // ===== Accepting the may ability and targeting a Vampire =====

    @Test
    @DisplayName("Accepting the may ability and targeting a Vampire gives it +2/+0")
    void acceptAndTargetVampireGivesBoost() {
        harness.addToBattlefield(player1, new AnointedDeacon());
        harness.addToBattlefield(player1, new ChildOfNight());
        UUID vampireId = harness.getPermanentId(player1, "Child of Night");

        advanceToCombat(player1);

        // MayEffect is on the stack — resolve it to get the may prompt
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        harness.handleMayAbilityChosen(player1, true);

        // Should be prompted for target selection
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        harness.handlePermanentChosen(player1, vampireId);

        Permanent vampire = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(vampireId))
                .findFirst().orElseThrow();
        assertThat(vampire.getPowerModifier()).isEqualTo(2);
        assertThat(vampire.getToughnessModifier()).isEqualTo(0);
    }

    // ===== Can target itself (Anointed Deacon is a Vampire) =====

    @Test
    @DisplayName("Can target itself since it is a Vampire")
    void canTargetItself() {
        harness.addToBattlefield(player1, new AnointedDeacon());
        UUID deaconId = harness.getPermanentId(player1, "Anointed Deacon");

        advanceToCombat(player1);
        harness.passBothPriorities(); // resolve stack entry → may prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, deaconId);

        Permanent deacon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(deaconId))
                .findFirst().orElseThrow();
        assertThat(deacon.getPowerModifier()).isEqualTo(2);
        assertThat(deacon.getToughnessModifier()).isEqualTo(0);
    }

    // ===== Declining the may ability =====

    @Test
    @DisplayName("Declining the may ability does not boost any creature")
    void declineMayAbilityNoBoost() {
        harness.addToBattlefield(player1, new AnointedDeacon());
        harness.addToBattlefield(player1, new ChildOfNight());
        UUID vampireId = harness.getPermanentId(player1, "Child of Night");

        advanceToCombat(player1);
        harness.passBothPriorities(); // resolve stack entry → may prompt
        harness.handleMayAbilityChosen(player1, false);

        Permanent vampire = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(vampireId))
                .findFirst().orElseThrow();
        assertThat(vampire.getPowerModifier()).isEqualTo(0);
        assertThat(vampire.getToughnessModifier()).isEqualTo(0);
    }

    // ===== Does not trigger during opponent's combat =====

    @Test
    @DisplayName("Does not trigger during opponent's combat")
    void doesNotTriggerDuringOpponentCombat() {
        harness.addToBattlefield(player1, new AnointedDeacon());
        harness.addToBattlefield(player1, new ChildOfNight());

        advanceToCombat(player2); // opponent's combat
        harness.passBothPriorities();

        // No may prompt — stack should be empty and no interaction awaiting
        assertThat(gd.stack).isEmpty();
    }

    // ===== Can target opponent's Vampire =====

    @Test
    @DisplayName("Can target opponent's Vampire")
    void canTargetOpponentVampire() {
        harness.addToBattlefield(player1, new AnointedDeacon());
        harness.addToBattlefield(player2, new ChildOfNight());
        UUID vampireId = harness.getPermanentId(player2, "Child of Night");

        advanceToCombat(player1);
        harness.passBothPriorities(); // resolve stack entry → may prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, vampireId);

        Permanent vampire = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(vampireId))
                .findFirst().orElseThrow();
        assertThat(vampire.getPowerModifier()).isEqualTo(2);
        assertThat(vampire.getToughnessModifier()).isEqualTo(0);
    }

    // ===== Boost wears off at end of turn =====

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new AnointedDeacon());
        harness.addToBattlefield(player1, new ChildOfNight());
        UUID vampireId = harness.getPermanentId(player1, "Child of Night");

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, vampireId);

        Permanent vampire = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(vampireId))
                .findFirst().orElseThrow();
        assertThat(vampire.getPowerModifier()).isEqualTo(2);

        // Advance to end step — modifiers reset
        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(vampire.getPowerModifier()).isEqualTo(0);
        assertThat(vampire.getToughnessModifier()).isEqualTo(0);
    }
}
