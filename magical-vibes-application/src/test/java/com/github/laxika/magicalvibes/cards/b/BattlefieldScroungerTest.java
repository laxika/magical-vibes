package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AnuridSwarmsnapper;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BattlefieldScrounger.class, AnuridSwarmsnapper.class})
class BattlefieldScroungerTest extends BaseCardTest {

    @Test
    void putsThreeGraveyardCardsOnLibraryBottomAndBoostsOncePerTurn() {
        Permanent scrounger = addCreatureReady(player1, new BattlefieldScrounger());
        List<Card> graveyard = List.of(
                new AnuridSwarmsnapper(), new AnuridSwarmsnapper(), new AnuridSwarmsnapper(),
                new AnuridSwarmsnapper(), new AnuridSwarmsnapper(), new AnuridSwarmsnapper(),
                new AnuridSwarmsnapper(), new AnuridSwarmsnapper(), new AnuridSwarmsnapper(),
                new AnuridSwarmsnapper());
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
                new AnuridSwarmsnapper(), new AnuridSwarmsnapper(), new AnuridSwarmsnapper(),
                new AnuridSwarmsnapper(), new AnuridSwarmsnapper(), new AnuridSwarmsnapper()));
        harness.setGraveyard(player2, List.of(
                new AnuridSwarmsnapper(), new AnuridSwarmsnapper(), new AnuridSwarmsnapper(),
                new AnuridSwarmsnapper(), new AnuridSwarmsnapper(), new AnuridSwarmsnapper(),
                new AnuridSwarmsnapper()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canActivateWithExactlySevenCardsInOwnGraveyard() {
        Permanent scrounger = addCreatureReady(player1, new BattlefieldScrounger());
        List<Card> graveyard = List.of(
                new AnuridSwarmsnapper(), new AnuridSwarmsnapper(), new AnuridSwarmsnapper(),
                new AnuridSwarmsnapper(), new AnuridSwarmsnapper(), new AnuridSwarmsnapper(),
                new AnuridSwarmsnapper());
        List<Card> selected = List.copyOf(graveyard.subList(0, 3));
        List<Card> remaining = List.copyOf(graveyard.subList(3, 7));
        harness.setGraveyard(player1, graveyard);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.ActivatedAbilityGraveyardLibraryCostChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ActivatedAbilityGraveyardLibraryCostChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.minimumCards()).isEqualTo(3);
        assertThat(choice.maximumCards()).isEqualTo(3);
        assertThat(choice.validCardIds()).containsExactlyElementsOf(
                graveyard.stream().map(Card::getId).toList());
        harness.handleMultipleCardsChosen(player1, selected.stream().map(Card::getId).toList());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, scrounger)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, scrounger)).isEqualTo(6);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(remaining);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(selected);
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent scrounger = addCreatureReady(player1, new BattlefieldScrounger());
        List<Card> graveyard = List.of(
                new AnuridSwarmsnapper(), new AnuridSwarmsnapper(), new AnuridSwarmsnapper(),
                new AnuridSwarmsnapper(), new AnuridSwarmsnapper(), new AnuridSwarmsnapper(),
                new AnuridSwarmsnapper());
        harness.setGraveyard(player1, graveyard);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.handleMultipleCardsChosen(player1,
                graveyard.subList(0, 3).stream().map(Card::getId).toList());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, scrounger)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, scrounger)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, scrounger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, scrounger)).isEqualTo(3);
    }
}
