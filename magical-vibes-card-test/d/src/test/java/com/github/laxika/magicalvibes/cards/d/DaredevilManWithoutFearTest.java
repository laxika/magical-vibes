package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DaredevilManWithoutFear.class, GrizzlyBears.class})
class DaredevilManWithoutFearTest extends BaseCardTest {

    @Test
    void radarSenseShowsTheCurrentTopCardOnlyToItsController() {
        harness.addToBattlefield(player1, new DaredevilManWithoutFear());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Grizzly Bears"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("Grizzly Bears"));

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.clearMessages();
        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .noneMatch(message -> message.contains("Grizzly Bears"));
    }

    @Test
    void anotherCreatureAttackingTriggersWhileDaredevilStaysBack() {
        Permanent daredevil = addCreatureReady(player1, new DaredevilManWithoutFear());
        addCreatureReady(player1, new GrizzlyBears());
        DaredevilManWithoutFear topHero = new DaredevilManWithoutFear();
        harness.setLibrary(player1, List.of(topHero));

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topHero);
        assertThat(gqs.getEffectivePower(gd, daredevil)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, daredevil)).isEqualTo(5);
    }

    @Test
    void acceptingWithAnEmptyLibraryDoesNotBoost() {
        Permanent daredevil = addCreatureReady(player1, new DaredevilManWithoutFear());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, daredevil)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, daredevil)).isEqualTo(4);
    }

    @Test
    void exiledNonHeroCanBeCastInThePostcombatMainPhaseByPayingItsCost() {
        addCreatureReady(player1, new DaredevilManWithoutFear());
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void attackingWithMultipleCreaturesCreatesOneMayTriggerAndBoostsForHero() {
        Permanent daredevil = addCreatureReady(player1, new DaredevilManWithoutFear());
        addCreatureReady(player1, new GrizzlyBears());
        DaredevilManWithoutFear topHero = new DaredevilManWithoutFear();
        GrizzlyBears remainingCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topHero, remainingCard));

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topHero);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topHero.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topHero.getId());
        assertThat(gqs.getEffectivePower(gd, daredevil)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, daredevil)).isEqualTo(5);
    }

    @Test
    void attackingWithNonHeroExilesItWithoutBoosting() {
        Permanent daredevil = addCreatureReady(player1, new DaredevilManWithoutFear());
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gqs.getEffectivePower(gd, daredevil)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, daredevil)).isEqualTo(4);
    }

    @Test
    void decliningLeavesTheTopCardAndDoesNotBoost() {
        Permanent daredevil = addCreatureReady(player1, new DaredevilManWithoutFear());
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gqs.getEffectivePower(gd, daredevil)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, daredevil)).isEqualTo(4);
    }

    @Test
    void boostAndPlayPermissionExpireAtEndOfTurn() {
        Permanent daredevil = addCreatureReady(player1, new DaredevilManWithoutFear());
        DaredevilManWithoutFear topHero = new DaredevilManWithoutFear();
        harness.setLibrary(player1, List.of(topHero));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, daredevil)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, daredevil)).isEqualTo(4);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topHero.getId());
    }
}
