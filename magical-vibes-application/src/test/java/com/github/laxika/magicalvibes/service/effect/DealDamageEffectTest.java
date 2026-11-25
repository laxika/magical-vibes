package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.v.VampireNighthawk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestStackDoor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** The damage a triggered ability has the permanent its trigger event was about deal (CR 608.2h). */
@CardUsed({GrizzlyBears.class, HillGiant.class, VampireNighthawk.class})
class DealDamageEffectTest extends BaseCardTest {

    @BeforeEach
    void setUpTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
    }

    /** Resolves {@code effects} as player 1's triggered ability whose trigger event was about {@code dealer}. */
    private StackEntry resolveDealtBy(Permanent dealer, UUID targetId, CardEffect... effects) {
        StackEntry entry = TestStackDoor.triggered(new GrizzlyBears(), player1.getId(), "Dealt by its triggering permanent",
                List.of(effects), targetId, (UUID) null);
        entry.setTriggeringPermanentId(dealer.getId());
        entry.setTriggeringPermanentControllerId(player1.getId());
        gd.stack.add(entry);
        harness.passBothPriorities();
        return entry;
    }

    @Test
    @DisplayName("Excess damage counts the dealing permanent's deathtouch: 3 damage to a 3/3 is 2 excess")
    void excessDamageCountsTheDealersDeathtouch() {
        Permanent nighthawk = harness.addToBattlefieldAndReturn(player1, new VampireNighthawk());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        resolveDealtBy(nighthawk, giant.getId(),
                DealDamageEffect.toAnyTarget(new Fixed(3)).byTriggeringPermanent(),
                new ConditionalEffect(new EventValueAtLeast(2), new GainLifeEffect(5)));

        assertThat(giant.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.getLife(player1.getId())).isEqualTo(28);
    }

    @Test
    @DisplayName("Any-target damage dealt by the triggering permanent is the resolution's damage step")
    void anyTargetDamageByTheTriggeringPermanentIsTheDamageStep() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        resolveDealtBy(bears, player2.getId(),
                DealDamageEffect.toAnyTarget(new Fixed(2)).byTriggeringPermanent(),
                new GainLifeEffect(new AmountThisWay(StepKind.DEAL_DAMAGE)));

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Player-or-planeswalker damage dealt by the triggering creature is the resolution's damage step")
    void playerOrPlaneswalkerDamageByTheTriggeringCreatureIsTheDamageStep() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        resolveDealtBy(bears, player2.getId(),
                DealDamageEffect.toTargetPlayerOrPlaneswalker(new Fixed(2)).byTriggeringPermanent(),
                new GainLifeEffect(new AmountThisWay(StepKind.DEAL_DAMAGE)));

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }
}
