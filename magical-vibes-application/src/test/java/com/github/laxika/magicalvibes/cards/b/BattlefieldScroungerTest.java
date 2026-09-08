package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BattlefieldScrounger.class, GrizzlyBears.class})
class BattlefieldScroungerTest extends BaseCardTest {

    @Test
    void putsThreeGraveyardCardsOnLibraryBottomAndBoostsOncePerTurn() {
        Permanent scrounger = addCreatureReady(player1, new BattlefieldScrounger());
        List<Card> graveyard = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears());
        List<Card> selected = List.copyOf(graveyard.subList(0, 3));
        List<Card> remaining = List.copyOf(graveyard.subList(3, 10));
        harness.setGraveyard(player1, graveyard);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.ActivatedAbilityGraveyardLibraryCostChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ActivatedAbilityGraveyardLibraryCostChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultipleCardsChosen(player1, selected.stream().map(Card::getId).toList());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, scrounger)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, scrounger)).isEqualTo(6);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(remaining);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(selected);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requiresSevenCardsInGraveyard() {
        addCreatureReady(player1, new BattlefieldScrounger());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
