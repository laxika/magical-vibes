package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DefiantSurvivor.class, GrizzlyBears.class, Forest.class})
class DefiantSurvivorTest extends BaseCardTest {

    @Test
    void tappedSurvivorManifestsDreadAtPostcombatMain() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new DefiantSurvivor());
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        survivor.tap();
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));

        advanceToPostcombatMain();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(graveyardCard);
    }

    @Test
    void untappedSurvivorDoesNotTrigger() {
        harness.addToBattlefieldAndReturn(player1, new DefiantSurvivor());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

        advanceToPostcombatMain();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void untappingBeforeResolutionPreventsManifestingDread() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new DefiantSurvivor());
        survivor.tap();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

        advanceToPostcombatMain();
        survivor.untap();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(Permanent::isManifested);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void mayManifestNoncreatureAndPutCreatureIntoGraveyard() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new DefiantSurvivor());
        Card creature = new DefiantSurvivor();
        Card land = new Forest();
        survivor.tap();
        harness.setLibrary(player1, List.of(creature, land));

        advanceToPostcombatMain();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested() && permanent.isFaceDown()
                        && permanent.getCard().getId().equals(land.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void manifestedCreatureCanTurnFaceUpForItsManaCost() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new DefiantSurvivor());
        Card creature = new DefiantSurvivor();
        survivor.tap();
        harness.setLibrary(player1, List.of(creature, new Forest()));

        advanceToPostcombatMain();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.turnFaceUp(player1, 1);

        Permanent turnedUp = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(turnedUp.getCard().getId()).isEqualTo(creature.getId());
        assertThat(turnedUp.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void singleCardLibraryManifestsItsOnlyCard() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new DefiantSurvivor());
        Card card = new Forest();
        survivor.tap();
        harness.setLibrary(player1, List.of(card));

        advanceToPostcombatMain();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(card.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void emptyLibraryDoesNotManifestOrRequestChoice() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new DefiantSurvivor());
        survivor.tap();
        harness.setLibrary(player1, List.of());

        advanceToPostcombatMain();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(survivor);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerDuringOpponentsSecondMainPhase() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new DefiantSurvivor());
        survivor.tap();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_OF_COMBAT);

        harness.passUntil(player2, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotTriggerAgainDuringThirdMainPhase() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new DefiantSurvivor());
        survivor.tap();
        harness.setLibrary(player1, List.of());
        advanceToPostcombatMain();
        harness.passBothPriorities();
        gd.additionalCombatMainPhasePairs = 1;

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void advanceToPostcombatMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
    }
}
