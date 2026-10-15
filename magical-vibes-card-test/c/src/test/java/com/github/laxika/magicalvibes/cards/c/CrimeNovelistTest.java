package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DemandAnswers;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.cards.m.MagnifyingGlass;
import com.github.laxika.magicalvibes.cards.n.NervousGardener;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrimeNovelist.class, DemandAnswers.class, MagnifyingGlass.class, NervousGardener.class})
class CrimeNovelistTest extends BaseCardTest {

    @Test
    void sacrificingAnArtifactAddsACounterAndRedMana() {
        Permanent novelist = harness.addToBattlefieldAndReturn(player1, new CrimeNovelist());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MagnifyingGlass());

        sacrifice(artifact);
        resolveAllTriggers();

        assertThat(novelist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void sacrificingANonArtifactDoesNotTrigger() {
        Permanent novelist = harness.addToBattlefieldAndReturn(player1, new CrimeNovelist());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NervousGardener());

        sacrifice(creature);
        resolveAllTriggers();

        assertThat(novelist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void opponentsArtifactSacrificeDoesNotTrigger() {
        Permanent novelist = harness.addToBattlefieldAndReturn(player1, new CrimeNovelist());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MagnifyingGlass());

        sacrifice(player2, artifact);
        resolveAllTriggers();

        assertThat(novelist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void eachArtifactSacrificeTriggersSeparately() {
        Permanent novelist = harness.addToBattlefieldAndReturn(player1, new CrimeNovelist());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MagnifyingGlass());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MagnifyingGlass());

        sacrifice(first);
        sacrifice(second);

        assertThat(novelist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        resolveAllTriggers();

        assertThat(novelist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    void destroyingAnArtifactDoesNotTrigger() {
        Permanent novelist = harness.addToBattlefieldAndReturn(player1, new CrimeNovelist());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MagnifyingGlass());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, artifact));
        resolveAllTriggers();

        assertThat(novelist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void stillAddsManaWhenNovelistLeavesBeforeResolution() {
        Permanent novelist = harness.addToBattlefieldAndReturn(player1, new CrimeNovelist());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MagnifyingGlass());

        sacrifice(artifact);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, novelist));
        resolveAllTriggers();

        assertThat(novelist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @CardUsed({LiquimetalCoating.class})
    void sacrificingCreatureMadeIntoArtifactTriggers() {
        Permanent novelist = harness.addToBattlefieldAndReturn(player1, new CrimeNovelist());
        Permanent coating = harness.addToBattlefieldAndReturn(player1, new LiquimetalCoating());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NervousGardener());
        coating.setSummoningSick(false);
        harness.activateAbility(player1, 1, null, creature.getId());
        resolveAllTriggers();
        assertThat(gqs.isArtifact(gd, creature)).isTrue();
        harness.setHand(player1, List.of(new DemandAnswers()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, null, creature.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Nervous Gardener");
        assertThat(novelist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    private void sacrifice(Permanent permanent) {
        sacrifice(player1, permanent);
    }

    private void sacrifice(Player player, Permanent permanent) {
        harness.inMutationScope(() -> {
            assertThat(harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, permanent)).isTrue();
            harness.getTriggerCollectionService()
                    .checkAllyPermanentSacrificedTriggers(gd, player.getId(), permanent.getCard());
        });
    }
}
