package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Permanent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ParhelionII.class, GrizzlyBears.class})
class ParhelionIITest extends BaseCardTest {

    @Test
    void attackingCreatesTwoUntappedAttackingAngels() {
        addCreatureReady(player1, new ParhelionII());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        List<Permanent> angels = findPermanents(player1, "Angel");
        assertThat(angels).hasSize(2)
                .allSatisfy(angel -> {
                    assertThat(angel.isAttacking()).isTrue();
                    assertThat(angel.isTapped()).isFalse();
                    assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
                    assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
                });
    }

    @Test
    void crewFourAnimatesParhelionAndTapsCreaturesWithTotalPowerFour() {
        Permanent parhelion = addCreatureReady(player1, new ParhelionII());
        Permanent firstCrew = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCrew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, parhelion)).isTrue();
        assertThat(firstCrew.isTapped()).isTrue();
        assertThat(secondCrew.isTapped()).isTrue();
    }
}
