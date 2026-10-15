package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JukaiPreserver;
import com.github.laxika.magicalvibes.cards.k.KaitoShizuki;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TakenumaAbandonedMire.class, Forest.class, JukaiPreserver.class,
        TatsunariToadRider.class, KaitoShizuki.class})
class TakenumaAbandonedMireTest extends BaseCardTest {

    @Test
    @DisplayName("Adds black mana")
    void addsBlackMana() {
        harness.addToBattlefield(player1, new TakenumaAbandonedMire());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Channel mills three cards and returns a creature card to hand")
    void channelMillsAndReturnsCreatureWithLegendaryCostReduction() {
        Card target = new JukaiPreserver();
        harness.addToBattlefield(player1, new TatsunariToadRider());
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new TakenumaAbandonedMire()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(target);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Takenuma, Abandoned Mire");
    }

    @Test
    @DisplayName("Channel cannot return a noncreature, nonplaneswalker card")
    void channelCannotReturnNonCreatureOrPlaneswalkerCard() {
        Card target = new Forest();
        harness.setGraveyard(player1, List.of(target, new JukaiPreserver()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new TakenumaAbandonedMire()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.handleGraveyardCardChosen(player1, 1);
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Jukai Preserver");
    }

    @Test
    @DisplayName("Channel can return a creature milled during its resolution")
    void returnsNewlyMilledCreature() {
        Card creature = new JukaiPreserver();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(creature, new Forest(), new Forest()));
        prepareChannel();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(creature));
        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Channel returns a planeswalker chosen after milling")
    void returnsPlaneswalker() {
        Card planeswalker = new KaitoShizuki();
        harness.setGraveyard(player1, List.of(planeswalker));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        prepareChannel();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(planeswalker);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Channel can mill with no creature or planeswalker available")
    void millsWithoutReturnableCard() {
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        prepareChannel();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void prepareChannel() {
        harness.setHand(player1, List.of(new TakenumaAbandonedMire()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
