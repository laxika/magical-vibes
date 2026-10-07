package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.cards.r.Repeal;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AwakenedSkyclave.class, GrizzlyBears.class, InvasionOfZendikar.class,
        LeoninScimitar.class, NicolBolasPlaneswalker.class, Repeal.class, SteamcoreWeird.class})
class SteamcoreWeirdTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a player when red mana was spent to cast it")
    void dealsDamageWhenRedManaWasSpent() {
        castSteamcoreWeird(ManaColor.RED, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals 2 damage to a creature when red mana was spent to cast it")
    void dealsDamageToCreatureWhenRedManaWasSpent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castSteamcoreWeird(ManaColor.RED, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 2 damage to a planeswalker when red mana was spent to cast it")
    void dealsDamageToPlaneswalkerWhenRedManaWasSpent() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);

        castSteamcoreWeird(ManaColor.RED, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Deals 2 damage to a battle when red mana was spent to cast it")
    void dealsDamageToBattleWhenRedManaWasSpent() {
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 3);

        castSteamcoreWeird(ManaColor.RED, battle.getId());

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Invasion of Zendikar");
    }

    @Test
    @DisplayName("Does not deal damage when red mana was not spent to cast it")
    void doesNotDealDamageWithoutRedMana() {
        harness.castFromHand(player1, new SteamcoreWeird(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player1, "Steamcore Weird");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast does not trigger damage")
    void doesNotTriggerWhenPutOntoBattlefield() {
        harness.addMana(player1, ManaColor.RED, 1);

        harness.enterBattlefieldAndReturn(player1, new SteamcoreWeird());

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The damage trigger still resolves after Steamcore Weird returns to hand")
    void dealsDamageAfterSourceLeavesBattlefield() {
        harness.setHand(player1, List.of(new SteamcoreWeird()));
        addManaToCast(ManaColor.RED);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Repeal()));
        harness.setLibrary(player1, List.of(new SteamcoreWeird()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0, 4, harness.getPermanentId(player1, "Steamcore Weird"));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Steamcore Weird");
        harness.assertNotOnBattlefield(player1, "Steamcore Weird");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The damage trigger does not follow its target into hand")
    void doesNotDealDamageToTargetThatLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SteamcoreWeird());
        harness.setHand(player1, List.of(new SteamcoreWeird()));
        addManaToCast(ManaColor.RED);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Repeal()));
        harness.setLibrary(player1, List.of(new SteamcoreWeird()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0, 4, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Steamcore Weird");
        harness.assertNotInGraveyard(player2, "Steamcore Weird");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature, nonplaneswalker permanent")
    void cannotTargetInvalidPermanent() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new SteamcoreWeird()));
        addManaToCast(ManaColor.RED);

        assertThatThrownBy(() -> harness.castCreature(
                player1, 0, harness.getPermanentId(player2, "Leonin Scimitar")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature, planeswalker, battle, or player");
    }

    private void castSteamcoreWeird(ManaColor extraManaColor, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new SteamcoreWeird()));
        addManaToCast(extraManaColor);
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addManaToCast(ManaColor extraManaColor) {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, extraManaColor, 1);
    }
}
