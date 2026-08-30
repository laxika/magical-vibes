package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AbandonedAirTemple.class, Forest.class, GrizzlyBears.class})
class AbandonedAirTempleTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you control no basic land")
    void entersTappedWithoutBasicLand() {
        playTemple(player1);

        assertThat(findTemple(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a basic land")
    void entersUntappedWithBasicLand() {
        harness.addToBattlefield(player1, new Forest());

        playTemple(player1);

        assertThat(findTemple(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("A nonbasic land does not satisfy the basic-land check")
    void nonbasicLandDoesNotSatisfyCheck() {
        harness.addToBattlefield(player1, new AbandonedAirTemple());

        playTemple(player1);

        assertThat(gd.playerBattlefields.get(player1.getId()).get(1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for white mana produces one white")
    void tappingProducesWhiteMana() {
        Permanent temple = addReadyTemple(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(temple.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The activated ability puts a +1/+1 counter on each creature you control")
    void putsCountersOnControlledCreatures() {
        Permanent temple = addReadyTemple(player1);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(temple.isTapped()).isTrue();
    }

    private void playTemple(Player player) {
        harness.setHand(player, List.of(new AbandonedAirTemple()));
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player, 0);
    }

    private Permanent addReadyTemple(Player player) {
        Permanent temple = harness.addToBattlefieldAndReturn(player, new AbandonedAirTemple());
        temple.setSummoningSick(false);
        return temple;
    }

    private Permanent findTemple(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof AbandonedAirTemple)
                .findFirst()
                .orElseThrow();
    }
}
