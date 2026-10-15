package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.Batterbone;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.n.Nettlecyst;
import com.github.laxika.magicalvibes.cards.s.ScaldingTarn;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DiscipleOfTheSun.class, GrizzlyBears.class, HillGiant.class, Shatter.class,
        Batterbone.class, Nettlecyst.class, ScaldingTarn.class})
class DiscipleOfTheSunTest extends BaseCardTest {

    @Test
    @DisplayName("ETB targets a permanent card with mana value 3 or less")
    void etbFiltersGraveyardCards() {
        GrizzlyBears eligible = new GrizzlyBears();
        HillGiant tooExpensive = new HillGiant();
        Shatter nonPermanent = new Shatter();
        harness.setGraveyard(player1, List.of(eligible, tooExpensive, nonPermanent));

        castDisciple();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());
    }

    @Test
    @DisplayName("ETB returns the chosen permanent card to its owner's hand")
    void returnsChosenPermanentToHand() {
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        castDisciple();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Disciple of the Sun");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB does not prompt when no eligible permanent card is in the graveyard")
    void noEligibleTargetSkipsPrompt() {
        harness.setGraveyard(player1, List.of(new HillGiant(), new Shatter()));

        castDisciple();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Disciple of the Sun");
    }

    @Test
    @DisplayName("ETB can return a noncreature permanent with mana value exactly three")
    void returnsPermanentAtManaValueBoundary() {
        Nettlecyst target = new Nettlecyst();
        Batterbone other = new Batterbone();
        ScaldingTarn land = new ScaldingTarn();
        harness.setGraveyard(player1, List.of(target, other, land));

        castDisciple();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(target.getId(), other.getId(), land.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Nettlecyst");
        harness.assertNotInGraveyard(player1, "Nettlecyst");
        harness.assertNotOnBattlefield(player1, "Nettlecyst");
        harness.assertInGraveyard(player1, "Batterbone");
        harness.assertInGraveyard(player1, "Scalding Tarn");
    }

    @Test
    @DisplayName("ETB can return a land with no mana cost")
    void returnsLandToHand() {
        ScaldingTarn target = new ScaldingTarn();
        harness.setGraveyard(player1, List.of(target));

        castDisciple();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Scalding Tarn");
        harness.assertNotInGraveyard(player1, "Scalding Tarn");
        harness.assertNotOnBattlefield(player1, "Scalding Tarn");
    }

    @Test
    @DisplayName("ETB only offers cards from its controller's graveyard")
    void excludesOpponentsGraveyard() {
        Batterbone ownCard = new Batterbone();
        Nettlecyst opponentsCard = new Nettlecyst();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentsCard));

        castDisciple();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(ownCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownCard.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Batterbone");
        harness.assertInGraveyard(player2, "Nettlecyst");
        harness.assertNotInHand(player1, "Nettlecyst");
        harness.assertNotInHand(player2, "Nettlecyst");
    }

    @Test
    @DisplayName("ETB does not choose a replacement when its target leaves the graveyard")
    void missingTargetDoesNotReturnAnotherCard() {
        Batterbone target = new Batterbone();
        Nettlecyst other = new Nettlecyst();
        harness.setGraveyard(player1, List.of(target, other));

        castDisciple();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(other));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Batterbone");
        harness.assertNotInHand(player1, "Nettlecyst");
        harness.assertInGraveyard(player1, "Nettlecyst");
    }

    @Test
    @DisplayName("Lifelink gains life when Disciple deals combat damage")
    void combatDamageGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent disciple = addCreatureReady(player1, new DiscipleOfTheSun());
        disciple.setAttacking(true);
        harness.forceActivePlayer(player1);

        harness.resolveCombatDamage();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    private void castDisciple() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new DiscipleOfTheSun(), "{4}{W}");
        harness.passBothPriorities();
    }
}
