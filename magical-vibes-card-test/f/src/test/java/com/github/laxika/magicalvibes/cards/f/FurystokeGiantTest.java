package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JuvenileGloomwidow;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FurystokeGiant.class, GrizzlyBears.class, JuvenileGloomwidow.class})
class FurystokeGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control gain a tap-to-deal-2-damage ability on ETB")
    void otherCreaturesGainDamageAbility() {
        harness.setLife(player2, 20);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears()); // index 0

        castFurystokeGiant();

        // Bears (index 0) can now tap to deal 2 damage to any target.
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Furystoke Giant itself does not gain the granted ability")
    void giantDoesNotGainAbility() {
        addCreatureReady(player1, new GrizzlyBears()); // index 0

        castFurystokeGiant(); // giant enters at index 1

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Granted ability wears off at end of turn")
    void abilityWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new GrizzlyBears()); // index 0

        castFurystokeGiant();

        // End player1's turn — until-end-of-turn abilities are cleared during cleanup.
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to cleanup (resets "until end of turn" modifiers)

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Persist returns the Giant with a -1/-1 counter and its ETB grants the ability again")
    void persistReturnsGiantAndTriggersEtbAgain() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GrizzlyBears()); // index 0
        addCreatureReady(player1, new GrizzlyBears()); // index 1
        addCreatureReady(player1, new GrizzlyBears()); // index 2

        castFurystokeGiant();
        Permanent giant = findPermanent(player1, "Furystoke Giant");
        Permanent lateBear = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, giant.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, giant.getId());
        resolveAllTriggers();

        Permanent returnedGiant = findPermanent(player1, "Furystoke Giant");
        assertThat(returnedGiant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, returnedGiant)).isEqualTo(2);

        assertThat(gd.playerBattlefields.get(player1.getId()).get(3)).isSameAs(lateBear);
        harness.activateAbility(player1, 3, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        harness.activateAbility(player1, 2, null, returnedGiant.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Furystoke Giant")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(returnedGiant.getCard());
    }

    @Test
    @DisplayName("Opposing creatures do not gain the ability")
    void opponentsDoNotGainAbility() {
        addCreatureReady(player2, new GrizzlyBears());
        castFurystokeGiant();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Creatures entering after the trigger resolves do not gain the ability")
    void laterCreaturesDoNotGainAbility() {
        castFurystokeGiant();
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Creatures present when the trigger resolves gain the ability")
    void recipientsAreDeterminedAtResolution() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new FurystokeGiant(), "{3}{R}{R}");
        harness.passBothPriorities();
        addCreatureReady(player1, new GrizzlyBears());
        resolveAllTriggers();

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Summoning sickness prevents use of the granted tap ability")
    void summoningSicknessPreventsActivation() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castFurystokeGiant();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The creature using the granted ability is the damage source")
    void grantedAbilityUsesRecipientsWither() {
        addCreatureReady(player1, new JuvenileGloomwidow());
        Permanent target = addCreatureReady(player2, new JuvenileGloomwidow());
        castFurystokeGiant();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    private void castFurystokeGiant() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new FurystokeGiant(), "{3}{R}{R}");
        resolveAllTriggers();
    }
}
