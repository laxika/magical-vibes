package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BurrentonShieldBearers;
import com.github.laxika.magicalvibes.cards.c.CennsTactician;
import com.github.laxika.magicalvibes.cards.m.Mutavault;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeedPrunerPoplar.class, BurrentonShieldBearers.class, Mutavault.class, CennsTactician.class})
class WeedPrunerPoplarTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger presents mandatory target selection")
    void upkeepTriggerPresentsTargetSelection() {
        Permanent poplar = addCreatureReady(player1, new WeedPrunerPoplar());
        Permanent target = addCreatureReady(player2, new BurrentonShieldBearers());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId())
                .doesNotContain(poplar.getId());
    }

    @Test
    @DisplayName("Chosen creature gets -1/-1 until end of turn")
    void chosenCreatureGetsMinusOneMinusOne() {
        addCreatureReady(player1, new WeedPrunerPoplar());
        Permanent bears = addCreatureReady(player2, new BurrentonShieldBearers());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());

        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(-1);
        assertThat(bears.getToughnessModifier()).isEqualTo(-1);
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target the controller's own creatures")
    void canTargetOwnCreature() {
        addCreatureReady(player1, new WeedPrunerPoplar());
        Permanent bears = addCreatureReady(player1, new BurrentonShieldBearers());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(-1);
        assertThat(bears.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Can target another copy of Weed-Pruner Poplar")
    void canTargetAnotherCopyOfItself() {
        Permanent source = addCreatureReady(player1, new WeedPrunerPoplar());
        Permanent otherPoplar = addCreatureReady(player2, new WeedPrunerPoplar());

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(otherPoplar.getId())
                .doesNotContain(source.getId());

        harness.handlePermanentChosen(player1, otherPoplar.getId());
        harness.passBothPriorities();

        assertThat(otherPoplar.getPowerModifier()).isEqualTo(-1);
        assertThat(otherPoplar.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Target selection includes creatures but not lands")
    void targetSelectionIncludesCreaturesOnly() {
        Permanent source = addCreatureReady(player1, new WeedPrunerPoplar());
        Permanent creature = addCreatureReady(player2, new BurrentonShieldBearers());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mutavault());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(creature.getId())
                .doesNotContain(source.getId(), land.getId());
    }

    @Test
    @DisplayName("Presents no target choice when the Poplar is the only creature")
    void presentsNoTargetChoiceWhenOnlyCreature() {
        addCreatureReady(player1, new WeedPrunerPoplar());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Triggers only during its controller's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        addCreatureReady(player1, new WeedPrunerPoplar());
        Permanent target = addCreatureReady(player2, new BurrentonShieldBearers());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The -1/-1 wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        addCreatureReady(player1, new WeedPrunerPoplar());
        Permanent bears = addCreatureReady(player2, new BurrentonShieldBearers());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("A creature reduced to zero toughness dies")
    void zeroToughnessTargetDies() {
        addCreatureReady(player1, new WeedPrunerPoplar());
        Permanent target = addCreatureReady(player2, new CennsTactician());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Cenn's Tactician");
        harness.assertInGraveyard(player2, "Cenn's Tactician");
    }

    @Test
    @DisplayName("The trigger resolves after its source leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Permanent source = addCreatureReady(player1, new WeedPrunerPoplar());
        Permanent target = addCreatureReady(player2, new BurrentonShieldBearers());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        source.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Weed-Pruner Poplar");
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("A removed target is not replaced with another creature")
    void removedTargetDoesNotRedirectTrigger() {
        Permanent source = addCreatureReady(player1, new WeedPrunerPoplar());
        Permanent target = addCreatureReady(player2, new BurrentonShieldBearers());
        Permanent other = addCreatureReady(player2, new BurrentonShieldBearers());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        target.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player2, "Burrenton Shield-Bearers");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(source.getPowerModifier()).isZero();
        assertThat(source.getToughnessModifier()).isZero();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
    }
}
