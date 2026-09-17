package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AjaniSteadfast;
import com.github.laxika.magicalvibes.cards.d.DelugeOfTheDead;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfInnistrad;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FinalAct.class, AjaniSteadfast.class, InvasionOfInnistrad.class, DelugeOfTheDead.class,
        GrizzlyBears.class, Forest.class, Shock.class, LlanowarElves.class})
class FinalActTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all selected permanent types")
    void destroysAllSelectedPermanentTypes() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new AjaniSteadfast());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfInnistrad());
        battle.setCounterCount(CounterType.DEFENSE, 4);
        harness.addToBattlefield(player1, new Forest());

        cast(new int[]{0, 1, 2});

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Forest");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiles all graveyards")
    void exilesAllGraveyards() {
        Card ownCard = new Shock();
        Card opponentCard = new LlanowarElves();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));

        cast(new int[]{3});

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Final Act");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ownCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opponentCard);
    }

    @Test
    @DisplayName("Removes all tracked counters from each opponent")
    void removesAllTrackedCountersFromEachOpponent() {
        gd.playerPoisonCounters.put(player1.getId(), 2);
        gd.playerEnergyCounters.put(player1.getId(), 3);
        gd.playerExperienceCounters.put(player1.getId(), 4);
        gd.playerPoisonCounters.put(player2.getId(), 5);
        gd.playerEnergyCounters.put(player2.getId(), 6);
        gd.playerExperienceCounters.put(player2.getId(), 7);

        cast(new int[]{4});

        assertThat(gd.playerPoisonCounters).containsEntry(player1.getId(), 2)
                .doesNotContainKey(player2.getId());
        assertThat(gd.playerEnergyCounters).containsEntry(player1.getId(), 3)
                .doesNotContainKey(player2.getId());
        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 4)
                .doesNotContainKey(player2.getId());
    }

    @Test
    @DisplayName("Can resolve every mode together")
    void canResolveEveryModeTogether() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new AjaniSteadfast());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfInnistrad());
        battle.setCounterCount(CounterType.DEFENSE, 4);
        harness.setGraveyard(player2, List.of(new LlanowarElves()));
        gd.playerPoisonCounters.put(player2.getId(), 2);

        cast(new int[]{0, 1, 2, 3, 4});

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerPoisonCounters).doesNotContainKey(player2.getId());
    }

    private void cast(int[] modes) {
        harness.setHand(player1, List.of(new FinalAct()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castModalSorceryWithModes(player1, 0, 1, 5, modes, List.of(), null);
        harness.passBothPriorities();
    }
}
