package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FertileGround;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Overgrowth;
import com.github.laxika.magicalvibes.cards.u.UrbanBurgeoning;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StreetSweeper.class, Forest.class, Overgrowth.class, FertileGround.class, GrizzlyBears.class, UrbanBurgeoning.class})
class StreetSweeperTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking destroys every Aura attached to the chosen land")
    void destroysAurasAttachedToTargetLand() {
        addCreatureReady(player1, new StreetSweeper());
        Permanent targetLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent otherLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        Permanent overgrowth = new Permanent(new Overgrowth());
        overgrowth.setAttachedTo(targetLand.getId());
        gd.playerBattlefields.get(player1.getId()).add(overgrowth);

        Permanent fertileGround = new Permanent(new FertileGround());
        fertileGround.setAttachedTo(targetLand.getId());
        gd.playerBattlefields.get(player2.getId()).add(fertileGround);

        Permanent unrelatedAura = new Permanent(new Overgrowth());
        unrelatedAura.setAttachedTo(otherLand.getId());
        gd.playerBattlefields.get(player1.getId()).add(unrelatedAura);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, targetLand.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(targetLand, otherLand)
                .doesNotContain(fertileGround);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(unrelatedAura)
                .doesNotContain(overgrowth);
        harness.assertInGraveyard(player1, "Overgrowth");
        harness.assertInGraveyard(player2, "Fertile Ground");
    }

    @Test
    @DisplayName("The attack trigger only accepts a land target")
    void onlyTargetsLands() {
        addCreatureReady(player1, new StreetSweeper());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The trigger can target your own land and destroys Auras attached after targeting")
    void destroysAuraAttachedAfterTargetingOwnLand() {
        addCreatureReady(player1, new StreetSweeper());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, land.getId());

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new UrbanBurgeoning());
        aura.setAttachedTo(land.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Urban Burgeoning");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(aura);
    }

    @Test
    @DisplayName("The attack trigger still destroys Auras after Street Sweeper leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent sweeper = addCreatureReady(player1, new StreetSweeper());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new UrbanBurgeoning());
        aura.setAttachedTo(land.getId());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, land.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, sweeper));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Street Sweeper");
        harness.assertInGraveyard(player2, "Urban Burgeoning");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land).doesNotContain(aura);
    }

    @Test
    @DisplayName("An Aura moved to another land before resolution survives")
    void doesNotDestroyAuraMovedAwayBeforeResolution() {
        addCreatureReady(player1, new StreetSweeper());
        Permanent targetLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent otherLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new UrbanBurgeoning());
        aura.setAttachedTo(targetLand.getId());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, targetLand.getId());
        aura.setAttachedTo(otherLand.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(targetLand, otherLand, aura);
        harness.assertNotInGraveyard(player2, "Urban Burgeoning");
    }
}
