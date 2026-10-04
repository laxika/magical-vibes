package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.o.OrdinaryBear;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GatheringOfDarkness.class, OrdinaryBear.class, Swamp.class})
class GatheringOfDarknessTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature card and amasses Goblins 3 without an Army")
    void returnsCreatureAndCreatesGoblinArmy() {
        Card creature = new OrdinaryBear();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new GatheringOfDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
        Permanent army = findPermanent(player1, "Goblin Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("May omit the graveyard target and amass on an existing Army")
    void omitsTargetAndAmassesOnExistingArmy() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new OrdinaryBear());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
        harness.setHand(player1, List.of(new GatheringOfDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(findPermanents(player1, "Goblin Army")).isEmpty();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Cannot target a noncreature card in the graveyard")
    void cannotTargetNoncreatureCard() {
        Card noncreature = new Swamp();
        harness.setGraveyard(player1, List.of(noncreature));
        harness.setHand(player1, List.of(new GatheringOfDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetOpponentsCreatureCard() {
        Card creature = new OrdinaryBear();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new GatheringOfDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not amass if the only chosen target leaves the graveyard")
    void doesNotAmassWhenOnlyTargetBecomesIllegal() {
        Card creature = new OrdinaryBear();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new GatheringOfDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, creature.getId());
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Goblin Army")).isEmpty();
        harness.assertNotInHand(player1, "Ordinary Bear");
        harness.assertInGraveyard(player1, "Gathering of Darkness");
    }

    @Test
    @DisplayName("Can decline an available creature target and still amass")
    void canOmitAvailableCreatureTarget() {
        Card creature = new OrdinaryBear();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new GatheringOfDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, List.of());

        harness.assertInGraveyard(player1, "Ordinary Bear");
        harness.assertNotInHand(player1, "Ordinary Bear");
        assertThat(findPermanent(player1, "Goblin Army")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's Army does not prevent creating your own Army")
    void ignoresOpponentsArmy() {
        Permanent opponentArmy = harness.addToBattlefieldAndReturn(player2, new OrdinaryBear());
        opponentArmy.getGrantedSubtypes().add(CardSubtype.ARMY);
        harness.setHand(player1, List.of(new GatheringOfDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(findPermanent(player1, "Goblin Army")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(opponentArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentArmy.getGrantedSubtypes()).doesNotContain(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Chooses one of multiple Armies to receive counters and the Goblin type")
    void choosesOneOfMultipleArmies() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new OrdinaryBear());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new OrdinaryBear());
        first.getGrantedSubtypes().add(CardSubtype.ARMY);
        second.getGrantedSubtypes().add(CardSubtype.ARMY);
        harness.setHand(player1, List.of(new GatheringOfDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(first.getGrantedSubtypes()).doesNotContain(CardSubtype.GOBLIN);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(second.getGrantedSubtypes()).contains(CardSubtype.ARMY, CardSubtype.GOBLIN);
        assertThat(findPermanents(player1, "Goblin Army")).isEmpty();
    }
}
