package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CanyonMinotaur;
import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.r.RingOfEvosIsle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Switcheroo.class, GrizzlyBears.class, HillGiant.class, CanyonMinotaur.class,
        ElvishVisionary.class, Murder.class, RingOfEvosIsle.class})
class SwitcherooTest extends BaseCardTest {

    private void prepare() {
        harness.setHand(player1, List.of(new Switcheroo()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Exchanges control of the two target creatures")
    void exchangesControl() {
        prepare();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponents = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.castAndResolveSorcery(player1, 0, List.of(own.getId(), opponents.getId()));

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Exchanges control when the opponent's creature is the first target")
    void exchangesControlWithOpponentCreatureFirst() {
        prepare();
        Permanent opponents = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castAndResolveSorcery(player1, 0, List.of(opponents.getId(), own.getId()));

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does nothing when both target creatures have the same controller (CR 701.12b)")
    void doesNothingWhenSameController() {
        prepare();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Exchange does nothing when a target creature leaves the battlefield before resolution")
    void fizzlesWhenTargetGone() {
        prepare();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponents = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.castSorcery(player1, 0, List.of(own.getId(), opponents.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(opponents);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Exchange does nothing when the first target is destroyed in response")
    void doesNothingWhenFirstTargetDestroyed() {
        prepare();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new ElvishVisionary());
        Permanent opponents = harness.addToBattlefieldAndReturn(player2, new CanyonMinotaur());
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, List.of(own.getId(), opponents.getId()));
        harness.castAndResolveInstant(player2, 0, own.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Elvish Visionary");
        harness.assertOnBattlefield(player2, "Canyon Minotaur");
        harness.assertNotOnBattlefield(player1, "Canyon Minotaur");
        harness.assertInGraveyard(player1, "Switcheroo");
    }

    @Test
    @DisplayName("Exchange does nothing when the first target gains hexproof in response")
    void doesNothingWhenFirstTargetGainsHexproof() {
        assertExchangePreventedByHexproof(true);
    }

    @Test
    @DisplayName("Exchange does nothing when the second target gains hexproof in response")
    void doesNothingWhenSecondTargetGainsHexproof() {
        assertExchangePreventedByHexproof(false);
    }

    private void assertExchangePreventedByHexproof(boolean opponentFirst) {
        prepare();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new ElvishVisionary());
        Permanent opponents = harness.addToBattlefieldAndReturn(player2, new CanyonMinotaur());
        Permanent ring = harness.addToBattlefieldAndReturn(player2, new RingOfEvosIsle());
        ring.setAttachedTo(opponents.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, opponentFirst
                ? List.of(opponents.getId(), own.getId()) : List.of(own.getId(), opponents.getId()));
        harness.activateAbility(player2, 1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Elvish Visionary");
        harness.assertOnBattlefield(player2, "Canyon Minotaur");
        harness.assertNotOnBattlefield(player2, "Elvish Visionary");
        harness.assertNotOnBattlefield(player1, "Canyon Minotaur");
        harness.assertInGraveyard(player1, "Switcheroo");
    }

    @Test
    @DisplayName("Control changes preserve tapped state and do not trigger enters abilities")
    void preservesStateWithoutEnteringAgain() {
        prepare();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new CanyonMinotaur());
        Permanent opponents = harness.addToBattlefieldAndReturn(player2, new ElvishVisionary());
        own.tap();
        opponents.tap();
        own.setSummoningSick(false);
        opponents.setSummoningSick(false);

        harness.castAndResolveSorcery(player1, 0, List.of(own.getId(), opponents.getId()));

        harness.assertOnBattlefield(player2, "Canyon Minotaur");
        harness.assertOnBattlefield(player1, "Elvish Visionary");
        assertThat(own.isTapped()).isTrue();
        assertThat(opponents.isTapped()).isTrue();
        assertThat(own.isSummoningSick()).isTrue();
        assertThat(opponents.isSummoningSick()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot choose the same creature for both targets")
    void rejectsDuplicateTargets() {
        prepare();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CanyonMinotaur());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Switcheroo");
        assertThat(gd.stack).isEmpty();
    }
}
