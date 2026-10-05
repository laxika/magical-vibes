package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KindlesparkDuo.class, GrizzlyBears.class, Shock.class})
class KindlesparkDuoTest extends BaseCardTest {

    @Test
    void dealsDamageToTargetOpponent() {
        Permanent duo = addReadyDuo();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(duo.isTapped()).isTrue();
    }

    @Test
    void untapsWhenYouCastANoncreatureSpell() {
        Permanent duo = addReadyDuo();
        duo.tap();
        prepareMainPhase();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(duo.isTapped()).isFalse();
    }

    @Test
    void doesNotUntapWhenYouCastACreatureSpell() {
        Permanent duo = addReadyDuo();
        duo.tap();
        prepareMainPhase();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(duo.isTapped()).isTrue();
    }

    @Test
    void cannotTargetItsController() {
        Permanent duo = addReadyDuo();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(duo.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetACreature() {
        Permanent duo = addReadyDuo();
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(duo.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent duo = addReadyDuo();
        duo.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void untappingDoesNotRemoveSummoningSickness() {
        Permanent duo = addReadyDuo();
        duo.setSummoningSick(true);
        duo.tap();
        prepareMainPhase();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(duo.isTapped()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotUntapWhenOpponentCastsANoncreatureSpell() {
        Permanent duo = addReadyDuo();
        duo.tap();
        prepareMainPhase();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(duo.isTapped()).isTrue();
        harness.assertLife(player1, 18);
    }

    @Test
    void untapTriggerResolvesBeforeSpellAndAllowsAnotherActivation() {
        Permanent duo = addReadyDuo();
        prepareMainPhase();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 19);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(duo.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(duo.isTapped()).isFalse();
        harness.assertLife(player2, 19);
        assertThat(gd.stack).hasSize(1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(duo.isTapped()).isTrue();
    }

    private Permanent addReadyDuo() {
        return addCreatureReady(player1, new KindlesparkDuo());
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
