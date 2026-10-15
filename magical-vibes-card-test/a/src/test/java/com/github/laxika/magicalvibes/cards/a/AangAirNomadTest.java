package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AangAirNomad.class, GrizzlyBears.class})
class AangAirNomadTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control have vigilance")
    void grantsVigilanceToOtherControlledCreatures() {
        harness.addToBattlefield(player1, new AangAirNomad());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The static ability also applies to creatures that enter later")
    void grantsVigilanceToCreaturesEnteringLater() {
        harness.addToBattlefield(player1, new AangAirNomad());

        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Aang and creatures granted vigilance do not tap to attack")
    void attackersWithVigilanceRemainUntapped() {
        Permanent aang = addCreatureReady(player1, new AangAirNomad());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));

        assertThat(aang.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Creatures lose granted vigilance as soon as Aang leaves")
    void vigilanceEndsWhenAangLeaves() {
        Permanent aang = harness.addToBattlefieldAndReturn(player1, new AangAirNomad());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, aang));

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Ground creatures cannot block Aang, but flying creatures can")
    void flyingRestrictsBlockers() {
        Permanent aang = addCreatureReady(player1, new AangAirNomad());
        Permanent groundBlocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent flyingBlocker = harness.addToBattlefieldAndReturn(player2, new AangAirNomad());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(bls.canBlockAttacker(gd, groundBlocker, aang,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, flyingBlocker, aang,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }
}
