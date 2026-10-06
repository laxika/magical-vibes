package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BoneFlute;
import com.github.laxika.magicalvibes.cards.c.CavePeople;
import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Necropolis.class, Squire.class, CavePeople.class, BoneFlute.class})
class NecropolisTest extends BaseCardTest {

    @Test
    @DisplayName("Defender prevents attacking")
    void cannotAttack() {
        addCreatureReady(player1, new Necropolis());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exiling is paid before resolution, while counters wait for resolution")
    void exileCostIsPaidBeforeCountersAreAdded() {
        Permanent necropolis = addCreatureReady(player1, new Necropolis());
        harness.setGraveyard(player1, List.of(new Squire()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertNotInGraveyard(player1, "Squire");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Squire"));
        assertThat(necropolis.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(necropolis.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, necropolis)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, necropolis)).isEqualTo(3);
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick without paying mana")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent necropolis = harness.addToBattlefieldAndReturn(player1, new Necropolis());
        necropolis.setSummoningSick(true);
        necropolis.tap();
        harness.setGraveyard(player1, List.of(new CavePeople()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(necropolis.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isEqualTo(3);
        assertThat(necropolis.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot pay the exile cost using an opponent's graveyard")
    void cannotUseOpponentsGraveyard() {
        addCreatureReady(player1, new Necropolis());
        harness.setGraveyard(player2, List.of(new Squire()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player2, "Squire");
    }

    @Test
    @DisplayName("Exiles a creature and puts +0/+1 counters equal to its mana value")
    void exilesCreatureAndAddsCountersEqualToManaValue() {
        Permanent necropolis = addCreatureReady(player1, new Necropolis());
        harness.setGraveyard(player1, List.of(new Squire()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(necropolis.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, necropolis)).isEqualTo(3);
        harness.assertNotInGraveyard(player1, "Squire");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Squire"));
    }

    @Test
    @DisplayName("Cannot activate without a creature card in the graveyard")
    void cannotActivateWithoutCreatureCard() {
        addCreatureReady(player1, new Necropolis());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with only noncreature cards in the graveyard")
    void cannotActivateWithOnlyNonCreatureCards() {
        addCreatureReady(player1, new Necropolis());
        harness.setGraveyard(player1, List.of(new BoneFlute()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Each activation uses the mana value of its own exiled creature")
    void eachActivationUsesItsOwnExiledCreatureManaValue() {
        Permanent necropolis = addCreatureReady(player1, new Necropolis());
        harness.setGraveyard(player1, List.of(new Squire(), new CavePeople()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(necropolis.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isEqualTo(5);
    }
}
