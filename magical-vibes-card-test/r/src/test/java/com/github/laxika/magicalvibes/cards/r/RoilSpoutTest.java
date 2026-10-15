package com.github.laxika.magicalvibes.cards.r;

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

@CardUsed({RoilSpout.class, Forest.class, GrizzlyBears.class})
class RoilSpoutTest extends BaseCardTest {

    @Test
    void putsTargetCreatureOnTopOfItsOwnersLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Forest forest = new Forest();
        harness.setLibrary(player2, List.of(forest));
        harness.setHand(player1, List.of(new RoilSpout()));
        addNormalMana();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard(), forest);
        harness.assertInGraveyard(player1, "Roil Spout");
    }

    @Test
    void cannotTargetALand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new RoilSpout()));
        addNormalMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void alternateCastAwakensTargetLandAndPutsCreatureOnTop() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Forest forest = new Forest();
        harness.setLibrary(player2, List.of(forest));
        harness.setHand(player1, List.of(new RoilSpout()));
        addAwakenMana();

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(creature.getId(), land.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(creature.getCard(), forest);
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(4);
        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.ELEMENTAL)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(land.getCard().hasType(CardType.LAND)).isTrue();
    }

    @Test
    void alternateCastRequiresAwakenTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RoilSpout()));
        addAwakenMana();

        assertThatThrownBy(() -> gs.playCardWithAlternateCost(
                gd, player1, 0, 0, null, null, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("additional targets");
    }

    @Test
    void normalCastDoesNotAwakenALand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new RoilSpout()));
        addNormalMana();

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.isCreature(gd, land)).isFalse();
    }

    private void addNormalMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void addAwakenMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
