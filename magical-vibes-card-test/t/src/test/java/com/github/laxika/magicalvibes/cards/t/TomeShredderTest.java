package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TomeShredder.class, Forest.class, Shock.class, MindRot.class})
class TomeShredderTest extends BaseCardTest {

    @Test
    @DisplayName("Taps, exiles an instant or sorcery, and gets a +1/+1 counter")
    void exilesInstantOrSorceryAndGetsCounter() {
        Permanent shredder = addCreatureReady(player1, new TomeShredder());
        Card land = new Forest();
        Card instant = new Shock();
        Card sorcery = new MindRot();
        harness.setGraveyard(player1, List.of(land, instant, sorcery));

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.GraveyardExileCostChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardExileCostChoice.class);
        assertThat(choice.validIndices()).containsExactly(1, 2);
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land, sorcery);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(instant);

        harness.passBothPriorities();

        assertThat(shredder.isTapped()).isTrue();
        assertThat(shredder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate without an instant or sorcery card in the graveyard")
    void cannotActivateWithoutInstantOrSorcery() {
        addCreatureReady(player1, new TomeShredder());
        harness.setGraveyard(player1, List.of(new Forest()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard");
    }

    @Test
    void canExileSorceryAndPaysCostsBeforeResolution() {
        Permanent shredder = addCreatureReady(player1, new TomeShredder());
        Card sorcery = new MindRot();
        harness.setGraveyard(player1, List.of(sorcery));

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(shredder.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(sorcery);
        assertThat(shredder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(shredder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void hasteAllowsTapAbilityWhileSummoningSick() {
        Permanent shredder = harness.addToBattlefieldAndReturn(player1, new TomeShredder());
        shredder.setSummoningSick(true);
        harness.setGraveyard(player1, List.of(new Shock()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(shredder.isTapped()).isTrue();
        assertThat(shredder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent shredder = addCreatureReady(player1, new TomeShredder());
        shredder.tap();
        Card instant = new Shock();
        harness.setGraveyard(player1, List.of(instant));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(instant);
        assertThat(shredder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotPayWithOpponentsGraveyard() {
        Permanent shredder = addCreatureReady(player1, new TomeShredder());
        Card instant = new Shock();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(instant));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard");

        assertThat(shredder.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(instant);
    }
}
