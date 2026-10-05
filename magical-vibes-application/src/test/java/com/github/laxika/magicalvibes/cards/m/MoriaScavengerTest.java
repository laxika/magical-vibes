package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FaithlessLooting;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoriaScavenger.class, FaithlessLooting.class})
class MoriaScavengerTest extends BaseCardTest {

    @Test
    void drawsAndAmassesWhenDiscardingAcreature() {
        Permanent scavenger = addCreatureReady(player1, new MoriaScavenger());
        harness.setHand(player1, List.of(new MoriaScavenger()));
        harness.setLibrary(player1, List.of(new FaithlessLooting()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(scavenger.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Moria Scavenger");
        harness.assertNotInHand(player1, "Faithless Looting");
        assertThat(findPermanents(player1, "Orc Army")).isEmpty();

        harness.passBothPriorities();

        harness.assertInHand(player1, "Faithless Looting");
        Permanent army = findPermanent(player1, "Orc Army");
        assertThat(army.getCard().getSubtypes()).containsExactly(CardSubtype.ORC, CardSubtype.ARMY);
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void drawsAndDiscardsWithoutAmassingWhenDiscardingANoncreature() {
        addCreatureReady(player1, new MoriaScavenger());
        harness.setHand(player1, List.of(new FaithlessLooting()));
        harness.setLibrary(player1, List.of(new MoriaScavenger()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player1, "Faithless Looting");
        harness.assertNotInHand(player1, "Moria Scavenger");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Moria Scavenger");
        assertThat(findPermanents(player1, "Orc Army")).isEmpty();
    }

    @Test
    void cannotActivateWithAnEmptyHand() {
        Permanent scavenger = addCreatureReady(player1, new MoriaScavenger());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new FaithlessLooting()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(scavenger.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertNotInHand(player1, "Faithless Looting");
    }

    @Test
    void hasteAllowsActivationOnTheTurnItEnters() {
        Permanent scavenger = harness.addToBattlefieldAndReturn(player1, new MoriaScavenger());
        scavenger.setSummoningSick(true);
        harness.setHand(player1, List.of(new FaithlessLooting()));
        harness.setLibrary(player1, List.of(new MoriaScavenger()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(scavenger.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Faithless Looting");
        harness.assertInHand(player1, "Moria Scavenger");
    }

    @Test
    void subsequentAmassAddsToExistingArmyWithoutCreatingAnother() {
        Permanent scavenger = addCreatureReady(player1, new MoriaScavenger());
        harness.setHand(player1, List.of(new MoriaScavenger(), new MoriaScavenger()));
        harness.setLibrary(player1, List.of(new FaithlessLooting(), new FaithlessLooting()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        Permanent army = findPermanent(player1, "Orc Army");
        scavenger.setTapped(false);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Orc Army")).containsExactly(army);
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
