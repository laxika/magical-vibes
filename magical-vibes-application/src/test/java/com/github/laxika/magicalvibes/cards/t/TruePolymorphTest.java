package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TruePolymorph.class, DarksteelRelic.class, GrizzlyBears.class, Island.class,
        ProdigalPyromancer.class})
class TruePolymorphTest extends BaseCardTest {

    @Test
    @DisplayName("Makes the first creature gain the second creature's activated ability")
    void copiesFirstCreatureFromSecondCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent copySource = addCreatureReady(player2, new ProdigalPyromancer());

        castTruePolymorph(target, copySource);
        activateCopiedPyromancer(target);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Can make an artifact gain a creature's activated ability")
    void copiesFirstArtifactFromSecondCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        target.setSummoningSick(false);
        Permanent copySource = addCreatureReady(player2, new ProdigalPyromancer());

        castTruePolymorph(target, copySource);
        activateCopiedPyromancer(target);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Requires two distinct artifact or creature targets")
    void rejectsInvalidTargets() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new TruePolymorph()));
        addCastMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(land.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does nothing if either target is gone when it resolves")
    void doesNothingIfTargetLeavesBeforeResolution() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent copySource = addCreatureReady(player2, new ProdigalPyromancer());
        harness.setHand(player1, List.of(new TruePolymorph()));
        addCastMana();
        harness.castInstant(player1, 0, List.of(target.getId(), copySource.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(copySource);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(target), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castTruePolymorph(Permanent target, Permanent copySource) {
        harness.setHand(player1, List.of(new TruePolymorph()));
        addCastMana();
        harness.castAndResolveInstant(player1, 0, List.of(target.getId(), copySource.getId()));
    }

    private void addCastMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void activateCopiedPyromancer(Permanent target) {
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(target), null, player2.getId());
        harness.passBothPriorities();
    }
}
