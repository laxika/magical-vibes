package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImperialAerosaur.class, RaptorCompanion.class})
class ImperialAerosaurTest extends BaseCardTest {

    @Test
    @DisplayName("Casting does not choose the ETB target")
    void castingDoesNotChooseEtbTarget() {
        harness.addToBattlefield(player1, new RaptorCompanion());
        harness.setHand(player1, List.of(new ImperialAerosaur()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getTargetId()).isNull();
    }

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack")
    void resolvingPutsEtbOnStack() {
        harness.addToBattlefield(player1, new RaptorCompanion());
        harness.setHand(player1, List.of(new ImperialAerosaur()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player1, "Raptor Companion");
        harness.castCreature(player1, 0);

        // Resolve creature spell — enters battlefield, ETB triggers
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);

        harness.assertOnBattlefield(player1, "Imperial Aerosaur");

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("ETB resolves and gives target creature +1/+1 and flying")
    void etbBoostsAndGrantsFlying() {
        harness.addToBattlefield(player1, new RaptorCompanion());
        harness.setHand(player1, List.of(new ImperialAerosaur()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player1, "Raptor Companion");
        harness.castCreature(player1, 0);

        // Resolve creature spell
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        // Resolve ETB triggered ability
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();

        Permanent companion = findPermanent(player1, "Raptor Companion");
        assertThat(companion.getPowerModifier()).isEqualTo(1);
        assertThat(companion.getToughnessModifier()).isEqualTo(1);
        assertThat(companion.getEffectivePower()).isEqualTo(4);
        assertThat(companion.getEffectiveToughness()).isEqualTo(2);
        assertThat(companion.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Boost and flying wear off at end of turn")
    void boostAndFlyingWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new RaptorCompanion());
        harness.setHand(player1, List.of(new ImperialAerosaur()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player1, "Raptor Companion");
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // Resolve creature
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities(); // Resolve ETB

        Permanent companion = findPermanent(player1, "Raptor Companion");
        assertThat(companion.getPowerModifier()).isEqualTo(1);
        assertThat(companion.getGrantedKeywords()).contains(Keyword.FLYING);

        // Advance through the end step — cleanup resets modifiers and granted keywords
        harness.forceStep(TurnStep.END_STEP);
        assertThat(companion.getPowerModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, companion, Keyword.FLYING)).isTrue();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(companion.getPowerModifier()).isEqualTo(0);
        assertThat(companion.getToughnessModifier()).isEqualTo(0);
        assertThat(companion.getEffectivePower()).isEqualTo(3);
        assertThat(companion.getEffectiveToughness()).isEqualTo(1);
        assertThat(companion.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Can target another Imperial Aerosaur")
    void canTargetAnotherImperialAerosaur() {
        harness.addToBattlefield(player1, new ImperialAerosaur());
        harness.setHand(player1, List.of(new ImperialAerosaur()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID aerosaurOnBattlefieldId = harness.getPermanentId(player1, "Imperial Aerosaur");

        // The second Aerosaur can boost the first when it enters.
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve creature
        harness.handlePermanentChosen(player1, aerosaurOnBattlefieldId);

        // ETB should target the first Aerosaur, not the one that just entered
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(aerosaurOnBattlefieldId);
    }

    @Test
    @DisplayName("Cannot target opponent's creature")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new RaptorCompanion());
        harness.setHand(player1, List.of(new ImperialAerosaur()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        UUID opponentCreatureId = harness.getPermanentId(player2, "Raptor Companion");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(ownCreature.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreatureId))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();
        assertThat(ownCreature.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can cast without a target when no other creatures you control")
    void canCastWithoutTarget() {
        harness.setHand(player1, List.of(new ImperialAerosaur()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("ETB cannot be put on the stack when no legal target exists")
    void etbHasNoLegalTargetWhenAlone() {
        harness.setHand(player1, List.of(new ImperialAerosaur()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);

        // Resolve creature spell
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Imperial Aerosaur");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new RaptorCompanion());
        harness.setHand(player1, List.of(new ImperialAerosaur()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player1, "Raptor Companion");
        harness.castCreature(player1, 0);

        // Resolve creature spell — ETB on stack
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, targetId);

        // Remove target before ETB resolves
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(targetId));

        // Resolve ETB — fizzles
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    void cannotTargetEnteringAerosaur() {
        Permanent companion = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        Permanent aerosaur = harness.enterBattlefieldAndReturn(player1, new ImperialAerosaur());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(companion.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, aerosaur.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, companion.getId());
        harness.passBothPriorities();
        assertThat(companion.getPowerModifier()).isEqualTo(1);
        assertThat(aerosaur.getPowerModifier()).isZero();
    }

    @Test
    void enteringWithoutBeingCastStillBoostsAndGrantsFlying() {
        Permanent companion = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        harness.enterBattlefieldAndReturn(player1, new ImperialAerosaur());
        harness.handlePermanentChosen(player1, companion.getId());
        harness.passBothPriorities();

        assertThat(companion.getPowerModifier()).isEqualTo(1);
        assertThat(companion.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, companion, Keyword.FLYING)).isTrue();
    }

    @Test
    void targetBecomingOpponentsCreatureMakesAbilityFailToResolve() {
        Permanent companion = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        harness.enterBattlefieldAndReturn(player1, new ImperialAerosaur());
        harness.handlePermanentChosen(player1, companion.getId());
        gd.playerBattlefields.get(player1.getId()).remove(companion);
        gd.playerBattlefields.get(player2.getId()).add(companion);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(companion.getPowerModifier()).isZero();
        assertThat(companion.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, companion, Keyword.FLYING)).isFalse();
    }

    @Test
    void removingSourceDoesNotPreventAbilityFromResolving() {
        Permanent companion = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        Permanent aerosaur = harness.enterBattlefieldAndReturn(player1, new ImperialAerosaur());
        harness.handlePermanentChosen(player1, companion.getId());
        gd.playerBattlefields.get(player1.getId()).remove(aerosaur);
        harness.passBothPriorities();

        assertThat(companion.getPowerModifier()).isEqualTo(1);
        assertThat(companion.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, companion, Keyword.FLYING)).isTrue();
    }
}
