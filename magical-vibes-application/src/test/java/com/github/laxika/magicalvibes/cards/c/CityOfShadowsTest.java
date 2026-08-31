package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NaturalAffinity;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CityOfShadows.class, Forest.class, GrizzlyBears.class, NaturalAffinity.class})
class CityOfShadowsTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling a creature puts a storage counter on City of Shadows")
    void exilingCreatureAddsStorageCounter() {
        Permanent city = addReadyCity();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(city.getCounterCount(CounterType.STORAGE)).isEqualTo(1);
        assertThat(city.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).contains(creature.getCard().getId());
    }

    @Test
    @DisplayName("The mana ability adds one colorless mana per storage counter")
    void addsColorlessManaPerStorageCounter() {
        Permanent city = addReadyCity();
        city.setCounterCount(CounterType.STORAGE, 3);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(city.getCounterCount(CounterType.STORAGE)).isEqualTo(3);
        assertThat(city.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The mana ability can be activated with no storage counters")
    void manaAbilityWithNoStorageCountersAddsNoMana() {
        Permanent city = addReadyCity();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(city.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A creature City of Shadows can pay its own exile cost")
    void canExileItselfWhenItIsACreature() {
        Permanent city = addReadyCity();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new NaturalAffinity()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, city)).isTrue();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(city);
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).contains(city.getCard().getId());
    }

    @Test
    @DisplayName("The storage ability cannot exile a noncreature permanent")
    void cannotExileNoncreaturePermanent() {
        addReadyCity();
        harness.addToBattlefield(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyCity() {
        Permanent city = harness.addToBattlefieldAndReturn(player1, new CityOfShadows());
        city.setSummoningSick(false);
        return city;
    }
}
