package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.ArashinCleric;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinHeelcutter.class, ArashinCleric.class})
class GoblinHeelcutterTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking makes the target creature unable to block this turn")
    void attackMakesTargetUnableToBlock() {
        addCreatureReady(player1, new GoblinHeelcutter());
        Permanent blocker = addCreatureReady(player2, new ArashinCleric());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        resolveAllTriggers();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Normal cast does not grant haste or return the creature at end step")
    void normalCastDoesNotUseDash() {
        harness.setHand(player1, List.of(new GoblinHeelcutter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent heelcutter = findPermanent(player1, "Goblin Heelcutter");
        assertThat(heelcutter.hasKeyword(Keyword.HASTE)).isFalse();

        harness.passUntil(TurnStep.END_STEP);

        assertThat(findPermanent(player1, "Goblin Heelcutter")).isSameAs(heelcutter);
    }

    @Test
    @DisplayName("Dash grants haste and returns the creature to its owner's hand at end step")
    void dashGrantsHasteAndReturnsAtEndStep() {
        harness.setHand(player1, List.of(new GoblinHeelcutter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent heelcutter = findPermanent(player1, "Goblin Heelcutter");
        assertThat(heelcutter.hasKeyword(Keyword.HASTE)).isTrue();

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInHand(player1, "Goblin Heelcutter");
        harness.assertNotOnBattlefield(player1, "Goblin Heelcutter");
    }

    @Test
    @DisplayName("Dash establishes its delayed return without an enters-the-battlefield trigger")
    void dashDoesNotCreateAnEntryTrigger() {
        harness.setHand(player1, List.of(new GoblinHeelcutter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goblin Heelcutter");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The delayed dash return waits for resolution at the end step")
    void dashReturnUsesTheStack() {
        harness.setHand(player1, List.of(new GoblinHeelcutter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        resolveAllTriggers();

        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Goblin Heelcutter");
        harness.assertNotInHand(player1, "Goblin Heelcutter");
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertInHand(player1, "Goblin Heelcutter");
        harness.assertNotOnBattlefield(player1, "Goblin Heelcutter");
    }

    @Test
    @DisplayName("The attack trigger can target a creature controlled by the attacker")
    void attackCanTargetOwnCreature() {
        addCreatureReady(player1, new GoblinHeelcutter());
        Permanent ownCreature = addCreatureReady(player1, new ArashinCleric());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, ownCreature.getId());
        resolveAllTriggers();

        assertThat(ownCreature.isCantBlockThisTurn()).isTrue();
    }
}
