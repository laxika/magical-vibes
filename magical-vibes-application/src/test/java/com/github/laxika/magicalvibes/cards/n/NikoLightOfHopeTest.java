package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NikoLightOfHope.class, GrizzlyBears.class})
class NikoLightOfHopeTest extends BaseCardTest {

    @Test
    void enteringCreatesTwoShards() {
        addReadyNiko();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
    }

    @Test
    void abilityCopiesShardsUntilNextEndStepAndReturnsTheCreature() {
        Permanent creature = addReadyCreature(player1);
        Permanent niko = addReadyNiko();
        List<Permanent> shards = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();

        int nikoIndex = gd.playerBattlefields.get(player1.getId()).indexOf(niko);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, nikoIndex, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(creature.getId()));
        assertThat(shards).allSatisfy(shard -> {
            assertThat(gqs.isCreature(gd, shard)).isTrue();
            assertThat(gqs.getEffectivePower(gd, shard)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, shard)).isEqualTo(2);
        });

        harness.passUntil(player1, TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(shards).allSatisfy(shard -> assertThat(gqs.isCreature(gd, shard)).isFalse());
    }

    private Permanent addReadyNiko() {
        Permanent niko = harness.enterBattlefieldAndReturn(player1, new NikoLightOfHope());
        harness.passBothPriorities();
        niko.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return niko;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }
}
