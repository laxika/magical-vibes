package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LuxuriousLocomotive.class, GrizzlyBears.class})
class LuxuriousLocomotiveTest extends BaseCardTest {

    @Test
    void createsATreasureForEachCreatureThatCrewedIt() {
        addReady(new LuxuriousLocomotive());
        addReady(new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void countsACrewerThatLeftTheBattlefield() {
        addReady(new LuxuriousLocomotive());
        Permanent crewer = addReady(new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, crewer));
        harness.runStateBasedActions();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void crewCanBeActivatedOnlyOnceEachTurn() {
        addReady(new LuxuriousLocomotive());
        addReady(new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("once each turn");
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
