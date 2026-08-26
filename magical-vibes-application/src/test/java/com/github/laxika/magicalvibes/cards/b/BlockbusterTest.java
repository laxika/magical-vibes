package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Blockbuster.class, GrizzlyBears.class})
class BlockbusterTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Blockbuster deals 3 damage to each tapped creature and each player")
    void damagesTappedCreaturesAndPlayers() {
        Permanent blockbuster = harness.addToBattlefieldAndReturn(player1, new Blockbuster());
        Permanent tappedOwnCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent untappedOwnCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent tappedOpposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent untappedOpposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        tappedOwnCreature.tap();
        tappedOpposingCreature.tap();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(untappedOwnCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(untappedOpposingCreature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(blockbuster, tappedOwnCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(tappedOpposingCreature);
        harness.assertInGraveyard(player1, "Blockbuster");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
