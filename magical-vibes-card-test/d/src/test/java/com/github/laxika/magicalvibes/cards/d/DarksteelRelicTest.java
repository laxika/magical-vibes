package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BeastWithin;
import com.github.laxika.magicalvibes.cards.k.KarnLiberated;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarksteelRelic.class, BeastWithin.class, KarnLiberated.class})
class DarksteelRelicTest extends BaseCardTest {

    @Test
    void survivesDestructionAndItsControllerStillCreatesBeast() {
        Permanent relic = harness.addToBattlefieldAndReturn(player2, new DarksteelRelic());
        harness.setHand(player1, List.of(new BeastWithin()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, relic.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(relic).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken());
        harness.assertNotInGraveyard(player2, "Darksteel Relic");
        harness.assertInGraveyard(player1, "Beast Within");
    }

    @Test
    void indestructibleDoesNotPreventExile() {
        Permanent relic = harness.addToBattlefieldAndReturn(player2, new DarksteelRelic());
        Permanent karn = harness.addToBattlefieldAndReturn(player1, new KarnLiberated());
        karn.setCounterCount(CounterType.LOYALTY, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, relic.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(relic);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(relic.getCard());
        harness.assertNotInGraveyard(player2, "Darksteel Relic");
    }
}
