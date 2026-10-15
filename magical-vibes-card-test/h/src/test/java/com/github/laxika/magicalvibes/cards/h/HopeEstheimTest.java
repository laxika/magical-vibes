package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InstantRamen;
import com.github.laxika.magicalvibes.cards.r.Revitalize;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HopeEstheim.class, GrizzlyBears.class, Revitalize.class, InstantRamen.class})
class HopeEstheimTest extends BaseCardTest {

    private void resolveEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Each opponent mills the life gained by Hope Estheim")
    void millsEachOpponentByLifeGained() {
        harness.addToBattlefield(player1, new HopeEstheim());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Revitalize()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);

        resolveEndStepTrigger();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Grizzly Bears", "Grizzly Bears");
    }

    @Test
    @DisplayName("Mills no cards when its controller gained no life")
    void millsNoCardsWithoutLifeGain() {
        harness.addToBattlefield(player1, new HopeEstheim());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        resolveEndStepTrigger();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private void gainThreeLifeWithRamen() {
        harness.addToBattlefield(player1, new InstantRamen());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).size() - 1, null, null);
        resolveAllTriggers();
    }

    @Test
    void countsLifeGainedBeforeHopeEntered() {
        gainThreeLifeWithRamen();
        harness.addToBattlefield(player1, new HopeEstheim());
        harness.setLibrary(player2, List.of(new HopeEstheim(), new HopeEstheim(),
                new HopeEstheim(), new HopeEstheim()));

        resolveEndStepTrigger();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    void countsLifeGainedInResponseToTriggerEvenWhenNoLifeWasGainedAtTriggerTime() {
        harness.addToBattlefield(player1, new HopeEstheim());
        harness.addToBattlefield(player1, new InstantRamen());
        harness.setLibrary(player2, List.of(new HopeEstheim(), new HopeEstheim(),
                new HopeEstheim(), new HopeEstheim()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    void countsTotalLifeGainedRatherThanNetLifeChange() {
        harness.addToBattlefield(player1, new HopeEstheim());
        harness.setLife(player1, 20);
        gainThreeLifeWithRamen();
        gainThreeLifeWithRamen();
        harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 8, "test life loss");
        harness.setLibrary(player2, List.of(new HopeEstheim(), new HopeEstheim(),
                new HopeEstheim(), new HopeEstheim(), new HopeEstheim(),
                new HopeEstheim(), new HopeEstheim()));

        resolveEndStepTrigger();

        harness.assertLife(player1, 18);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(6);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    void millsOnlyAvailableCardsWhenLibraryIsShort() {
        harness.addToBattlefield(player1, new HopeEstheim());
        gainThreeLifeWithRamen();
        harness.setLibrary(player1, List.of(new HopeEstheim(), new HopeEstheim()));
        harness.setLibrary(player2, List.of(new HopeEstheim()));

        resolveEndStepTrigger();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void doesNotTriggerOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new HopeEstheim());
        gainThreeLifeWithRamen();
        harness.setLibrary(player2, List.of(new HopeEstheim(), new HopeEstheim(), new HopeEstheim()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }

    @Test
    void lifelinkCombatDamageContributesToMilling() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new HopeEstheim());
        harness.setLibrary(player2, List.of(new HopeEstheim(), new HopeEstheim(), new HopeEstheim()));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        resolveEndStepTrigger();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }
}
