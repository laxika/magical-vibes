package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CloudfinRaptor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TollOfTheInvasion.class, Forest.class, GrizzlyBears.class, Peek.class, CloudfinRaptor.class})
class TollOfTheInvasionTest extends BaseCardTest {

    @Test
    @DisplayName("Discards a chosen nonland card and amasses Zombies 1")
    void discardsChosenNonlandAndAmassesWithoutAnArmy() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        castTollOfTheInvasion();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Forest");
        Permanent army = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(army.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE, CardSubtype.ARMY);
    }

    @Test
    @DisplayName("Amasses on an existing Army")
    void amassesOnExistingArmy() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
        harness.setHand(player2, List.of(new Peek()));
        castTollOfTheInvasion();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .isEmpty();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new TollOfTheInvasion()));
        addManaForToll();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    void amassesWhenOpponentsHandIsEmpty() {
        harness.setHand(player2, List.of());
        castTollOfTheInvasion();
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(army -> assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void amassesWhenHandContainsOnlyLands() {
        harness.setHand(player2, List.of(new Forest()));
        castTollOfTheInvasion();
        harness.assertInHand(player2, "Forest");
        harness.assertNotInGraveyard(player2, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(army -> assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void choosesOnlyOneOfMultipleOwnArmies() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingArmy = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        first.getGrantedSubtypes().add(CardSubtype.ARMY);
        second.getGrantedSubtypes().add(CardSubtype.ARMY);
        opposingArmy.getGrantedSubtypes().add(CardSubtype.ARMY);
        harness.setHand(player2, List.of());
        castTollOfTheInvasion();
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(first.getGrantedSubtypes()).doesNotContain(CardSubtype.ZOMBIE);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE, CardSubtype.ARMY);
        assertThat(opposingArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void newArmyEntersAsZeroZeroAndDoesNotTriggerEvolve() {
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new CloudfinRaptor());
        harness.setHand(player2, List.of());
        castTollOfTheInvasion();
        assertThat(gd.stack).isEmpty();
        assertThat(raptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(army -> assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
    }

    private void castTollOfTheInvasion() {
        harness.setHand(player1, List.of(new TollOfTheInvasion()));
        addManaForToll();
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }

    private void addManaForToll() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
