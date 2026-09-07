package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaraudingDreadship.class, GrizzlyBears.class})
class MaraudingDreadshipTest extends BaseCardTest {

    @Test
    void entersWithAnIncubatorTokenWithTwoCounters() {
        harness.setHand(player1, List.of(new MaraudingDreadship()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void crewTwoAnimatesTheDreadshipUntilEndOfTurn() {
        Permanent dreadship = addDreadshipReady(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dreadship.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(gqs.isCreature(gd, dreadship)).isTrue();
        assertThat(gqs.getEffectivePower(gd, dreadship)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, dreadship)).isEqualTo(1);
        assertThat(creature.isTapped()).isTrue();
        assertThat(dreadship.isTapped()).isFalse();
    }

    private Permanent addDreadshipReady(Player player) {
        Permanent permanent = new Permanent(new MaraudingDreadship());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
