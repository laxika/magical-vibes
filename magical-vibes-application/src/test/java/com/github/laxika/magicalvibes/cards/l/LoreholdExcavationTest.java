package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.EagerFirstYear;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LoreholdExcavation.class, LoreholdCampus.class, EagerFirstYear.class, LeylineOfTheVoid.class})
class LoreholdExcavationTest extends BaseCardTest {

    @Test
    @DisplayName("End-step trigger gains life when a land is milled")
    void gainsLifeForMilledLand() {
        harness.addToBattlefield(player1, new LoreholdExcavation());
        harness.setLibrary(player1, List.of(new LoreholdCampus()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveControllerEndStep();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Lorehold Campus");
    }

    @Test
    @DisplayName("End-step trigger damages each opponent when a nonland is milled")
    void damagesOpponentsForMilledNonland() {
        harness.addToBattlefield(player1, new LoreholdExcavation());
        harness.setLibrary(player1, List.of(new EagerFirstYear()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveControllerEndStep();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Ability exiles a creature card and creates a tapped red and white Spirit")
    void exilesCreatureAndCreatesTappedSpirit() {
        harness.addToBattlefield(player1, new LoreholdExcavation());
        harness.setGraveyard(player1, List.of(new EagerFirstYear()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardExileCostChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(spirit.getCard().getPower()).isEqualTo(3);
        assertThat(spirit.getCard().getToughness()).isEqualTo(2);
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(spirit.getCard().getColors()).containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
        assertThat(spirit.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
        assertThat(spirit.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(card -> card.getName())
                .contains("Eager First-Year");
    }

    @Test
    void emptyLibraryStillDamagesOpponent() {
        harness.addToBattlefield(player1, new LoreholdExcavation());
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveControllerEndStep();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    void doesNotTriggerDuringOpponentEndStep() {
        harness.addToBattlefield(player1, new LoreholdExcavation());
        EagerFirstYear topCard = new EagerFirstYear();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void landMilledIntoExileStillGainsLife() {
        harness.addToBattlefield(player1, new LoreholdExcavation());
        harness.addToBattlefield(player2, new LeylineOfTheVoid());
        LoreholdCampus land = new LoreholdCampus();
        harness.setLibrary(player1, List.of(land));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveControllerEndStep();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(land);
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotPayExileCostWithLandOrOpponentCreature() {
        harness.addToBattlefield(player1, new LoreholdExcavation());
        harness.setGraveyard(player1, List.of(new LoreholdCampus()));
        harness.setGraveyard(player2, List.of(new EagerFirstYear()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(countPermanents(player1, "Spirit")).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void creatureIsExiledBeforeTokenAbilityResolves() {
        harness.addToBattlefield(player1, new LoreholdExcavation());
        EagerFirstYear creature = new EagerFirstYear();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);
        assertThat(countPermanents(player1, "Spirit")).isZero();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }

    private void resolveControllerEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
