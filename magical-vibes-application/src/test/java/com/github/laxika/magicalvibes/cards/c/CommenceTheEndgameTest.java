package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CommenceTheEndgame.class, Cancel.class, GrizzlyBears.class})
class CommenceTheEndgameTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards, then amasses Zombies based on the resulting hand size")
    void drawsThenAmassesWithoutAnArmy() {
        harness.setHand(player1, List.of(new CommenceTheEndgame(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0);

        Permanent army = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(army.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ZOMBIE, CardSubtype.ARMY);
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Adds counters to an existing Army and makes it a Zombie")
    void amassesOnExistingArmy() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
        harness.setHand(player1, List.of(new CommenceTheEndgame(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .isEmpty();
    }

    @Test
    @DisplayName("Cannot be countered")
    void cannotBeCountered() {
        CommenceTheEndgame commence = new CommenceTheEndgame();
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castFromHand(player1, commence, "{4}{U}{U}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, commence.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Cancel");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    @Test
    @DisplayName("Chooses one of multiple Armies and only makes that Army a Zombie")
    void choosesOneArmy() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.getGrantedSubtypes().add(CardSubtype.ARMY);
        second.getGrantedSubtypes().add(CardSubtype.ARMY);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.castFromHand(player1, new CommenceTheEndgame(), "{4}{U}{U}");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(first.getGrantedSubtypes()).doesNotContain(CardSubtype.ZOMBIE);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(second.getGrantedSubtypes()).contains(CardSubtype.ARMY, CardSubtype.ZOMBIE);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An opponent's Army does not prevent creating your own Army")
    void ignoresOpponentsArmy() {
        Permanent opposingArmy = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opposingArmy.getGrantedSubtypes().add(CardSubtype.ARMY);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.castFromHand(player1, new CommenceTheEndgame(), "{4}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent army = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(army.getCard().isToken()).isTrue();
        assertThat(army.getCard().getSubtypes()).contains(CardSubtype.ARMY, CardSubtype.ZOMBIE);
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opposingArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingArmy.getGrantedSubtypes()).doesNotContain(CardSubtype.ZOMBIE);
    }
}
