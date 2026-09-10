package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AustereCommand;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinDarkDwellers.class, Shock.class, CounselOfTheSoratami.class,
        AustereCommand.class, GrizzlyBears.class})
class GoblinDarkDwellersTest extends BaseCardTest {

    @Test
    @DisplayName("ETB targets only your instant or sorcery cards with mana value 3 or less")
    void etbTargetsOnlyQualifyingCards() {
        Card shock = new Shock();
        Card counselOfTheSoratami = new CounselOfTheSoratami();
        Card austereCommand = new AustereCommand();
        Card creature = new GrizzlyBears();
        Card opponentShock = new Shock();
        harness.setGraveyard(player1, List.of(shock, counselOfTheSoratami, austereCommand, creature));
        harness.setGraveyard(player2, List.of(opponentShock));
        harness.setHand(player1, List.of(new GoblinDarkDwellers()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(shock.getId(), counselOfTheSoratami.getId());
    }

    @Test
    @DisplayName("ETB casts the chosen spell for free and exiles it after resolution")
    void castsChosenSpellForFreeAndExilesIt() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GoblinDarkDwellers()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(shock.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(shock.getId()));
    }
}
