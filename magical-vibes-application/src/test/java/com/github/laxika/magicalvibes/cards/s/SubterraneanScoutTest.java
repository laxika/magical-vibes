package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cobblebrute;
import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.t.TimberpackWolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SubterraneanScout.class, TimberpackWolf.class, Cobblebrute.class, Disperse.class})
class SubterraneanScoutTest extends BaseCardTest {

    private void castScout() {
        harness.castFromHand(player1, new SubterraneanScout(), "{1}{R}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB makes a target creature with power 2 or less unblockable this turn")
    void etbMakesLowPowerCreatureUnblockable() {
        Permanent wolf = addCreatureReady(player1, new TimberpackWolf());
        castScout();
        harness.handlePermanentChosen(player1, wolf.getId());
        harness.passBothPriorities();

        assertThat(wolf.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("A creature with power greater than 2 is not a legal target")
    void cannotTargetHighPowerCreature() {
        Permanent brute = addCreatureReady(player1, new Cobblebrute());
        castScout();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, brute.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Subterranean Scout"));
        harness.passBothPriorities();

        assertThat(brute.isCantBeBlocked()).isFalse();
    }

    @Test
    void canTargetItselfWithNoOtherCreatures() {
        castScout();
        Permanent scout = findPermanent(player1, "Subterranean Scout");
        harness.handlePermanentChosen(player1, scout.getId());
        harness.passBothPriorities();

        assertThat(scout.isCantBeBlocked()).isTrue();
    }

    @Test
    void canTargetOpponentCreature() {
        Permanent wolf = addCreatureReady(player2, new TimberpackWolf());
        castScout();
        harness.handlePermanentChosen(player1, wolf.getId());
        harness.passBothPriorities();

        assertThat(wolf.isCantBeBlocked()).isTrue();
    }

    @Test
    void usesEffectivePowerWhenChoosingTarget() {
        Permanent wolf = addCreatureReady(player1, new TimberpackWolf());
        addCreatureReady(player1, new TimberpackWolf());
        castScout();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, wolf.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Subterranean Scout"));
        harness.passBothPriorities();

        assertThat(wolf.isCantBeBlocked()).isFalse();
    }

    @Test
    void targetBecomingTooPowerfulBeforeResolutionIsIllegal() {
        Permanent wolf = addCreatureReady(player1, new TimberpackWolf());
        castScout();
        harness.handlePermanentChosen(player1, wolf.getId());
        harness.addToBattlefield(player1, new TimberpackWolf());
        harness.passBothPriorities();

        assertThat(wolf.isCantBeBlocked()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void powerIncreaseAfterResolutionDoesNotRestoreBlockability() {
        Permanent wolf = addCreatureReady(player1, new TimberpackWolf());
        Permanent blocker = addCreatureReady(player2, new Cobblebrute());
        castScout();
        harness.handlePermanentChosen(player1, wolf.getId());
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new TimberpackWolf());

        assertThat(wolf.isCantBeBlocked()).isTrue();
        assertThat(bls.canBlockAttacker(gd, blocker, wolf,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    void abilityResolvesAfterScoutLeavesBattlefield() {
        Permanent wolf = addCreatureReady(player1, new TimberpackWolf());
        castScout();
        harness.handlePermanentChosen(player1, wolf.getId());
        Permanent scout = findPermanent(player1, "Subterranean Scout");
        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, scout.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Subterranean Scout");
        harness.passBothPriorities();

        assertThat(wolf.isCantBeBlocked()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
    @Test
    void unblockabilityExpiresAfterTheTurn() {
        Permanent wolf = addCreatureReady(player1, new TimberpackWolf());
        Permanent blocker = addCreatureReady(player2, new Cobblebrute());
        castScout();
        harness.handlePermanentChosen(player1, wolf.getId());
        harness.passBothPriorities();
        assertThat(bls.canBlockAttacker(gd, blocker, wolf,
                gd.playerBattlefields.get(player2.getId()))).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(wolf.isCantBeBlocked()).isFalse();
        assertThat(bls.canBlockAttacker(gd, blocker, wolf,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }
}
