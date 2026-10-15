package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.Castle;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Nightmare.class, Swamp.class, Plains.class, Castle.class, GiantSpider.class, GrizzlyBears.class})
class NightmareTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Nightmare puts it on the stack")
    void castingPutsOnStack() {
        Nightmare nightmare = new Nightmare();
        harness.castFromHand(player1, nightmare, "{5}{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(nightmare);
    }

    @Test
    @DisplayName("Nightmare dies to state-based actions with no Swamps")
    void diesWithNoSwamps() {
        Nightmare nightmare = new Nightmare();
        harness.castFromHand(player1, nightmare, "{5}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == nightmare);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card == nightmare);
    }

    @Test
    @DisplayName("Nightmare survives when you control a Swamp")
    void survivesWithSwamp() {
        harness.addToBattlefield(player1, new Swamp());
        Nightmare nightmare = new Nightmare();
        harness.castFromHand(player1, nightmare, "{5}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == nightmare);
    }

    @Test
    @DisplayName("Nightmare power and toughness equal number of Swamps you control")
    void ptEqualsControlledSwamps() {
        Permanent nightmare = addCreatureReady(player1, new Nightmare());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, nightmare)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nightmare)).isEqualTo(2);
    }

    @Test
    @DisplayName("Nightmare counts only your Swamps, not opponent Swamps")
    void countsOnlyControllersSwamps() {
        Permanent nightmare = addCreatureReady(player1, new Nightmare());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        assertThat(gqs.getEffectivePower(gd, nightmare)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nightmare)).isEqualTo(1);
    }

    @Test
    @DisplayName("Nightmare power and toughness update when Swamps change")
    void ptUpdatesWhenSwampsChange() {
        Permanent nightmare = addCreatureReady(player1, new Nightmare());
        Permanent firstSwamp = harness.addToBattlefieldAndReturn(player1, new Swamp());

        assertThat(gqs.getEffectivePower(gd, nightmare)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nightmare)).isEqualTo(1);

        Permanent secondSwamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        assertThat(gqs.getEffectivePower(gd, nightmare)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nightmare)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(firstSwamp);
        gd.playerBattlefields.get(player1.getId()).remove(secondSwamp);
        assertThat(gqs.getEffectivePower(gd, nightmare)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, nightmare)).isEqualTo(0);
    }

    @Test
    @DisplayName("Nightmare characteristic-defining P/T stacks with a static toughness bonus")
    void ptStacksWithOtherStaticBonuses() {
        Permanent nightmare = addCreatureReady(player1, new Nightmare());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Castle());

        assertThat(gqs.getEffectivePower(gd, nightmare)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nightmare)).isEqualTo(4);
    }

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Nightmare")
    void flyingPreventsGroundBlocker() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent nightmare = addCreatureReady(player1, new Nightmare());
        harness.addToBattlefield(player1, new Swamp());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(nightmare);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("A creature with reach can block Nightmare")
    void reachAllowsBlocking() {
        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        Permanent nightmare = addCreatureReady(player1, new Nightmare());
        harness.addToBattlefield(player1, new Swamp());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(nightmare);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.getBlockingTargetIds()).containsExactly(nightmare.getId());
    }

    @Test
    @DisplayName("Nightmare counts tapped Swamps as well as untapped Swamps")
    void countsTappedSwamps() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        swamp.tap();
        harness.addToBattlefield(player1, new Swamp());
        Permanent nightmare = addCreatureReady(player1, new Nightmare());

        assertThat(gqs.getEffectivePower(gd, nightmare)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nightmare)).isEqualTo(2);
    }

    @Test
    @DisplayName("Nightmare's defining ability works in hand and graveyard using its owner's Swamps")
    void definingAbilityWorksOutsideBattlefield() {
        Nightmare inHand = new Nightmare();
        Nightmare inGraveyard = new Nightmare();
        harness.setHand(player1, List.of(inHand));
        harness.setGraveyard(player1, List.of(inGraveyard));
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        assertThat(gqs.getEffectiveCardPower(gd, inHand)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, inHand)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardPower(gd, inGraveyard)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, inGraveyard)).isEqualTo(1);

        harness.addToBattlefield(player1, new Swamp());

        assertThat(gqs.getEffectiveCardPower(gd, inHand)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, inHand)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardPower(gd, inGraveyard)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, inGraveyard)).isEqualTo(2);
    }

    @Test
    @DisplayName("A flying creature can block Nightmare")
    void flyingAllowsBlocking() {
        harness.addToBattlefield(player2, new Swamp());
        Permanent blocker = addCreatureReady(player2, new Nightmare());
        Permanent nightmare = addCreatureReady(player1, new Nightmare());
        harness.addToBattlefield(player1, new Swamp());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(nightmare);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.getBlockingTargetIds()).containsExactly(nightmare.getId());
    }
}
