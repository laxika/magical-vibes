package com.github.laxika.magicalvibes.cards.g;

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

@CardUsed({GrimInitiate.class, Murder.class, GrizzlyBears.class})
class GrimInitiateTest extends BaseCardTest {

    @Test
    @DisplayName("When Grim Initiate dies, it amasses Zombies 1 without an Army")
    void deathTriggerCreatesZombieArmy() {
        Permanent initiate = harness.addToBattlefieldAndReturn(player1, new GrimInitiate());

        destroyInitiate(initiate.getId());

        Permanent army = findPermanent(player1, "Zombie Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(army.getEffectivePower()).isEqualTo(1);
        assertThat(army.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Grim Initiate dies, it amasses Zombies 1 on an existing Army")
    void deathTriggerAmassesOnExistingArmy() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
        Permanent initiate = harness.addToBattlefieldAndReturn(player1, new GrimInitiate());

        destroyInitiate(initiate.getId());

        assertThat(findPermanents(player1, "Zombie Army")).isEmpty();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
    }

    @Test
    @DisplayName("Amass chooses only one of multiple controlled Armies")
    void deathTriggerChoosesOneArmy() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrimInitiate());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrimInitiate());
        first.getGrantedSubtypes().add(CardSubtype.ARMY);
        second.getGrantedSubtypes().add(CardSubtype.ARMY);
        Permanent initiate = harness.addToBattlefieldAndReturn(player1, new GrimInitiate());

        destroyInitiate(initiate.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Zombie Army")).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Army does not prevent creating your own Army")
    void deathTriggerIgnoresOpponentsArmy() {
        Permanent opponentArmy = harness.addToBattlefieldAndReturn(player2, new GrimInitiate());
        opponentArmy.getGrantedSubtypes().add(CardSubtype.ARMY);
        Permanent initiate = harness.addToBattlefieldAndReturn(player1, new GrimInitiate());

        destroyInitiate(initiate.getId());

        assertThat(findPermanent(player1, "Zombie Army").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
        assertThat(opponentArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Amass checks for an Army when the death trigger resolves")
    void deathTriggerUsesArmyThatArrivesBeforeResolution() {
        Permanent initiate = harness.addToBattlefieldAndReturn(player1, new GrimInitiate());
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, initiate.getId());
        Permanent army = harness.addToBattlefieldAndReturn(player1, new GrimInitiate());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);

        harness.passBothPriorities();

        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Zombie Army")).isEmpty();
    }
    private void destroyInitiate(UUID initiateId) {
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, initiateId);
        harness.passBothPriorities();
    }
}
