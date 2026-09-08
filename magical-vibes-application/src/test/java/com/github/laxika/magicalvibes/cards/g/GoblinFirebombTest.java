package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinFirebombTest extends BaseCardTest {

    @Test
    void sacrificesItselfOnActivationAndDestroysTargetCreatureOnResolution() {
        Permanent firebomb = harness.addToBattlefieldAndReturn(player1, new GoblinFirebomb());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(firebomb);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firebomb.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
    }

    @Test
    void canDestroyALand() {
        Permanent firebomb = harness.addToBattlefieldAndReturn(player1, new GoblinFirebomb());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firebomb.getCard());
    }
}
