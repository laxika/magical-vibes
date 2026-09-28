package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeginAnew.class, GrizzlyBears.class})
class BeginAnewTest extends BaseCardTest {

    @Test
    void destroysAllCreaturesAndPerpetuallyBoostsAllCreatureCardsInHand() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        GrizzlyBears firstBear = new GrizzlyBears();
        GrizzlyBears secondBear = new GrizzlyBears();
        harness.setHand(player1, List.of(new BeginAnew(), firstBear, secondBear));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        List<Permanent> creatures = gd.playerBattlefields.get(player1.getId());
        assertThat(creatures).hasSize(2);
        assertThat(creatures).allSatisfy(permanent -> {
            assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(3);
        });
    }
}
