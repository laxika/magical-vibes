package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.p.ParallelLives;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeraldOfTheDreadhorde.class, Murder.class, GrizzlyBears.class, ParallelLives.class})
class HeraldOfTheDreadhordeTest extends BaseCardTest {

    @Test
    @DisplayName("When Herald of the Dreadhorde dies, it amasses Zombies 2 without an Army")
    void deathTriggerCreatesZombieArmy() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new HeraldOfTheDreadhorde());

        destroyHerald(herald.getId());

        Permanent army = findPermanent(player1, "Zombie Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(army.getEffectivePower()).isEqualTo(2);
        assertThat(army.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("When Herald of the Dreadhorde dies, it amasses Zombies 2 on an existing Army")
    void deathTriggerAmassesOnExistingArmy() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new HeraldOfTheDreadhorde());

        destroyHerald(herald.getId());

        assertThat(findPermanents(player1, "Zombie Army")).isEmpty();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
    }

    @Test
    void amassesOnlyOnTheChosenArmy() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HeraldOfTheDreadhorde());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HeraldOfTheDreadhorde());
        first.getGrantedSubtypes().add(CardSubtype.ARMY);
        second.getGrantedSubtypes().add(CardSubtype.ARMY);
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new HeraldOfTheDreadhorde());

        destroyHerald(herald.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanents(player1, "Zombie Army")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotDeclineToChooseAnArmy() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HeraldOfTheDreadhorde());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HeraldOfTheDreadhorde());
        first.getGrantedSubtypes().add(CardSubtype.ARMY);
        second.getGrantedSubtypes().add(CardSubtype.ARMY);
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new HeraldOfTheDreadhorde());

        destroyHerald(herald.getId());

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId()));
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void tokenDoublingStillPutsCountersOnOnlyOneArmy() {
        harness.addToBattlefield(player1, new ParallelLives());
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new HeraldOfTheDreadhorde());

        destroyHerald(herald.getId());

        List<Permanent> armies = findPermanents(player1, "Zombie Army");
        assertThat(armies).hasSize(2);
        harness.handleMultiplePermanentsChosen(player1, List.of(armies.getFirst().getId()));

        List<Permanent> survivors = findPermanents(player1, "Zombie Army");
        assertThat(survivors).hasSize(1);
        assertThat(survivors.getFirst().getId()).isEqualTo(armies.getFirst().getId());
        assertThat(survivors.getFirst().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void opposingArmyDoesNotPreventCreatingOwnArmy() {
        Permanent opposingArmy = harness.addToBattlefieldAndReturn(player2, new HeraldOfTheDreadhorde());
        opposingArmy.getGrantedSubtypes().add(CardSubtype.ARMY);
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new HeraldOfTheDreadhorde());

        destroyHerald(herald.getId());

        assertThat(findPermanent(player1, "Zombie Army").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
        assertThat(opposingArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void destroyHerald(UUID heraldId) {
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, heraldId);
        harness.passBothPriorities();
    }
}
