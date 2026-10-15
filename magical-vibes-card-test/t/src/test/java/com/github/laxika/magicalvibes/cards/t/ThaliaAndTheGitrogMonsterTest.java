package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FieldOfRuin;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
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

@CardUsed({ThaliaAndTheGitrogMonster.class, FieldOfRuin.class, Forest.class, GrizzlyBears.class})
class ThaliaAndTheGitrogMonsterTest extends BaseCardTest {

    @Test
    @DisplayName("Controller can play two lands in one turn")
    void controllerCanPlayTwoLands() {
        harness.addToBattlefield(player1, new ThaliaAndTheGitrogMonster());
        harness.setHand(player1, List.of(new Forest(), new Forest()));

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThat(countPermanents(player1, "Forest")).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponents' creatures and nonbasic lands enter tapped")
    void opponentsCreaturesAndNonbasicLandsEnterTapped() {
        harness.addToBattlefield(player1, new ThaliaAndTheGitrogMonster());
        harness.setHand(player2, List.of(new GrizzlyBears(), new FieldOfRuin()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.playLand(player2, 0);

        assertThat(findPermanent(player2, "Grizzly Bears").isTapped()).isTrue();
        assertThat(findPermanent(player2, "Field of Ruin").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attacking sacrifices a chosen creature or land, then draws a card")
    void attackingSacrificesChosenPermanentThenDraws() {
        addCreatureReady(player1, new ThaliaAndTheGitrogMonster());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(land.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void thirdLandPlayIsRefused() {
        harness.addToBattlefield(player1, new ThaliaAndTheGitrogMonster());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Forest")).isEqualTo(2);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void opponentDoesNotGetAdditionalLandPlay() {
        harness.addToBattlefield(player1, new ThaliaAndTheGitrogMonster());
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player2, 0);

        assertThatThrownBy(() -> harness.playLand(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player2, "Forest")).isEqualTo(1);
    }

    @Test
    void additionalLandPermissionEndsWhenSourceLeaves() {
        harness.addToBattlefield(player1, new ThaliaAndTheGitrogMonster());
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.playLand(player1, 0);
        gd.playerBattlefields.get(player1.getId())
                .remove(findPermanent(player1, "Thalia and The Gitrog Monster"));

        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
    }

    @Test
    void opponentBasicLandEntersUntapped() {
        harness.addToBattlefield(player1, new ThaliaAndTheGitrogMonster());
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player2, 0);

        assertThat(findPermanent(player2, "Forest").isTapped()).isFalse();
    }

    @Test
    void controllerCreatureAndNonbasicLandEnterUntapped() {
        harness.addToBattlefield(player1, new ThaliaAndTheGitrogMonster());
        harness.setHand(player1, List.of(new GrizzlyBears(), new FieldOfRuin()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isFalse();
        assertThat(findPermanent(player1, "Field of Ruin").isTapped()).isFalse();
    }

    @Test
    void onlyCreatureSacrificesItselfAndStillDraws() {
        addCreatureReady(player1, new ThaliaAndTheGitrogMonster());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Thalia and The Gitrog Monster");
        harness.assertInGraveyard(player1, "Thalia and The Gitrog Monster");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void canChooseAttackingSourceInsteadOfLand() {
        Permanent source = addCreatureReady(player1, new ThaliaAndTheGitrogMonster());
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.handleMultiplePermanentsChosen(player1, List.of(source.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Thalia and The Gitrog Monster");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void drawsEvenIfNoCreatureOrLandRemainsAtResolution() {
        addCreatureReady(player1, new ThaliaAndTheGitrogMonster());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(player1, List.of(0));
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }
}
