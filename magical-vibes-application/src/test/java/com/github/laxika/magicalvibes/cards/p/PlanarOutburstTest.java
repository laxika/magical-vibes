package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlanarOutburst.class, Forest.class, GrizzlyBears.class})
class PlanarOutburstTest extends BaseCardTest {

    @Test
    void destroysAllNonlandCreaturesAndLeavesLandsUntouched() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.setHand(player1, List.of(new PlanarOutburst()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
    }

    @Test
    void alternateCastAwakensTargetLand() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.setHand(player1, List.of(new PlanarOutburst()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of(land.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(4);
        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.ELEMENTAL)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(land.getCard().hasType(CardType.LAND)).isTrue();
    }

    @Test
    void alternateCastRequiresAwakenTarget() {
        harness.setHand(player1, List.of(new PlanarOutburst()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> gs.playCardWithAlternateCost(
                gd, player1, 0, 0, null, null, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("additional targets");
    }

    @Test
    void alternateCastCannotTargetOpponentsLand() {
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new PlanarOutburst()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> gs.playCardWithAlternateCost(
                gd, player1, 0, 0, null, null, List.of(opponentLand.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
