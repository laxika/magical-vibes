package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Delete.class, AlphaMyr.class, GrizzlyBears.class})
class DeleteTest extends BaseCardTest {

    @Test
    void dealsXDamageToEachNonartifactCreatureAndPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Delete()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void doesNotDamageArtifactCreatures() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Delete()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(artifactCreature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Alpha Myr");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void zeroXDoesNothing() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Delete()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(harness.getGameData().getLife(player1.getId())).isEqualTo(20);
        assertThat(harness.getGameData().getLife(player2.getId())).isEqualTo(20);
    }
}
