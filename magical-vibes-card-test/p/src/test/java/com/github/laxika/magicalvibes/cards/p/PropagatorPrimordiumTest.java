package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FeralThallid;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PropagatorPrimordium.class, FeralThallid.class, GrizzlyBears.class})
@DisplayName("Propagator Primordium")
class PropagatorPrimordiumTest extends BaseCardTest {

    @Test
    @DisplayName("Conjures two copies of itself into its controller's graveyard")
    void conjuresTwoCopiesIntoGraveyard() {
        Permanent propagator = harness.enterBattlefieldAndReturn(player1, new PropagatorPrimordium());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(2)
                .allSatisfy(card -> {
                    assertThat(card.isToken()).isFalse();
                    assertThat(card.getId()).isNotEqualTo(propagator.getCard().getId());
                });
    }

    @Test
    @DisplayName("Adds a spore counter during its controller's upkeep")
    void addsSporeCounterDuringUpkeep() {
        Permanent propagator = addPropagator();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(propagator.getCounterCount(CounterType.FUNGUS)).isOne();
    }

    @Test
    @DisplayName("Removes three spore counters to return a Fungus creature")
    void returnsTargetFungusCreature() {
        Permanent propagator = addPropagator();
        propagator.setCounterCount(CounterType.FUNGUS, 3);
        Card target = new FeralThallid();
        harness.setGraveyard(player1, List.of(target));

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(propagator.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Cannot target a non-Fungus creature card")
    void rejectsNonFungusCreatureTarget() {
        Permanent propagator = addPropagator();
        propagator.setCounterCount(CounterType.FUNGUS, 3);
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not gain a spore counter during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent propagator = addPropagator();

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(propagator.getCounterCount(CounterType.FUNGUS)).isZero();
    }

    @Test
    @DisplayName("Cannot return a Fungus from an opponent's graveyard")
    void rejectsOpponentsGraveyard() {
        Permanent propagator = addPropagator();
        propagator.setCounterCount(CounterType.FUNGUS, 3);
        Card target = new PropagatorPrimordium();
        harness.setGraveyard(player2, List.of(target));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(propagator.getCounterCount(CounterType.FUNGUS)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target);
    }

    @Test
    @DisplayName("Cannot activate with fewer than three spore counters")
    void rejectsInsufficientCounters() {
        Permanent propagator = addPropagator();
        propagator.setCounterCount(CounterType.FUNGUS, 2);
        Card target = new PropagatorPrimordium();
        harness.setGraveyard(player1, List.of(target));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(propagator.getCounterCount(CounterType.FUNGUS)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
    }

    @Test
    @DisplayName("Returning a conjured copy triggers its own conjure ability")
    void returnedConjuredCopyConjuresTwoMoreCards() {
        Permanent propagator = harness.enterBattlefieldAndReturn(player1, new PropagatorPrimordium());
        resolveAllTriggers();
        Card target = gd.playerGraveyards.get(player1.getId()).getFirst();
        propagator.setCounterCount(CounterType.FUNGUS, 4);
        propagator.tap();

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);

        assertThat(propagator.getCounterCount(CounterType.FUNGUS)).isOne();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(3)
                .doesNotContain(target)
                .allSatisfy(card -> {
                    assertThat(card.isToken()).isFalse();
                    assertThat(card.getOwnerId()).isEqualTo(player1.getId());
                });
    }

    @Test
    @DisplayName("Counter costs stay paid when the target leaves the graveyard")
    void targetLeavingGraveyardDoesNotRefundCounters() {
        Permanent propagator = addPropagator();
        propagator.setCounterCount(CounterType.FUNGUS, 3);
        Card target = new PropagatorPrimordium();
        harness.setGraveyard(player1, List.of(target));

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player1, List.of());
        resolveAllTriggers();

        assertThat(propagator.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(propagator);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private Permanent addPropagator() {
        return addCreatureReady(player1, new PropagatorPrimordium());
    }
}
