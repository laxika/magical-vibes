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

        assertThat(p1a.getEffectivePower()).isEqualTo(4);
        assertThat(p1a.getEffectiveToughness()).isEqualTo(5);
        assertThat(p1b.getEffectivePower()).isEqualTo(4);
        assertThat(p1b.getEffectiveToughness()).isEqualTo(5);
        assertThat(p1a.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(p1b.hasKeyword(Keyword.TRAMPLE)).isTrue();

        assertThat(p2.getEffectivePower()).isEqualTo(1);
        assertThat(p2.getEffectiveToughness()).isEqualTo(2);
        assertThat(p2.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Overrun trample assigns excess damage to defending player")
    void trampleAssignsExcessDamageToDefender() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new WoodlandDruid());
        Permanent blocker = addCreatureReady(player2, new WoodlandDruid());

        harness.castFromHand(player1, new Overrun(), "{2}{G}{G}{G}");
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

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

        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(5);
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(1);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Only creatures present at resolution receive Overrun's effects")
    void onlyCreaturesPresentAtResolutionAreAffected() {
        Permanent existingCreature = addCreatureReady(player1, new WoodlandDruid());

        harness.castFromHand(player1, new Overrun(), "{2}{G}{G}{G}");
        harness.passBothPriorities();

        Permanent laterCreature = addCreatureReady(player1, new WoodlandDruid());

        assertThat(existingCreature.getEffectivePower()).isEqualTo(4);
        assertThat(existingCreature.getEffectiveToughness()).isEqualTo(5);
        assertThat(existingCreature.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(laterCreature.getEffectivePower()).isEqualTo(1);
        assertThat(laterCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(laterCreature.hasKeyword(Keyword.TRAMPLE)).isFalse();
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
