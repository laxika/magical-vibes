package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HardenedEscort.class, GrizzlyBears.class, Murder.class})
class HardenedEscortTest extends BaseCardTest {

    @Test
    @DisplayName("Attack trigger targets another creature I control")
    void attackTriggerRestrictsTargets() {
        Permanent escort = addCreatureReady(player1, new HardenedEscort());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownCreature.getId())
                .doesNotContain(escort.getId(), opponentCreature.getId());
    }

    @Test
    @DisplayName("Attack trigger gives the target +1/+0 and indestructible until end of turn")
    void attackTriggerBoostsAndGrantsIndestructible() {
        addCreatureReady(player1, new HardenedEscort());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(opponentCreature.getPowerModifier()).isEqualTo(0);
        assertThat(opponentCreature.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Attack trigger effects wear off at end of turn")
    void attackTriggerEffectsWearOffAtEndOfTurn() {
        addCreatureReady(player1, new HardenedEscort());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Attacking alone cannot target the Escort itself")
    void attackingAloneHasNoLegalTarget() {
        Permanent escort = addCreatureReady(player1, new HardenedEscort());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(escort.getPowerModifier()).isZero();
        assertThat(escort.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Another Escort is a legal target even with the same name")
    void anotherEscortCanReceiveTheBonus() {
        Permanent attacker = addCreatureReady(player1, new HardenedEscort());
        Permanent target = addCreatureReady(player1, new HardenedEscort());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The attack trigger resolves after the attacking Escort is destroyed")
    void triggerSurvivesRemovalOfItsSource() {
        Permanent attacker = addCreatureReady(player1, new HardenedEscort());
        Permanent target = addCreatureReady(player1, new HardenedEscort());
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.castAndResolveInstant(player2, 0, attacker.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The trigger does not redirect its effects when the target is destroyed")
    void removedTargetDoesNotGrantBonusToAnotherCreature() {
        Permanent attacker = addCreatureReady(player1, new HardenedEscort());
        Permanent target = addCreatureReady(player1, new HardenedEscort());
        Permanent other = addCreatureReady(player1, new HardenedEscort());
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The protected creature survives lethal damage and a destroy spell")
    void grantedIndestructiblePreventsDestruction() {
        addCreatureReady(player1, new HardenedEscort());
        Permanent target = addCreatureReady(player1, new HardenedEscort());
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        target.setMarkedDamage(4);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);

        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }
}
