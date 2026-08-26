package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GuildpactParagon;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.InvasionOfRavnica;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CosmicHunger.class, GarrukWildspeaker.class, GrizzlyBears.class, HillGiant.class,
        GuildpactParagon.class, InvasionOfRavnica.class})
class CosmicHungerTest extends BaseCardTest {

    @Test
    void dealsSourcePowerDamageToCreatureWithoutTakingDamageBack() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CosmicHunger()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, List.of(source.getId(), harness.getPermanentId(player2, "Grizzly Bears")));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    void dealsSourcePowerDamageToPlaneswalker() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new CosmicHunger()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, List.of(source.getId(), planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void dealsSourcePowerDamageToBattle() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfRavnica());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        harness.setHand(player1, List.of(new CosmicHunger()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, List.of(source.getId(), battle.getId()));
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(2);
    }

    @Test
    void cannotUseOpponentCreatureAsSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent victim = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CosmicHunger()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), victim.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetSameCreatureTwice() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new CosmicHunger()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), source.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetPlayer() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new CosmicHunger()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
