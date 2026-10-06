package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.IzzetSignet;
import com.github.laxika.magicalvibes.cards.c.Cloudshift;
import com.github.laxika.magicalvibes.cards.r.Repeal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkySwallower.class, StreetbreakerWurm.class, IzzetSignet.class,
        SkarrgTheRagePits.class, Repeal.class, Cloudshift.class})
class SkySwallowerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives the target opponent control of all other permanents you control")
    void givesOpponentControlOfAllOtherPermanents() {
        Permanent wurmA = harness.addToBattlefieldAndReturn(player1, new StreetbreakerWurm());
        Permanent wurmB = harness.addToBattlefieldAndReturn(player1, new StreetbreakerWurm());
        Permanent signet = harness.addToBattlefieldAndReturn(player1, new IzzetSignet());
        Permanent opponentWurm = harness.addToBattlefieldAndReturn(player2, new StreetbreakerWurm());

        harness.setHand(player1, List.of(new SkySwallower()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(wurmA.getId()))
                .noneMatch(permanent -> permanent.getId().equals(wurmB.getId()))
                .noneMatch(permanent -> permanent.getId().equals(signet.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Sky Swallower"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(opponentWurm)
                .contains(wurmA, wurmB, signet);
    }

    @Test
    @DisplayName("ETB cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new SkySwallower()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    void transfersLandsAndPermanentsPresentAtResolution() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SkarrgTheRagePits());
        land.tap();
        harness.setHand(player1, List.of(new SkySwallower()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();

        Permanent signet = harness.addToBattlefieldAndReturn(player1, new IzzetSignet());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land, signet);
        assertThat(land.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Sky Swallower");
    }

    @Test
    void triggerResolvesAfterSourceReturnsToHand() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new StreetbreakerWurm());
        harness.setLibrary(player1, List.of(new IzzetSignet()));
        harness.setHand(player1, List.of(new SkySwallower()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Repeal()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castInstant(player1, 0, 5, harness.getPermanentId(player1, "Sky Swallower"));
        harness.passBothPriorities();
        harness.assertInHand(player1, "Sky Swallower");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(wurm);
        harness.assertNotOnBattlefield(player1, "Sky Swallower");
    }

    @Test
    void originalTriggerTransfersBlinkedSourceAsANewPermanent() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new StreetbreakerWurm());
        harness.setHand(player1, List.of(new SkySwallower()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        var originalId = harness.getPermanentId(player1, "Sky Swallower");

        harness.setHand(player1, List.of(new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, originalId);
        harness.handlePermanentChosen(player1, player2.getId());
        var returnedId = harness.getPermanentId(player1, "Sky Swallower");
        assertThat(returnedId).isNotEqualTo(originalId);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(wurm);
        harness.assertOnBattlefield(player1, "Sky Swallower");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sky Swallower");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(returnedId));
    }

    @Test
    void resolvesWithNoOtherPermanents() {
        harness.setHand(player1, List.of(new SkySwallower()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sky Swallower");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
