package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VertexPaladin;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrizzlyBears.class, InvasionOfXerex.class, VertexPaladin.class})
class InvasionOfXerexTest extends BaseCardTest {

    @Test
    void returnsOptionalTargetCreatureToItsOwnersHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castInvasion(target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerHands.get(player2.getId())).contains(target.getCard());
    }

    @Test
    void canEnterWithoutChoosingTheOptionalTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castInvasion(null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof InvasionOfXerex);
    }

    @Test
    void defeatingTheSiegeCastsVertexPaladinWithCreatureCountPowerAndToughness() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfXerex());
        battle.setCounterCount(CounterType.DEFENSE, 0);

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent paladin = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof VertexPaladin)
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, paladin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, paladin)).isEqualTo(3);

        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, paladin)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, paladin)).isEqualTo(4);
    }

    private void castInvasion(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new InvasionOfXerex()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        gs.playCard(gd, player1, 0, 0, targetId, null);
    }
}
