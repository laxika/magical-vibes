package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AnointedProcession;
import com.github.laxika.magicalvibes.cards.l.LazotepReaver;
import com.github.laxika.magicalvibes.cards.p.PouncingLynx;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreadhordeTwins.class, LazotepReaver.class, PouncingLynx.class, AnointedProcession.class})
class DreadhordeTwinsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and amasses Zombies 2 without an Army")
    void entersAndAmassesWithoutAnArmy() {
        harness.castFromHand(player1, new DreadhordeTwins(), "{3}{R}");
        resolveAllTriggers();
        Permanent twins = findPermanent(player1, "Dreadhorde Twins");

        Permanent army = findPermanent(player1, "Zombie Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(army.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE, CardSubtype.ARMY);
        assertThat(army.getEffectivePower()).isEqualTo(2);
        assertThat(army.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, army, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, twins, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Enters and amasses Zombies 2 on an existing Army")
    void entersAndAmassesOnExistingArmy() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new PouncingLynx());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
        harness.castFromHand(player1, new DreadhordeTwins(), "{3}{R}");
        resolveAllTriggers();
        Permanent twins = findPermanent(player1, "Dreadhorde Twins");

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .isEmpty();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(gqs.hasKeyword(gd, army, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, twins, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void choosesOnlyOneOfMultipleArmies() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PouncingLynx());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PouncingLynx());
        first.getGrantedSubtypes().add(CardSubtype.ARMY);
        second.getGrantedSubtypes().add(CardSubtype.ARMY);

        harness.castFromHand(player1, new DreadhordeTwins(), "{3}{R}");
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(first.getGrantedSubtypes()).doesNotContain(CardSubtype.ZOMBIE);
        assertThat(second.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(countPermanents(player1, "Zombie Army")).isZero();
    }

    @Test
    void trampleAppliesOnlyToOwnZombieTokensAndEndsWhenTwinsLeaves() {
        harness.enterBattlefieldAndReturn(player1, new LazotepReaver());
        resolveAllTriggers();
        Permanent ownArmy = findPermanent(player1, "Zombie Army");
        harness.enterBattlefieldAndReturn(player2, new LazotepReaver());
        resolveAllTriggers();
        Permanent opposingArmy = findPermanent(player2, "Zombie Army");
        Permanent twins = harness.addToBattlefieldAndReturn(player1, new DreadhordeTwins());

        assertThat(gqs.hasKeyword(gd, ownArmy, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingArmy, Keyword.TRAMPLE)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(twins);
        assertThat(gqs.hasKeyword(gd, ownArmy, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void tokenDoublingStillPutsCountersOnOnlyOneArmy() {
        harness.addToBattlefield(player1, new AnointedProcession());
        harness.castFromHand(player1, new DreadhordeTwins(), "{3}{R}");
        resolveAllTriggers();
        List<Permanent> armies = findPermanents(player1, "Zombie Army");
        assertThat(armies).hasSize(2);
        harness.handleMultiplePermanentsChosen(player1, List.of(armies.getFirst().getId()));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Zombie Army")).hasSize(1);
        assertThat(armies.getFirst().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(armies.get(1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
