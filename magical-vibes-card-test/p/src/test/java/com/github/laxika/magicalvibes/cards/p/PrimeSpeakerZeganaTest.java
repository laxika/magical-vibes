package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.b.BurstOfStrength;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.r.RapidHybridization;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrimeSpeakerZegana.class, HillGiant.class, AvatarOfMight.class,
        BurstOfStrength.class, RapidHybridization.class})
class PrimeSpeakerZeganaTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with no counters and draws one card when it is the only creature")
    void entersAloneDrawsOne() {
        castZegana();

        Permanent zegana = findPermanent(player1, "Prime Speaker Zegana");
        assertThat(zegana.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(drawnCards(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enters with counters equal to the greatest power among other creatures you control")
    void entersWithCountersFromOtherCreatures() {
        addCreatureReady(player1, new HillGiant());

        castZegana();

        Permanent zegana = findPermanent(player1, "Prime Speaker Zegana");
        assertThat(zegana.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(drawnCards(player1)).isEqualTo(4);
    }

    @Test
    @DisplayName("Ignores creatures controlled by the opponent")
    void ignoresOpponentCreatures() {
        addCreatureReady(player2, new AvatarOfMight());

        castZegana();

        Permanent zegana = findPermanent(player1, "Prime Speaker Zegana");
        assertThat(zegana.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(drawnCards(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Uses the greatest effective power rather than summing creature powers")
    void usesGreatestEffectivePower() {
        addCreatureReady(player1, new AvatarOfMight());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        giant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 6);

        castZegana();

        assertThat(findPermanent(player1, "Prime Speaker Zegana")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(9);
        assertThat(drawnCards(player1)).isEqualTo(10);
    }

    @Test
    @DisplayName("Draw count includes a counter added in response to the draw trigger")
    void drawsUsingPowerAtResolution() {
        castZeganaLeavingDrawTrigger();
        Permanent zegana = findPermanent(player1, "Prime Speaker Zegana");
        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, zegana.getId());
        resolveAllTriggers();

        assertThat(zegana.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(drawnCards(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("Uses last-known power when Zegana is destroyed in response to its draw trigger")
    void drawsUsingLastKnownPowerAfterRemoval() {
        addCreatureReady(player1, new HillGiant());
        castZeganaLeavingDrawTrigger();
        Permanent zegana = findPermanent(player1, "Prime Speaker Zegana");
        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, zegana.getId());
        harness.setHand(player1, List.of(new RapidHybridization()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, zegana.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Prime Speaker Zegana")).isZero();
        assertThat(drawnCards(player1)).isEqualTo(5);
    }

    private void castZegana() {
        castZeganaLeavingDrawTrigger();
        resolveAllTriggers();
    }

    private void castZeganaLeavingDrawTrigger() {
        harness.setHand(player1, List.of(new PrimeSpeakerZegana()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private int drawnCards(Player player) {
        return gd.playerHands.get(player.getId()).size();
    }
}
