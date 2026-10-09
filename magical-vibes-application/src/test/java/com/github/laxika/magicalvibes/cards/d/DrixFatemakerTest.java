package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.Hullcarver;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({DrixFatemaker.class, Hullcarver.class})
class DrixFatemakerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on the target creature when it enters")
    void etbPutsCounterOnTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Hullcarver());

        harness.setHand(player1, List.of(new DrixFatemaker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gives trample to your creatures with +1/+1 counters")
    void givesTrampleToYourCounteredCreatures() {
        Permanent drix = harness.addToBattlefieldAndReturn(player1, new DrixFatemaker());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new Hullcarver());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new Hullcarver());
        ownCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, drix, Keyword.TRAMPLE)).isFalse();

        drix.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, drix, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Warp casts it for {1}{G} and exiles it at the next end step")
    void warpCastsAndExilesAtNextEndStep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Hullcarver());
        DrixFatemaker drixCard = new DrixFatemaker();
        harness.setHand(player1, List.of(drixCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.passUntil(TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Drix Fatemaker");
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(drixCard.getId())).isNotNull();
    }

    @Test
    @DisplayName("Trample tracks counter removal and disappears when Drix leaves")
    void trampleTracksCountersAndSource() {
        Permanent drix = harness.addToBattlefieldAndReturn(player1, new DrixFatemaker());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Hullcarver());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(drix);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Casting normally does not exile Drix at the end step")
    void normalCastStaysOnBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Hullcarver());
        harness.setHand(player1, List.of(new DrixFatemaker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Drix Fatemaker");
    }

    @Test
    @DisplayName("Warped Drix can be recast for its normal cost on a later turn")
    void recastWarpedDrixFromExile() {
        harness.setHand(player2, List.of());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Hullcarver());
        DrixFatemaker drix = new DrixFatemaker();
        harness.setHand(player1, List.of(drix));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castWithAlternateCost(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(gd.findExiledCard(drix.getId())).isNotNull();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromExile(player1, drix.getId(), target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Drix Fatemaker");
    }

    @Test
    @DisplayName("An ETB target that leaves receives no counter and Drix remains")
    void targetLeavesBeforeCounterAbilityResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Hullcarver());
        harness.setHand(player1, List.of(new DrixFatemaker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Drix Fatemaker");
    }
}
