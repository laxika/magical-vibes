package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RhonassMonumentTest extends BaseCardTest {

    // ===== Cost reduction =====

    @Test
    @DisplayName("Green creature spells cost {1} less")
    void greenCreatureSpellsCostOneLess() {
        harness.addToBattlefield(player1, new RhonassMonument());
        // Grizzly Bears costs {1}{G}; with the {1} reduction it is castable for {G} alone
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Non-green creature spells are not reduced")
    void nonGreenCreaturesNotReduced() {
        harness.addToBattlefield(player1, new RhonassMonument());
        // Hill Giant costs {3}{R} — not green, so {2}{R} worth of mana is not enough
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Creature-cast pump trigger =====

    @Test
    @DisplayName("Casting a creature gives a chosen creature you control +2/+2 and trample")
    void castingCreatureBoostsControlledCreature() {
        harness.addToBattlefield(player1, new RhonassMonument());
        Permanent existing = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        // The trigger prompts for a "creature you control" target
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, existing.getId());
        harness.passBothPriorities(); // resolve the trigger

        assertThat(existing.getPowerModifier()).isEqualTo(2);
        assertThat(existing.getToughnessModifier()).isEqualTo(2);
        assertThat(existing.getEffectivePower()).isEqualTo(4);
        assertThat(existing.getEffectiveToughness()).isEqualTo(4);
        assertThat(existing.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("The +2/+2 and trample wear off at end of turn")
    void boostAndTrampleWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new RhonassMonument());
        Permanent existing = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, existing.getId());
        harness.passBothPriorities(); // resolve the trigger
        harness.passBothPriorities(); // resolve the creature spell

        assertThat(existing.getPowerModifier()).isEqualTo(2);
        assertThat(existing.getGrantedKeywords()).contains(Keyword.TRAMPLE);

        // Advance to end step — modifiers and granted keywords reset
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(existing.getPowerModifier()).isEqualTo(0);
        assertThat(existing.getToughnessModifier()).isEqualTo(0);
        assertThat(existing.getEffectivePower()).isEqualTo(2);
        assertThat(existing.getEffectiveToughness()).isEqualTo(2);
        assertThat(existing.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("The trigger can only target a creature you control, not an opponent's")
    void triggerCannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new RhonassMonument());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        // No creature you control exists, so the trigger has no legal target and is not put on the stack
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
        assertThat(opponentCreature.getPowerModifier()).isZero();
    }

    // ===== Non-creature spell does not trigger =====

    @Test
    @DisplayName("Casting a non-creature spell does not trigger the pump")
    void nonCreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new RhonassMonument());
        Permanent existing = addCreatureReady(player1, new GrizzlyBears()); // a legal target exists
        // Spellbook is a {2} artifact — not a creature spell
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(existing.getPowerModifier()).isZero();
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }
}
