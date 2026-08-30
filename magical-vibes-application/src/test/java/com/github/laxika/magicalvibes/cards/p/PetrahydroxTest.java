package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Petrahydrox.class, ProdigalPyromancer.class, Shock.class})
class PetrahydroxTest extends BaseCardTest {

    @Test
    @DisplayName("Returns itself to its owner's hand when targeted by a spell")
    void returnsToHandWhenTargetedBySpell() {
        Permanent petrahydrox = harness.addToBattlefieldAndReturn(player1, new Petrahydrox());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, petrahydrox.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Petrahydrox");
        harness.assertInHand(player1, "Petrahydrox");
    }

    @Test
    @DisplayName("Returns itself to its owner's hand when targeted by an ability")
    void returnsToHandWhenTargetedByAbility() {
        Permanent petrahydrox = harness.addToBattlefieldAndReturn(player1, new Petrahydrox());
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);

        harness.activateAbility(player2, 0, null, petrahydrox.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(petrahydrox.getId()));
        harness.assertInHand(player1, "Petrahydrox");
    }
}
