package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindFlayer.class, GrizzlyBears.class, Forest.class, Unsummon.class})
class MindFlayerTest extends BaseCardTest {

    @Test
    void entersAndGainsControlOfTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castMindFlayer(bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
    }

    @Test
    void returnsCreatureWhenMindFlayerLeavesTheBattlefield() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castMindFlayer(bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent mindFlayer = findPermanent(player1, "Mind Flayer");
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, mindFlayer.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> castMindFlayer(forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void doesNotGainControlWhenSourceLeavesBeforeTriggerResolves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castMindFlayer(bears.getId());
        harness.passBothPriorities();
        Permanent mindFlayer = findPermanent(player1, "Mind Flayer");

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, mindFlayer.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        harness.assertInHand(player1, "Mind Flayer");
    }

    @Test
    void doesNotGainControlWhenTargetLeavesBeforeTriggerResolves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castMindFlayer(bears.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Mind Flayer");
    }

    @Test
    void losingControlOfSourceEndsTheEffectPermanently() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castMindFlayer(bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent firstMindFlayer = findPermanent(player1, "Mind Flayer");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new MindFlayer()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castCreature(player2, 0, 0, firstMindFlayer.getId());
        harness.passBothPriorities();
        Permanent secondMindFlayer = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard() instanceof MindFlayer).findFirst().orElseThrow();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears, firstMindFlayer);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears, firstMindFlayer);

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, secondMindFlayer.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firstMindFlayer).doesNotContain(bears);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears).doesNotContain(firstMindFlayer);
    }

    private void castMindFlayer(UUID targetId) {
        harness.setHand(player1, List.of(new MindFlayer()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
