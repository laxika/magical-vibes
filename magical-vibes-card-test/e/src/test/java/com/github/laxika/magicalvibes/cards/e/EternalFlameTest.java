package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EternalFlame.class, Mountain.class, ChandraNalaar.class})
class EternalFlameTest extends BaseCardTest {

    @Test
    @DisplayName("Deals Mountain-count damage to an opponent and half rounded up to its controller")
    void dealsDamageAndSelfDamage() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new EternalFlame()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Counts only Mountains controlled by the spell's controller")
    void countsOnlyControllersMountains() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new EternalFlame()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Can target a planeswalker and still damage its controller")
    void dealsDamageToPlaneswalkerAndSelf() {
        var planeswalker = harness.addToBattlefieldAndReturn(player1, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 10);
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new EternalFlame()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Counts Mountains as the spell resolves")
    void countsMountainsAtResolution() {
        var mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new EternalFlame()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(mountain);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Does not deal controller damage when a planeswalker target leaves before resolution")
    void fizzlesWithoutSelfDamageWhenPlaneswalkerTargetLeaves() {
        var planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 10);
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new EternalFlame()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, planeswalker.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Cannot target the controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new EternalFlame()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deals no damage with no Mountains")
    void zeroMountainsDealsNoDamage() {
        harness.setHand(player1, List.of(new EternalFlame()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Eternal Flame");
    }

    @Test
    @DisplayName("An even Mountain count deals exactly half that much damage to you")
    void evenMountainCountHalvesExactly() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new EternalFlame()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Lethal damage to both players results in a draw")
    void lethalDamageToBothPlayersDraws() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.setLife(player1, 2);
        harness.setLife(player2, 3);
        harness.setHand(player1, List.of(new EternalFlame()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player1, 0);
        harness.assertLife(player2, 0);
        assertThat(gd.gameResult).isEqualTo(GameEventFact.GameResult.DRAW);
        assertThat(gd.winnerPlayerId).isNull();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        var mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new EternalFlame()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
