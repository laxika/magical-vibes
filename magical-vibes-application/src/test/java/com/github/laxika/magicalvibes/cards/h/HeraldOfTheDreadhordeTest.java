package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
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

@CardUsed({HeraldOfTheDreadhorde.class, Murder.class, GrizzlyBears.class})
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

    private void destroyHerald(UUID heraldId) {
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, heraldId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
