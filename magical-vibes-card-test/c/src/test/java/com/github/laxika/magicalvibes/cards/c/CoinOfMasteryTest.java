package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.ManaVault;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoinOfMastery.class, ManaVault.class, GrizzlyBears.class})
class CoinOfMasteryTest extends BaseCardTest {

    @Test
    void artifactManaSpentOnCreatureAddsThatManyCounters() {
        harness.addToBattlefield(player1, new CoinOfMastery());
        Permanent vault = addReadyVault(player1);
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vault));

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void tappingCreatesTreasureToken() {
        Permanent coin = harness.addToBattlefieldAndReturn(player1, new CoinOfMastery());
        int coinIndex = gd.playerBattlefields.get(player1.getId()).indexOf(coin);

        harness.activateAbility(player1, coinIndex, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void creatureCastWithoutArtifactManaGetsNoCounters() {
        harness.addToBattlefield(player1, new CoinOfMastery());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void multipleCoinsEachAddCountersForTheSameArtifactMana() {
        harness.addToBattlefield(player1, new CoinOfMastery());
        harness.addToBattlefield(player1, new CoinOfMastery());
        Permanent vault = addReadyVault(player1);
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vault));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void opponentsCoinDoesNotAddCountersToYourCreature() {
        harness.addToBattlefield(player2, new CoinOfMastery());
        Permanent vault = addReadyVault(player1);
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vault));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void creatureEnteringWithoutBeingCastGetsNoCounters() {
        harness.addToBattlefield(player1, new CoinOfMastery());
        Permanent vault = addReadyVault(player1);
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vault));

        Permanent bears = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void noncreatureCastWithArtifactManaGetsNoCounters() {
        harness.addToBattlefield(player1, new CoinOfMastery());
        Permanent vault = addReadyVault(player1);
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vault));
        harness.setHand(player1, List.of(new ManaVault()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mana Vault")).hasSize(2)
                .allSatisfy(permanent -> assertThat(permanent
                        .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    void treasureManaAddsCountersAndTreasureIsSacrificed() {
        harness.addToBattlefield(player1, new CoinOfMastery());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent treasure = findPermanent(player1, "Treasure");

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(treasure), null, null);
        harness.handleListChoice(player1, "GREEN");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void treasureAndNonTreasureArtifactManaBothCount() {
        harness.addToBattlefield(player1, new CoinOfMastery());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent treasure = findPermanent(player1, "Treasure");
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(treasure), null, null);
        harness.handleListChoice(player1, "GREEN");
        Permanent vault = addReadyVault(player1);
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vault));
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    void treasureCreationUsesTheStackAndCannotBeActivatedAgainWhileTapped() {
        Permanent coin = harness.addToBattlefieldAndReturn(player1, new CoinOfMastery());

        harness.activateAbility(player1, 0, null, null);

        assertThat(coin.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    private Permanent addReadyVault(Player player) {
        Permanent vault = harness.addToBattlefieldAndReturn(player, new ManaVault());
        vault.setSummoningSick(false);
        return vault;
    }
}
