package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FloralEvoker.class, Forest.class, GrizzlyBears.class})
class FloralEvokerTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall puts a +1/+1 counter on Floral Evoker")
    void landfallPutsCounterOnSource() {
        Permanent evoker = harness.addToBattlefieldAndReturn(player1, new FloralEvoker());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(evoker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The activated ability discards a creature and returns a land tapped")
    void returnsTargetLandTapped() {
        FloralEvoker evoker = new FloralEvoker();
        Forest land = new Forest();
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(evoker, land));
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(land.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(evoker.getId(), discarded.getId());
        Permanent returnedLand = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(land.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returnedLand.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The activated ability requires a creature card to discard")
    void requiresCreatureDiscard() {
        FloralEvoker evoker = new FloralEvoker();
        Forest land = new Forest();
        harness.setGraveyard(player1, List.of(evoker, land));
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The activated ability can target only a land card in the graveyard")
    void requiresLandTarget() {
        FloralEvoker evoker = new FloralEvoker();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(evoker, creature));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
