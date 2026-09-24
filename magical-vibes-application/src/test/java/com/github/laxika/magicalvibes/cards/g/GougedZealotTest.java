package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GougedZealot.class, GrizzlyBears.class, Forest.class, Shock.class, Pacifism.class})
class GougedZealotTest extends BaseCardTest {

    @Test
    @DisplayName("With delirium, attacking deals 1 damage to each defending creature")
    void attacksDamageDefendingCreaturesWithDelirium() {
        addCreatureReady(player1, new GougedZealot());
        Permanent defendingBears = addCreatureReady(player2, new GrizzlyBears());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        setDelirium();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(defendingBears.getMarkedDamage()).isEqualTo(1);
        assertThat(ownBears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Without delirium, attacking does not damage defending creatures")
    void attacksDoNotDamageDefendingCreaturesWithoutDelirium() {
        addCreatureReady(player1, new GougedZealot());
        Permanent defendingBears = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(defendingBears.getMarkedDamage()).isZero();
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Pacifism()));
    }
}
