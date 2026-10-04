package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZombieInfestation;
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

@CardUsed({SurrakAndGoreclaw.class, GrizzlyBears.class, ZombieInfestation.class})
class SurrakAndGoreclawTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control have trample")
    void grantsTrampleToOtherCreaturesYouControl() {
        harness.addToBattlefield(player1, new SurrakAndGoreclaw());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A nontoken creature entering under your control gets a counter and haste")
    void nontokenCreatureEnteringGetsCounterAndHaste() {
        harness.addToBattlefield(player1, new SurrakAndGoreclaw());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent enteringCreature = findPermanent(player1, "Grizzly Bears");
        assertThat(enteringCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, enteringCreature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("A token entering under your control does not trigger the counter and haste ability")
    void tokenEnteringDoesNotTrigger() {
        harness.addToBattlefield(player1, new SurrakAndGoreclaw());
        harness.addToBattlefield(player1, new ZombieInfestation());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isFalse();
    }
}
