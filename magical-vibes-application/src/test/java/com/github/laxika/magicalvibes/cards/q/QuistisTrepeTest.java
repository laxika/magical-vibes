package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuistisTrepe.class, Shock.class, GrizzlyBears.class})
class QuistisTrepeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB targets instant or sorcery cards from either graveyard")
    void etbTargetsEitherGraveyard() {
        Shock ownShock = new Shock();
        Shock opposingShock = new Shock();
        GrizzlyBears opposingBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownShock));
        harness.setGraveyard(player2, List.of(opposingShock, opposingBears));
        harness.setHand(player1, List.of(new QuistisTrepe()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(ownShock.getId(), opposingShock.getId());
    }

    @Test
    @DisplayName("Casts the chosen spell with any mana and exiles it after resolution")
    void castsWithAnyManaAndExilesAfterResolution() {
        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(shock));
        harness.setHand(player1, List.of(new QuistisTrepe()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(shock.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(shock.getId()));
    }
}
