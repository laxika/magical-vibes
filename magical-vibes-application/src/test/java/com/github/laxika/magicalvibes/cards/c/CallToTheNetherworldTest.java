package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.c.CorpulentCorpse;
import com.github.laxika.magicalvibes.cards.s.Smallpox;
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

@CardUsed({CallToTheNetherworld.class, CorpulentCorpse.class, AshcoatBear.class, Smallpox.class})
class CallToTheNetherworldTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target black creature card from your graveyard to your hand")
    void returnsTargetBlackCreatureToHand() {
        Card creature = new CorpulentCorpse();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new CallToTheNetherworld()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Cannot target a nonblack creature card")
    void cannotTargetNonblackCreature() {
        Card creature = new AshcoatBear();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new CallToTheNetherworld()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature black card")
    void cannotTargetNoncreatureCard() {
        Card card = new Smallpox();
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player1, List.of(new CallToTheNetherworld()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, card.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a black creature card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card creature = new CorpulentCorpse();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new CallToTheNetherworld()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can cast for zero madness after being discarded")
    void castsForZeroMadness() {
        Card creature = new CorpulentCorpse();
        CallToTheNetherworld call = new CallToTheNetherworld();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(call));
        harness.setHand(player2, List.of(new Smallpox()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);

        harness.castSorcery(player2, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(call.getId()));
    }

    @Test
    @DisplayName("Putting it into the graveyard declines the madness cast")
    void declinesMadnessCast() {
        CallToTheNetherworld call = new CallToTheNetherworld();
        harness.setHand(player1, List.of(call));
        harness.setHand(player2, List.of(new Smallpox()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);

        harness.castSorcery(player2, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(call.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(call.getId()));
    }
}
