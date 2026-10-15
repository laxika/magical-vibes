package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BannerhideKrushok;
import com.github.laxika.magicalvibes.cards.d.DeepwoodDenizen;
import com.github.laxika.magicalvibes.cards.r.RiftSower;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScurryOak.class, DeepwoodDenizen.class, BannerhideKrushok.class, RiftSower.class})
class ScurryOakTest extends BaseCardTest {

    @Test
    void evolvesAndMayCreateSquirrel() {
        Permanent oak = harness.addToBattlefieldAndReturn(player1, new ScurryOak());
        castCreature(new DeepwoodDenizen());

        assertThat(oak.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Squirrel")).isEqualTo(1);
    }

    @Test
    void decliningDoesNotCreateSquirrel() {
        Permanent oak = harness.addToBattlefieldAndReturn(player1, new ScurryOak());
        castCreature(new DeepwoodDenizen());

        assertThat(oak.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Squirrel")).isZero();
    }

    @Test
    void doesNotEvolveForCreatureThatIsNotBigger() {
        Permanent oak = harness.addToBattlefieldAndReturn(player1, new ScurryOak());
        castCreature(new ScurryOak());

        assertThat(oak.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castCreature(Card creature) {
        harness.castFromHand(player1, creature, "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    void evolvesWhenOnlyEnteringCreaturesToughnessIsGreater() {
        Permanent oak = harness.addToBattlefieldAndReturn(player1, new ScurryOak());
        castCreature(new RiftSower());

        assertThat(oak.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Squirrel")).isEqualTo(1);
        assertThat(oak.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void multipleCountersFromOpponentsReinforceCreateOnlyOneSquirrelForOakController() {
        Permanent oak = harness.addToBattlefieldAndReturn(player1, new ScurryOak());
        harness.setHand(player2, List.of(new BannerhideKrushok()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.activateHandAbility(player2, 0, oak.getId());
        harness.passBothPriorities();

        assertThat(oak.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Squirrel")).isEqualTo(1);
        assertThat(countPermanents(player2, "Squirrel")).isZero();
        assertThat(oak.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsCreatureDoesNotTriggerEvolve() {
        Permanent oak = harness.addToBattlefieldAndReturn(player1, new ScurryOak());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new DeepwoodDenizen(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(oak.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void evolveRechecksSizeAfterOakReceivesOtherCounters() {
        Permanent oak = harness.addToBattlefieldAndReturn(player1, new ScurryOak());
        harness.castFromHand(player1, new DeepwoodDenizen(), "{2}{G}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new BannerhideKrushok()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateHandAbility(player1, 0, oak.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(oak.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(countPermanents(player1, "Squirrel")).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
