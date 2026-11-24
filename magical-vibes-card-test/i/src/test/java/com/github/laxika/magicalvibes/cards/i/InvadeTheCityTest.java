package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InvadeTheCity.class, Divination.class, Shock.class, GrizzlyBears.class})
class InvadeTheCityTest extends BaseCardTest {

    @Test
    void amassesBasedOnInstantAndSorceryCardsInGraveyardWithoutAnArmy() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination(), new GrizzlyBears()));

        castInvadeTheCity();

        Permanent army = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(army.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.ZOMBIE, CardSubtype.ARMY);
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, army)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, army)).isEqualTo(2);
    }

    @Test
    void amassesOnAnExistingArmyAndMakesItZombie() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination(), new GrizzlyBears()));
        Permanent army = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        PermanentGrants.subtype(gd, army, CardSubtype.ARMY);

        castInvadeTheCity();

        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(PermanentGrants.subtypesOf(gd, army)).contains(CardSubtype.ZOMBIE);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .isEmpty();
    }

    private void castInvadeTheCity() {
        harness.castFromHand(player1, new InvadeTheCity(), "{1}{U}{R}");
        harness.passBothPriorities();
    }

    @Test
    void zeroAmassCreatesAnArmyThatDiesWithoutCountingTheResolvingSpell() {
        harness.setGraveyard(player1, List.of());

        castInvadeTheCity();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Invade the City");
    }

    @Test
    void zeroAmassStillMakesAnExistingArmyZombie() {
        harness.setGraveyard(player1, List.of());
        Permanent army = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        PermanentGrants.subtype(gd, army, CardSubtype.ARMY);

        castInvadeTheCity();

        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(PermanentGrants.subtypesOf(gd, army)).contains(CardSubtype.ZOMBIE);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(army);
    }

    @Test
    void countsGraveyardAtResolutionIncludingEarlierCopies() {
        harness.setGraveyard(player1, List.of());
        harness.castFromHand(player1, new InvadeTheCity(), "{1}{U}{R}");
        harness.setGraveyard(player1, List.of(new InvadeTheCity(), new InvadeTheCity()));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(army -> assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(2));
    }

    @Test
    void ignoresOpponentsArmyAndGraveyard() {
        harness.setGraveyard(player1, List.of(new InvadeTheCity()));
        harness.setGraveyard(player2, List.of(new InvadeTheCity(), new InvadeTheCity()));
        Permanent opposingArmy = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        PermanentGrants.subtype(gd, opposingArmy, CardSubtype.ARMY);

        castInvadeTheCity();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(army -> assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(1));
        assertThat(opposingArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(PermanentGrants.subtypesOf(gd, opposingArmy)).doesNotContain(CardSubtype.ZOMBIE);
    }

    @Test
    void choosesOnlyOneArmyAndMakesOnlyThatArmyZombie() {
        harness.setGraveyard(player1, List.of(new InvadeTheCity()));
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        PermanentGrants.subtype(gd, first, CardSubtype.ARMY);
        PermanentGrants.subtype(gd, second, CardSubtype.ARMY);

        castInvadeTheCity();
        harness.handlePermanentChosen(player1, second.getId());

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(PermanentGrants.subtypesOf(gd, first)).doesNotContain(CardSubtype.ZOMBIE);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(PermanentGrants.subtypesOf(gd, second)).contains(CardSubtype.ZOMBIE);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    void cannotDeclineChoosingAnArmyWhenMultipleArmiesExist() {
        harness.setGraveyard(player1, List.of(new InvadeTheCity()));
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        PermanentGrants.subtype(gd, first, CardSubtype.ARMY);
        PermanentGrants.subtype(gd, second, CardSubtype.ARMY);

        castInvadeTheCity();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
