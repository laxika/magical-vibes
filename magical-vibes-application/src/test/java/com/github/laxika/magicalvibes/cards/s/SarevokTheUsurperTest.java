package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SarevokTheUsurper.class, Forest.class, GrizzlyBears.class, Swamp.class,
        Plains.class, Island.class, Mountain.class, ShockingGrasp.class, CounselOfTheSoratami.class})
class SarevokTheUsurperTest extends BaseCardTest {

    @Test
    void beginningOfCombatBoostCountsCreatureCardsInGraveyard() {
        harness.addToBattlefield(player1, new SarevokTheUsurper());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToCombat(player1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
    }

    @Test
    void greenSpecializationGrantsTrampleToTheCombatTarget() {
        harness.addToBattlefield(player1, new SarevokTheUsurper());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 4, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent sarevok = findPermanent(player1, "Sarevok, Mighty Usurper");
        assertThat(gqs.hasKeyword(gd, sarevok, Keyword.TRAMPLE)).isTrue();

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
    }

    @Test
    void blackSpecializationSeeksACreatureAndConjuresTwoDuplicates() {
        harness.addToBattlefield(player1, new SarevokTheUsurper());
        harness.setHand(player1, List.of(new Swamp()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Sarevok, Deadly Usurper")).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears"))
                .hasSize(3);
    }

    @Test
    void whiteSpecializationGrantsFirstStrikeEvenWithNoCreatureCardsInGraveyard() {
        harness.addToBattlefield(player1, new SarevokTheUsurper());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Sarevok, Divine Usurper"), Keyword.FIRST_STRIKE)).isTrue();
        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void redSpecializationGrantsMenaceAndBoostUntilEndOfTurn() {
        harness.addToBattlefield(player1, new SarevokTheUsurper());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 3, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Sarevok, Ferocious Usurper"), Keyword.MENACE)).isTrue();
        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
    }

    @Test
    void blueSpecializationCountsCreaturesInstantsAndSorceriesButNotLandsOrOpponentsCards() {
        harness.addToBattlefield(player1, new SarevokTheUsurper());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new ShockingGrasp(),
                new CounselOfTheSoratami(), new Forest()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new ShockingGrasp()));
        harness.setHand(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void unspecializedBoostCountsOnlyControllersCreaturesAtResolution() {
        harness.addToBattlefield(player1, new SarevokTheUsurper());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(),
                new Forest(), new ShockingGrasp(), new CounselOfTheSoratami()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void combatAbilityDoesNotTriggerDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new SarevokTheUsurper());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    void blackSpecializationWithNoCreatureInLibraryDoesNotConjureCards() {
        harness.addToBattlefield(player1, new SarevokTheUsurper());
        harness.setHand(player1, List.of(new Swamp()));
        harness.setLibrary(player1, List.of(new Forest(), new ShockingGrasp()));
        harness.setGraveyard(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Sarevok, Deadly Usurper")).isNotNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Swamp");
    }

    @Test
    void blackCombatBoostCountsTheSoughtCreatureAndBothDuplicates() {
        harness.addToBattlefield(player1, new SarevokTheUsurper());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Swamp()));
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
    }

    @Test
    void greenSpecializationCanDiscardAGreenCreatureAndCountItForTheBoost() {
        harness.addToBattlefield(player1, new SarevokTheUsurper());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 4, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent sarevok = findPermanent(player1, "Sarevok, Mighty Usurper");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, sarevok.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sarevok)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, sarevok)).isEqualTo(4);
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
