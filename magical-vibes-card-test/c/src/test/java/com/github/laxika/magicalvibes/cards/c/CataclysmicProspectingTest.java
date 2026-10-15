package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DesertOfTheFervent;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CataclysmicProspecting.class, DesertOfTheFervent.class, GrizzlyBears.class})
class CataclysmicProspectingTest extends BaseCardTest {

    @Test
    void dealsXDamageToEachCreatureAndCreatesTreasureForDesertManaSpent() {
        harness.addToBattlefield(player1, new DesertOfTheFervent());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 3);
        harness.setHand(player1, List.of(new CataclysmicProspecting()));

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        List<Permanent> treasures = findPermanents(player1, "Treasure");
        assertThat(treasures).hasSize(1);
        assertThat(treasures.getFirst().isTapped()).isTrue();
    }

    @Test
    void createsNoTreasureWithoutDesertMana() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 4);
        harness.setHand(player1, List.of(new CataclysmicProspecting()));

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void zeroXStillCreatesTreasureForEachDesertManaSpent() {
        harness.addToBattlefield(player1, new DesertOfTheFervent());
        harness.addToBattlefield(player1, new DesertOfTheFervent());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.setHand(player1, List.of(new CataclysmicProspecting()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanent(player1, "Grizzly Bears").getMarkedDamage()).isZero();
        assertThat(findPermanent(player2, "Grizzly Bears").getMarkedDamage()).isZero();
        assertThat(findPermanents(player1, "Treasure")).hasSize(2)
                .allSatisfy(treasure -> assertThat(treasure.isTapped()).isTrue());
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void countsOnlyDesertManaSpentAndNotManaLeftInPool() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new DesertOfTheFervent());
            harness.activateAbility(player1, i, 0, null, null);
        }
        harness.setHand(player1, List.of(new CataclysmicProspecting()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2)
                .allSatisfy(treasure -> assertThat(treasure.isTapped()).isTrue());
    }
}
