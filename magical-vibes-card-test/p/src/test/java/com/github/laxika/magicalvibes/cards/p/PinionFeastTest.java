package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvenSunstriker;
import com.github.laxika.magicalvibes.cards.d.DragonScarredBear;
import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PinionFeast.class, AvenSunstriker.class, DragonScarredBear.class, ColossodonYearling.class})
class PinionFeastTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature with flying and bolsters the least-tough creature")
    void destroysFlyingCreatureAndBolsters() {
        Permanent target = addCreatureReady(player2, new AvenSunstriker());
        Permanent leastToughCreature = addCreatureReady(player1, new DragonScarredBear());
        Permanent largerCreature = addCreatureReady(player1, new ColossodonYearling());

        cast(target.getId());

        harness.assertNotOnBattlefield(player2, "Aven Sunstriker");
        harness.assertInGraveyard(player2, "Aven Sunstriker");
        assertThat(leastToughCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(largerCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetCreatureWithoutFlying() {
        harness.addToBattlefield(player2, new AvenSunstriker());
        Permanent nonFlyingCreature = addCreatureReady(player2, new DragonScarredBear());

        harness.setHand(player1, List.of(new PinionFeast()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, nonFlyingCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with flying");
    }

    @Test
    void choosesOneOfTheCreaturesTiedForLeastToughness() {
        Permanent target = addCreatureReady(player2, new AvenSunstriker());
        Permanent first = addCreatureReady(player1, new DragonScarredBear());
        Permanent second = addCreatureReady(player1, new DragonScarredBear());
        Permanent larger = addCreatureReady(player1, new ColossodonYearling());

        cast(target.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(larger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Pinion Feast");
    }

    @Test
    void usesCurrentToughnessIncludingCounters() {
        Permanent target = addCreatureReady(player2, new AvenSunstriker());
        Permanent bear = addCreatureReady(player1, new DragonScarredBear());
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent yearling = addCreatureReady(player1, new ColossodonYearling());

        cast(target.getId());

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(yearling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void destroysOwnFlyingCreatureBeforeChoosingBolsterRecipient() {
        Permanent target = addCreatureReady(player1, new AvenSunstriker());
        Permanent survivor = addCreatureReady(player1, new DragonScarredBear());

        cast(target.getId());

        harness.assertInGraveyard(player1, "Aven Sunstriker");
        harness.assertNotOnBattlefield(player1, "Aven Sunstriker");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void destroysTargetWithoutBolsteringOpponentsCreaturesWhenControllerHasNone() {
        Permanent target = addCreatureReady(player2, new AvenSunstriker());
        Permanent opponentCreature = addCreatureReady(player2, new DragonScarredBear());

        cast(target.getId());

        harness.assertInGraveyard(player2, "Aven Sunstriker");
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Pinion Feast");
    }

    @Test
    void doesNotBolsterWhenTargetLeavesBeforeResolution() {
        Permanent target = addCreatureReady(player2, new AvenSunstriker());
        Permanent ownCreature = addCreatureReady(player1, new DragonScarredBear());
        harness.setHand(player1, List.of(new PinionFeast()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerHands.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Pinion Feast");
    }

    private void cast(UUID targetId) {
        harness.setHand(player1, List.of(new PinionFeast()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
