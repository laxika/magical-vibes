package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreadhordeTwins.class, GrizzlyBears.class})
class DreadhordeTwinsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and amasses Zombies 2 without an Army")
    void entersAndAmassesWithoutAnArmy() {
        harness.castFromHand(player1, new DreadhordeTwins(), "{3}{R}");
        harness.passBothPriorities();
        Permanent twins = findPermanent(player1, "Dreadhorde Twins");
        harness.passBothPriorities();

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
        Permanent army = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
        harness.castFromHand(player1, new DreadhordeTwins(), "{3}{R}");
        harness.passBothPriorities();
        Permanent twins = findPermanent(player1, "Dreadhorde Twins");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .isEmpty();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(gqs.hasKeyword(gd, army, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, twins, Keyword.TRAMPLE)).isFalse();
    }
}
