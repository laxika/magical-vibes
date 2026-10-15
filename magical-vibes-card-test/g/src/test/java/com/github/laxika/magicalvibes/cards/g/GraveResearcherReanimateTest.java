package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.Reanimate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GraveResearcherReanimate.class, GrizzlyBears.class, Reanimate.class})
class GraveResearcherReanimateTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes prepared after surveil leaves three creature cards in the graveyard")
    void becomesPreparedAfterSurveilReachesThreshold() {
        Permanent researcher = addResearcher();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        gd.playerDecks.get(player1.getId()).add(0, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(researcher.isPrepared()).isTrue();
        assertThat(gd.findExiledCard(researcher.getPreparedSpellCardId())).isNotNull();
    }

    @Test
    @DisplayName("Does not become prepared with fewer than three creature cards in the graveyard")
    void doesNotBecomePreparedBelowThreshold() {
        Permanent researcher = addResearcher();
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        gd.playerDecks.get(player1.getId()).add(0, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(researcher.isPrepared()).isFalse();
    }

    @Test
    @DisplayName("Casting the prepared Reanimate copy unprepares Grave Researcher and reanimates a creature")
    void castingPreparedCopyUnpreparesAndReanimates() {
        Permanent researcher = addResearcher();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target, new GrizzlyBears(), new GrizzlyBears()));
        gd.playerDecks.get(player1.getId()).add(0, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        UUID copyId = researcher.getPreparedSpellCardId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromExile(player1, copyId, target.getId());
        harness.passBothPriorities();

        assertThat(researcher.isPrepared()).isFalse();
        assertThat(researcher.getPreparedSpellCardId()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        harness.assertLife(player1, 18);
    }

    @Test
    void keepingTopCardStillPreparesWithThreeCreatures() {
        Permanent researcher = addResearcher();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        Card top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(researcher.isPrepared()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void nonCreatureCardsDoNotCountTowardThreshold() {
        Permanent researcher = addResearcher();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Reanimate()));
        harness.setLibrary(player1, List.of(new Reanimate()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(researcher.isPrepared()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    void opponentsGraveyardDoesNotCountTowardThreshold() {
        Permanent researcher = addResearcher();
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(researcher.isPrepared()).isFalse();
    }

    @Test
    void emptyLibraryDoesNotPreventPreparation() {
        Permanent researcher = addResearcher();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(researcher.isPrepared()).isTrue();
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent researcher = addResearcher();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        Card top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(researcher.isPrepared()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void alreadyPreparedResearcherStillSurveilsWithoutCreatingAnotherCopy() {
        Permanent researcher = addResearcher();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        UUID copyId = researcher.getPreparedSpellCardId();
        Card top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(researcher.isPrepared()).isTrue();
        assertThat(researcher.getPreparedSpellCardId()).isEqualTo(copyId);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(top);
        assertThat(gd.findExiledCard(copyId)).isNotNull();
    }

    @Test
    void preparedReanimateRequiresSorceryTiming() {
        Permanent researcher = addResearcher();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target, new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        UUID copyId = researcher.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(researcher.isPrepared()).isTrue();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
    }

    @Test
    void preparedCopyUnpreparesOnCastingEvenIfTargetLaterBecomesIllegal() {
        Permanent researcher = addResearcher();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        UUID copyId = researcher.getPreparedSpellCardId();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFromExile(player1, copyId, target.getId());
        assertThat(researcher.isPrepared()).isFalse();
        assertThat(researcher.getPreparedSpellCardId()).isNull();
        harness.setGraveyard(player2, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertNotInGraveyard(player1, "Reanimate");
    }

    private Permanent addResearcher() {
        Permanent researcher = harness.addToBattlefieldAndReturn(player1, new GraveResearcherReanimate());
        researcher.setSummoningSick(false);
        return researcher;
    }
}
