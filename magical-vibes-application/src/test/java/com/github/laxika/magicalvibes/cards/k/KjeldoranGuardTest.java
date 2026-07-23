package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KjeldoranGuardTest extends BaseCardTest {

    private Permanent addGuardReady() {
        Permanent guard = new Permanent(new KjeldoranGuard());
        guard.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(guard);
        return guard;
    }

    private void enterCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
    }

    private Permanent snowLandOnDefender() {
        Permanent snowLand = new Permanent(new Plains());
        TestCards.mutableCard(snowLand).setSupertypes(EnumSet.of(CardSupertype.BASIC, CardSupertype.SNOW));
        gd.playerBattlefields.get(player2.getId()).add(snowLand);
        return snowLand;
    }

    @Test
    @DisplayName("Cannot activate outside combat")
    void cannotActivateOutsideCombat() {
        Permanent guard = addGuardReady();
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(guard), 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when defending player controls a snow land")
    void cannotActivateWhenDefenderHasSnowLand() {
        Permanent guard = addGuardReady();
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        snowLandOnDefender();

        enterCombat();
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(guard), 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("snow lands");
    }

    @Test
    @DisplayName("Non-snow land does not block activation")
    void nonsnowLandDoesNotBlockActivation() {
        Permanent guard = addGuardReady();
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        gd.playerBattlefields.get(player2.getId()).add(new Permanent(new Plains()));

        int basePower = gqs.getEffectivePower(gd, bears);

        enterCombat();
        harness.activateAbility(player1, indexOf(guard), 0, null, bears.getId());
        harness.passBothPriorities();

        Permanent after = gqs.findPermanentById(gd, bears.getId());
        assertThat(gqs.getEffectivePower(gd, after)).isEqualTo(basePower + 1);
    }

    @Test
    @DisplayName("Gives target creature +1/+1 until end of turn during combat")
    void pumpsTargetDuringCombat() {
        Permanent guard = addGuardReady();
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        int basePower = gqs.getEffectivePower(gd, bears);
        int baseToughness = gqs.getEffectiveToughness(gd, bears);

        enterCombat();
        harness.activateAbility(player1, indexOf(guard), 0, null, bears.getId());
        harness.passBothPriorities();

        Permanent after = gqs.findPermanentById(gd, bears.getId());
        assertThat(gqs.getEffectivePower(gd, after)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, after)).isEqualTo(baseToughness + 1);
        assertThat(guard.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrifices itself when the pumped creature leaves the battlefield this turn")
    void sacrificesWhenTargetLeaves() {
        Permanent guard = addGuardReady();
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        enterCombat();
        harness.activateAbility(player1, indexOf(guard), 0, null, bears.getId());
        harness.passBothPriorities();

        harness.getPermanentRemovalService().removePermanentToHand(gd, bears);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kjeldoran Guard");
        harness.assertInGraveyard(player1, "Kjeldoran Guard");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not sacrifice when the pumped creature stays on the battlefield")
    void doesNotSacrificeIfTargetStays() {
        Permanent guard = addGuardReady();
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        enterCombat();
        harness.activateAbility(player1, indexOf(guard), 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kjeldoran Guard");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent guard = addGuardReady();
        Permanent mountain = new Permanent(new Mountain());
        gd.playerBattlefields.get(player1.getId()).add(mountain);

        enterCombat();
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(guard), 0, null, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private int indexOf(Permanent perm) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(perm);
    }
}
