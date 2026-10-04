package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LazotepReaver;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(army.getEffectivePower()).isEqualTo(2);
        assertThat(army.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, army, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("ETB amasses Zombies 2 on an existing Army without granting flying to nontokens")
    void amassesOnExistingArmy() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);

        castEternalSkylord();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .isEmpty();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
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
        first.getGrantedSubtypes().add(CardSubtype.ARMY);
        second.getGrantedSubtypes().add(CardSubtype.ARMY);

        castEternalSkylord();
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).isEmpty();
        assertThat(gd.stack).isEmpty();
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
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
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
        army.getGrantedSubtypes().add(CardSubtype.ARMY);

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
