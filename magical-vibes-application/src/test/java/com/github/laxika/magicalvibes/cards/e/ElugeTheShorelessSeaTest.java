package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElugeTheShorelessSea.class, Island.class, Mountain.class, Divination.class})
class ElugeTheShorelessSeaTest extends BaseCardTest {

    @Test
    void powerAndToughnessEqualControlledIslands() {
        harness.addToBattlefield(player1, new Island());
        Permanent eluge = addCreatureReady(player1, new ElugeTheShorelessSea());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());

        assertThat(gqs.getEffectivePower(gd, eluge)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, eluge)).isEqualTo(2);
    }

    @Test
    void etbFloodsLandAndIslandGrantSurvivesElugeLeavingUntilCounterIsRemoved() {
        harness.addToBattlefield(player1, new Island());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new ElugeTheShorelessSea()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0, 0, mountain.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent eluge = findPermanent(player1, "Eluge, the Shoreless Sea");
        assertThat(mountain.getCounterCount(CounterType.FLOOD)).isEqualTo(1);
        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).contains(CardSubtype.ISLAND);

        gd.playerBattlefields.get(player1.getId()).remove(eluge);
        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).contains(CardSubtype.ISLAND);

        mountain.setCounterCount(CounterType.FLOOD, 0);
        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).doesNotContain(CardSubtype.ISLAND);
    }

    @Test
    void attacksFloodTheOnlyTargetLand() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        addCreatureReady(player1, new ElugeTheShorelessSea());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(island.getCounterCount(CounterType.FLOOD)).isEqualTo(1);
        assertThat(gqs.effectiveBasicLandTypes(gd, island)).contains(CardSubtype.ISLAND);
    }

    @Test
    void FirstInstantOrSorceryGetsOneReductionPerFloodedLand() {
        harness.addToBattlefield(player1, new Island());
        addCreatureReady(player1, new ElugeTheShorelessSea());
        Permanent floodedLand = harness.addToBattlefieldAndReturn(player1, new Mountain());
        floodedLand.setCounterCount(CounterType.FLOOD, 1);
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof Divination);

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
