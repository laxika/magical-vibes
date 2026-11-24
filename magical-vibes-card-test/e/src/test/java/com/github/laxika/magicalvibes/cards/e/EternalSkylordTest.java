package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LazotepReaver;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EternalSkylord.class, GrizzlyBears.class, LazotepReaver.class})
class EternalSkylordTest extends BaseCardTest {

    @Test
    @DisplayName("ETB amasses Zombies 2 by creating a flying Zombie Army")
    void amassesWithoutAnArmy() {
        castEternalSkylord();

        Permanent army = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        assertThat(army.getCard().getName()).isEqualTo("Zombie Army");
        assertThat(army.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.ZOMBIE, CardSubtype.ARMY);
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, army)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, army)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, army, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("ETB amasses Zombies 2 on an existing Army without granting flying to nontokens")
    void amassesOnExistingArmy() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        PermanentGrants.subtype(gd, army, CardSubtype.ARMY);

        castEternalSkylord();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .isEmpty();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(PermanentGrants.subtypesOf(gd, army)).contains(CardSubtype.ZOMBIE);
        assertThat(gqs.hasKeyword(gd, army, Keyword.FLYING)).isFalse();
    }

    @Test
    void addsCountersToExistingTokenArmy() {
        harness.enterBattlefieldAndReturn(player1, new LazotepReaver());
        harness.passBothPriorities();
        Permanent army = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();

        castEternalSkylord();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).containsExactly(army);
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, army, Keyword.FLYING)).isTrue();
        Permanent skylord = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof EternalSkylord)
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(skylord);
        assertThat(gqs.hasKeyword(gd, army, Keyword.FLYING)).isFalse();
    }

    @Test
    void choosesOnlyOneOfMultipleArmies() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new LazotepReaver());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new LazotepReaver());
        PermanentGrants.subtype(gd, first, CardSubtype.ARMY);
        PermanentGrants.subtype(gd, second, CardSubtype.ARMY);

        castEternalSkylord();
        harness.handlePermanentChosen(player1, second.getId());

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Amass must choose one of several Armies: an empty answer is refused (CR 701.47a)")
    void cannotDeclineChoosingAnArmy() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new LazotepReaver());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new LazotepReaver());
        PermanentGrants.subtype(gd, first, CardSubtype.ARMY);
        PermanentGrants.subtype(gd, second, CardSubtype.ARMY);

        castEternalSkylord();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.handlePermanentChosen(player1, first.getId());

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(PermanentGrants.subtypesOf(gd, first)).contains(CardSubtype.ZOMBIE);
    }

    @Test
    void ignoresOpponentsArmyAndDoesNotGrantItFlying() {
        harness.enterBattlefieldAndReturn(player2, new LazotepReaver());
        harness.passBothPriorities();
        Permanent opposingArmy = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();

        castEternalSkylord();

        assertThat(opposingArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, opposingArmy, Keyword.FLYING)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    void doesNotGrantFlyingToNonZombieTokens() {
        GrizzlyBears token = new GrizzlyBears();
        token.setToken(true);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, token);

        castEternalSkylord();

        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isFalse();
    }

    @Test
    void createsArmyWhenExistingArmyLeavesBeforeTriggerResolves() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new LazotepReaver());
        PermanentGrants.subtype(gd, army, CardSubtype.ARMY);
        harness.enterBattlefieldAndReturn(player1, new EternalSkylord());
        gd.playerBattlefields.get(player1.getId()).remove(army);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(token -> {
                    assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
                    assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
                });
    }

    @Test
    void usesArmyThatAppearsBeforeTriggerResolves() {
        harness.enterBattlefieldAndReturn(player1, new EternalSkylord());
        Permanent army = harness.addToBattlefieldAndReturn(player1, new LazotepReaver());
        PermanentGrants.subtype(gd, army, CardSubtype.ARMY);

        harness.passBothPriorities();

        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).isEmpty();
    }

    @Test
    void tokenCopyGrantsItselfFlying() {
        EternalSkylord tokenCopy = new EternalSkylord();
        tokenCopy.setToken(true);
        Permanent skylord = harness.addToBattlefieldAndReturn(player1, tokenCopy);

        assertThat(gqs.hasKeyword(gd, skylord, Keyword.FLYING)).isTrue();
    }

    private void castEternalSkylord() {
        harness.setHand(player1, List.of(new EternalSkylord()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
