package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GladiolusAmicitia.class, Forest.class, GrizzlyBears.class})
class GladiolusAmicitiaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB searches for a land and puts it onto the battlefield tapped")
    void entersAndSearchesForTappedLand() {
        harness.setLibrary(player1, List.of(new Forest()));
        castGladiolus();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().hasType(CardType.LAND) && permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof GladiolusAmicitia);
    }

    @Test
    @DisplayName("Landfall boosts and grants trample to another creature you control")
    void landfallBoostsAnotherCreatureAndGrantsTrample() {
        Permanent gladiolus = harness.addToBattlefieldAndReturn(player1, new GladiolusAmicitia());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(gladiolus.getEffectivePower()).isEqualTo(6);
        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Landfall cannot target Gladiolus Amicitia itself")
    void landfallCannotTargetSource() {
        harness.addToBattlefield(player1, new GladiolusAmicitia());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(bear.getId());
    }

    private void castGladiolus() {
        harness.castFromHand(player1, new GladiolusAmicitia(), "{4}{R}{G}");
    }

    @Test
    void searchedLandTriggersLandfall() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        castGladiolus();
        resolveAllTriggers();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.handlePermanentChosen(player1, bear.getId());
        resolveAllTriggers();

        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void landfallExcludesOpponentsCreaturesAndNoncreatures() {
        harness.addToBattlefield(player1, new GladiolusAmicitia());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(bear.getId());
    }

    @Test
    void opponentsLandDoesNotTriggerLandfall() {
        harness.addToBattlefield(player1, new GladiolusAmicitia());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.enterBattlefieldAndReturn(player2, new Forest());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void landfallWithNoOtherCreatureHasNoLegalTarget() {
        Permanent gladiolus = harness.addToBattlefieldAndReturn(player1, new GladiolusAmicitia());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gladiolus.getEffectivePower()).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, gladiolus, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void entersWithNoLandInLibraryStillFinishesSearch() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castGladiolus();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().hasType(CardType.LAND));
    }

    @Test
    void landfallResolvesAfterGladiolusLeavesBattlefield() {
        Permanent gladiolus = harness.addToBattlefieldAndReturn(player1, new GladiolusAmicitia());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, bear.getId());

        gd.playerBattlefields.get(player1.getId()).remove(gladiolus);
        gd.playerGraveyards.get(player1.getId()).add(gladiolus.getCard());
        resolveAllTriggers();

        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isTrue();
    }
}
