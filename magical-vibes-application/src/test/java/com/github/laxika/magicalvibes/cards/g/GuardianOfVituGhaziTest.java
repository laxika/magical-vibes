package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GuardianOfVituGhazi.class, GrizzlyBears.class})
class GuardianOfVituGhaziTest extends BaseCardTest {

    @Test
    @DisplayName("Convoke taps creatures to help pay Guardian of Vitu-Ghazi's cost")
    void castsWithConvoke() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GuardianOfVituGhazi()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof GuardianOfVituGhazi)
                .hasSize(1);
    }

    @Test
    @DisplayName("Vigilance keeps Guardian of Vitu-Ghazi untapped after attacking")
    void vigilanceDoesNotTapOnAttack() {
        Permanent guardian = addCreatureReady(player1, new GuardianOfVituGhazi());

        declareAttackers(List.of(0));

        assertThat(guardian.isTapped()).isFalse();
    }
}
