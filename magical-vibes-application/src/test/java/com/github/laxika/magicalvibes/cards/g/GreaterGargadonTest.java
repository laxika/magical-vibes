package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GreaterGargadon.class, Forest.class, GrizzlyBears.class})
class GreaterGargadonTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Greater Gargadon with ten time counters")
    void suspendExilesWithTenTimeCounters() {
        GreaterGargadon card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 10);
    }

    @Test
    @DisplayName("The suspended ability lets its controller sacrifice a creature or land")
    void sacrificesChosenPermanentToRemoveTimeCounter() {
        GreaterGargadon card = suspendCard();
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateExiledAbility(player1, card.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(bear.getId(), forest.getId());

        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 9);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear).doesNotContain(forest);
    }

    @Test
    @DisplayName("Removing the last time counter offers the suspended card for free")
    void removingLastTimeCounterOffersFreeCast() {
        GreaterGargadon card = suspendCard();
        addCreatureReady(player1, new GrizzlyBears());
        gd.exiledCardTimeCounters.put(card.getId(), 1);

        harness.activateExiledAbility(player1, card.getId());
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == card);
    }

    @Test
    @DisplayName("The suspended ability cannot be activated while Greater Gargadon is on the battlefield")
    void abilityRequiresSuspendedSource() {
        harness.addToBattlefield(player1, new GreaterGargadon());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private GreaterGargadon suspendCard() {
        GreaterGargadon card = new GreaterGargadon();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }
}
