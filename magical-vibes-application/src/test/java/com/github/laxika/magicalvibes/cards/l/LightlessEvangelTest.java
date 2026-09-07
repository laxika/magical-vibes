package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LightlessEvangel.class, GrizzlyBears.class, DarksteelRelic.class, Forest.class})
class LightlessEvangelTest extends BaseCardTest {

    @Test
    void growsWhenYouSacrificeAnotherCreature() {
        Permanent evangel = harness.addToBattlefieldAndReturn(player1, new LightlessEvangel());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        sacrifice(player1, creature);
        resolveAllTriggers();

        assertThat(evangel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void growsWhenYouSacrificeAnotherArtifact() {
        Permanent evangel = harness.addToBattlefieldAndReturn(player1, new LightlessEvangel());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());

        sacrifice(player1, artifact);
        resolveAllTriggers();

        assertThat(evangel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotGrowWhenYouSacrificeANoncreatureNonartifact() {
        Permanent evangel = harness.addToBattlefieldAndReturn(player1, new LightlessEvangel());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        sacrifice(player1, land);
        resolveAllTriggers();

        assertThat(evangel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotGrowWhenOpponentSacrificesACreatureOrArtifact() {
        Permanent evangel = harness.addToBattlefieldAndReturn(player1, new LightlessEvangel());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        sacrifice(player2, creature);
        resolveAllTriggers();

        assertThat(evangel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void sacrifice(Player player, Permanent permanent) {
        Card card = permanent.getCard();
        gd.playerBattlefields.get(player.getId()).remove(permanent);
        gd.playerGraveyards.get(player.getId()).add(card);
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkAllyPermanentSacrificedTriggers(gd, player.getId(), card));
    }
}
