package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OptimisticScavenger;
import com.github.laxika.magicalvibes.cards.s.Shock;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheWanderingRescuer.class, GrizzlyBears.class, Shock.class, OptimisticScavenger.class})
class TheWanderingRescuerTest extends BaseCardTest {

    @Test
    @DisplayName("Grants hexproof to other tapped creatures you control")
    void grantsHexproofToOtherTappedCreaturesYouControl() {
        Permanent rescuer = addCreatureReady(player1, new TheWanderingRescuer());
        Permanent tappedCreature = addCreatureReady(player1, new GrizzlyBears());
        tappedCreature.tap();
        Permanent untappedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.tap();
        rescuer.tap();

        assertThat(gqs.hasKeyword(gd, tappedCreature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, untappedCreature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, rescuer, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Hexproof follows a creature's tap state")
    void hexproofFollowsTapState() {
        harness.addToBattlefield(player1, new TheWanderingRescuer());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();

        creature.tap();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();

        creature.untap();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Tapped creatures with granted hexproof cannot be targeted by opponents")
    void tappedCreatureCannotBeTargetedByOpponent() {
        harness.addToBattlefield(player1, new TheWanderingRescuer());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    void canFlashInDuringOpponentsUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new TheWanderingRescuer(), "{3}{W}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "The Wandering Rescuer");
    }

    @Test
    void convokeTapsSummoningSickCreaturesAndProtectsThemOnlyAfterResolution() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new OptimisticScavenger());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new OptimisticScavenger());
        harness.setHand(player1, List.of(new TheWanderingRescuer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(first.getId(), second.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, first, Keyword.HEXPROOF)).isFalse();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "The Wandering Rescuer");
        assertThat(gqs.hasKeyword(gd, first, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void cannotConvokeMoreCreaturesThanTotalCost() {
        List<java.util.UUID> creatures = java.util.stream.IntStream.range(0, 6)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new OptimisticScavenger()).getId())
                .toList();
        harness.setHand(player1, List.of(new TheWanderingRescuer()));

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0, List.of(), creatures))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total cost");
        harness.assertNotOnBattlefield(player1, "The Wandering Rescuer");
    }

    @Test
    void dealsDamageInBothCombatDamageSteps() {
        addCreatureReady(player1, new TheWanderingRescuer());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 14);
    }

    @Test
    void controllerCanTargetOwnTappedCreature() {
        harness.addToBattlefield(player1, new TheWanderingRescuer());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.tap();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void rescuerLeavingRemovesHexproofFromTappedCreatures() {
        Permanent rescuer = addCreatureReady(player1, new TheWanderingRescuer());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.tap();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, rescuer.getId());
        harness.castAndResolveInstant(player2, 0, rescuer.getId());

        harness.assertInGraveyard(player1, "The Wandering Rescuer");
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void gainingHexproofBeforeResolutionMakesOpponentsTargetIllegal() {
        harness.addToBattlefield(player1, new TheWanderingRescuer());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, creature.getId());

        creature.tap();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }
}
