package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.w.WoodlandDruid;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Overrun.class, WoodlandDruid.class})
class OverrunTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Overrun gives own creatures +3/+3 and trample")
    void resolvesAndBuffsOwnCreatures() {
        Permanent p1a = addCreatureReady(player1, new WoodlandDruid());
        Permanent p1b = addCreatureReady(player1, new WoodlandDruid());
        Permanent p2 = addCreatureReady(player2, new WoodlandDruid());

        harness.castFromHand(player1, new Overrun(), "{2}{G}{G}{G}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, p1a)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, p1a)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, p1b)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, p1b)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, p1a, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, p1b, Keyword.TRAMPLE)).isTrue();

        assertThat(gqs.getEffectivePower(gd, p2)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, p2)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, p2, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Overrun trample assigns excess damage to defending player")
    void trampleAssignsExcessDamageToDefender() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new WoodlandDruid());
        Permanent blocker = addCreatureReady(player2, new WoodlandDruid());

        harness.castFromHand(player1, new Overrun(), "{2}{G}{G}{G}");
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // 4/5 trample blocked by 1/2 → assign lethal to blocker, excess to player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 2
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player1, "Woodland Druid");
        harness.assertInGraveyard(player2, "Woodland Druid");
    }

    @Test
    @DisplayName("Overrun effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new WoodlandDruid());

        harness.castFromHand(player1, new Overrun(), "{2}{G}{G}{G}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Only creatures present at resolution receive Overrun's effects")
    void onlyCreaturesPresentAtResolutionAreAffected() {
        Permanent existingCreature = addCreatureReady(player1, new WoodlandDruid());

        harness.castFromHand(player1, new Overrun(), "{2}{G}{G}{G}");
        harness.passBothPriorities();

        Permanent laterCreature = addCreatureReady(player1, new WoodlandDruid());

        assertThat(gqs.getEffectivePower(gd, existingCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, existingCreature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, existingCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering while Overrun is on the stack receive its effects")
    void creaturesEnteringBeforeResolutionAreAffected() {
        harness.castFromHand(player1, new Overrun(), "{2}{G}{G}{G}");
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new WoodlandDruid());

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Repeated Overruns add their boosts and both expire at end of turn")
    void repeatedOverrunsStackAndExpire() {
        Permanent creature = addCreatureReady(player1, new WoodlandDruid());

        harness.castFromHand(player1, new Overrun(), "{2}{G}{G}{G}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new Overrun(), "{2}{G}{G}{G}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Resolves with no creatures on the battlefield")
    void resolvesWithNoCreatures() {

        harness.castFromHand(player1, new Overrun(), "{2}{G}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting Overrun puts it on stack as sorcery spell")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new Overrun(), "{2}{G}{G}{G}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
    }
}
