package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AncestorDragon.class, GrizzlyBears.class})
class AncestorDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life for each creature attacking")
    void gainsLifeForEachAttackingCreature() {
        harness.addToBattlefield(player1, new AncestorDragon());

        Permanent bear1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear1.setSummoningSick(false);
        Permanent bear2 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear2.setSummoningSick(false);

        harness.setLife(player1, 20);
        declareAttackers(List.of(1, 2));

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Does not trigger when no creatures attack")
    void doesNotTriggerWithoutAttackers() {
        harness.addToBattlefield(player1, new AncestorDragon());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setLife(player1, 20);
        declareAttackers(List.of());

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

}
