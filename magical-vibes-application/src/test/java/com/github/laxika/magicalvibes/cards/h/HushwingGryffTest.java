package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IchorWellspring;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.cards.p.PriestOfUrabrask;
import com.github.laxika.magicalvibes.cards.s.SuturePriest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HushwingGryff.class, GrizzlyBears.class, PriestOfUrabrask.class,
        SuturePriest.class, IchorWellspring.class, MarchOfTheMachines.class})
class HushwingGryffTest extends BaseCardTest {

    @Test
    void suppressesEnteringCreaturesOwnEtbTrigger() {
        harness.addToBattlefield(player1, new HushwingGryff());
        harness.castFromHand(player1, new PriestOfUrabrask(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void suppressesOtherCreaturesEnteringTriggers() {
        harness.addToBattlefield(player1, new HushwingGryff());
        harness.addToBattlefield(player1, new SuturePriest());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    void suppressesOpponentCreaturesOwnEtbTrigger() {
        harness.addToBattlefield(player2, new HushwingGryff());
        harness.castFromHand(player1, new PriestOfUrabrask(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.assertOnBattlefield(player1, "Priest of Urabrask");
    }

    @Test
    void suppressesTriggersCausedByItsOwnEntry() {
        harness.addToBattlefield(player1, new SuturePriest());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new HushwingGryff(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Hushwing Gryff");
    }

    @Test
    void doesNotSuppressNoncreatureArtifactEntry() {
        harness.addToBattlefield(player1, new HushwingGryff());
        harness.setLibrary(player1, List.of(new HushwingGryff()));
        harness.castFromHand(player1, new IchorWellspring(), "{2}");
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInHand(player1, "Hushwing Gryff");
    }

    @Test
    void suppressesEntryOfArtifactAnimatedByContinuousEffect() {
        harness.addToBattlefield(player1, new HushwingGryff());
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        harness.setLibrary(player1, List.of(new HushwingGryff()));
        harness.castFromHand(player1, new IchorWellspring(), "{2}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ichor Wellspring");
        assertThat(gqs.isCreature(gd, findPermanent(player1, "Ichor Wellspring"))).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.assertNotInHand(player1, "Hushwing Gryff");
    }
}
