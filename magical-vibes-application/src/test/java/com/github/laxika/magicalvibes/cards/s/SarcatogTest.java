package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CatalystStone;
import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Sarcatog.class, DuskImp.class, CatalystStone.class})
class SarcatogTest extends BaseCardTest {

    @Test
    void exilingTwoGraveyardCardsBoostsSarcatog() {
        Permanent sarcatog = harness.addToBattlefieldAndReturn(player1, new Sarcatog());
        harness.setGraveyard(player1, List.of(new DuskImp(), new DuskImp()));
        int powerBefore = gqs.getEffectivePower(gd, sarcatog);
        int toughnessBefore = gqs.getEffectiveToughness(gd, sarcatog);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sarcatog)).isEqualTo(powerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, sarcatog)).isEqualTo(toughnessBefore + 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
    }

    @Test
    void sacrificingAnArtifactBoostsSarcatog() {
        Permanent sarcatog = harness.addToBattlefieldAndReturn(player1, new Sarcatog());
        harness.addToBattlefield(player1, new CatalystStone());
        int powerBefore = gqs.getEffectivePower(gd, sarcatog);
        int toughnessBefore = gqs.getEffectiveToughness(gd, sarcatog);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Catalyst Stone");
        harness.assertInGraveyard(player1, "Catalyst Stone");
        assertThat(gqs.getEffectivePower(gd, sarcatog)).isEqualTo(powerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, sarcatog)).isEqualTo(toughnessBefore + 1);
    }

    @Test
    void cannotExileTwoCardsWithoutTwoCardsInGraveyard() {
        harness.addToBattlefield(player1, new Sarcatog());
        harness.setGraveyard(player1, List.of(new DuskImp()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exilesExactlyTwoChosenCardsFromLargerGraveyard() {
        Permanent sarcatog = harness.addToBattlefieldAndReturn(player1, new Sarcatog());
        DuskImp first = new DuskImp();
        DuskImp second = new DuskImp();
        DuskImp remaining = new DuskImp();
        harness.setGraveyard(player1, List.of(first, second, remaining));
        int powerBefore = gqs.getEffectivePower(gd, sarcatog);
        int toughnessBefore = gqs.getEffectiveToughness(gd, sarcatog);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ActivatedAbilityGraveyardExileCostChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sarcatog)).isEqualTo(powerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, sarcatog)).isEqualTo(toughnessBefore + 1);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(remaining);
    }

    @Test
    void cannotExileCardsFromOpponentsGraveyard() {
        harness.addToBattlefield(player1, new Sarcatog());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new DuskImp(), new DuskImp()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void cannotSacrificeWithoutAnArtifact() {
        Permanent sarcatog = harness.addToBattlefieldAndReturn(player1, new Sarcatog());
        harness.addToBattlefield(player1, new DuskImp());
        int powerBefore = gqs.getEffectivePower(gd, sarcatog);
        int toughnessBefore = gqs.getEffectiveToughness(gd, sarcatog);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");

        assertThat(gqs.getEffectivePower(gd, sarcatog)).isEqualTo(powerBefore);
        assertThat(gqs.getEffectiveToughness(gd, sarcatog)).isEqualTo(toughnessBefore);
        harness.assertOnBattlefield(player1, "Dusk Imp");
    }

    @Test
    void cannotSacrificeOpponentsArtifact() {
        harness.addToBattlefield(player1, new Sarcatog());
        harness.addToBattlefield(player2, new CatalystStone());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");
        harness.assertOnBattlefield(player2, "Catalyst Stone");
    }

    @Test
    void choosesWhichArtifactToSacrificeWhenMultipleAreAvailable() {
        Permanent sarcatog = harness.addToBattlefieldAndReturn(player1, new Sarcatog());
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player1, new CatalystStone());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player1, new CatalystStone());
        int powerBefore = gqs.getEffectivePower(gd, sarcatog);
        int toughnessBefore = gqs.getEffectiveToughness(gd, sarcatog);

        harness.activateAbility(player1, 0, 1, null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstArtifact.getId(), secondArtifact.getId());

        harness.handlePermanentChosen(player1, secondArtifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(firstArtifact)
                .doesNotContain(secondArtifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(secondArtifact.getCard());
        assertThat(gqs.getEffectivePower(gd, sarcatog)).isEqualTo(powerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, sarcatog)).isEqualTo(toughnessBefore + 1);
    }

    @Test
    void boostsWearOffAtEndOfTurn() {
        Permanent sarcatog = harness.addToBattlefieldAndReturn(player1, new Sarcatog());
        harness.addToBattlefield(player1, new CatalystStone());
        int powerBefore = gqs.getEffectivePower(gd, sarcatog);
        int toughnessBefore = gqs.getEffectiveToughness(gd, sarcatog);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sarcatog)).isEqualTo(powerBefore);
        assertThat(gqs.getEffectiveToughness(gd, sarcatog)).isEqualTo(toughnessBefore);
    }
}
