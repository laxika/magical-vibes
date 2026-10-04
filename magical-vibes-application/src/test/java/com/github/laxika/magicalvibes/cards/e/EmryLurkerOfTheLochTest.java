package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.ForeverYoung;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmryLurkerOfTheLoch.class, Forest.class, Ornithopter.class, Gingerbrute.class, ForeverYoung.class})
class EmryLurkerOfTheLochTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for artifacts reduces Emry's casting cost")
    void affinityForArtifactsReducesCastingCost() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.setHand(player1, List.of(new EmryLurkerOfTheLoch()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Entering the battlefield mills four cards")
    void entersMillsFour() {
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new EmryLurkerOfTheLoch()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Can cast the targeted artifact from the graveyard this turn")
    void castsTargetedArtifactFromGraveyard() {
        Permanent emry = addReadyEmry();
        Ornithopter ornithopter = new Ornithopter();
        harness.setGraveyard(player1, List.of(ornithopter));

        activate(emry, ornithopter);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(ornithopter);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == ornithopter);
    }

    @Test
    @DisplayName("Only artifact cards in the controller's own graveyard are legal targets")
    void onlyOwnArtifactsAreTargetable() {
        Permanent emry = addReadyEmry();
        Card nonArtifact = new Forest();
        Card opponentArtifact = new Ornithopter();
        harness.setGraveyard(player1, List.of(nonArtifact));
        harness.setGraveyard(player2, List.of(opponentArtifact));

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(emry);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, index, 0, null, nonArtifact.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, index, 0, null, opponentArtifact.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opposingArtifactsDoNotReduceCastingCost() {
        harness.addToBattlefield(player2, new Gingerbrute());
        harness.addToBattlefield(player2, new Gingerbrute());
        harness.setHand(player1, List.of(new EmryLurkerOfTheLoch()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void affinityDoesNotRemoveTheBlueManaRequirement() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new Gingerbrute());
        }
        harness.setHand(player1, List.of(new EmryLurkerOfTheLoch()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void millsAllRemainingCardsWhenLibraryHasFewerThanFour() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new EmryLurkerOfTheLoch()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    void summoningSicknessPreventsActivatingTheTapAbility() {
        harness.addToBattlefield(player1, new EmryLurkerOfTheLoch());
        Gingerbrute artifact = new Gingerbrute();
        harness.setGraveyard(player1, List.of(artifact));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void graveyardCastingStillRequiresPayingTheArtifactsCost() {
        Permanent emry = addReadyEmry();
        Gingerbrute artifact = new Gingerbrute();
        harness.setGraveyard(player1, List.of(artifact));
        activate(emry, artifact);
        assertThat(emry.isTapped()).isTrue();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Gingerbrute");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void permissionDoesNotGrantInstantSpeedCasting() {
        Permanent emry = addReadyEmry();
        Gingerbrute artifact = new Gingerbrute();
        harness.setGraveyard(player1, List.of(artifact));
        activate(emry, artifact);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void resolvedPermissionSurvivesEmryLeavingTheBattlefield() {
        Permanent emry = addReadyEmry();
        Gingerbrute artifact = new Gingerbrute();
        harness.setGraveyard(player1, List.of(artifact));
        activate(emry, artifact);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, emry));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromGraveyard(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Gingerbrute");
    }

    @Test
    void castingAndSacrificingTheArtifactDoesNotAllowASecondCast() {
        Permanent emry = addReadyEmry();
        Gingerbrute artifact = new Gingerbrute();
        harness.setGraveyard(player1, List.of(artifact));
        activate(emry, artifact);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromGraveyard(player1, 0);
        resolveAllTriggers();
        int artifactIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Gingerbrute"));
        harness.activateAbility(player1, artifactIndex, 1, null, null);
        resolveAllTriggers();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void leavingGraveyardWithoutCastingAlsoInvalidatesThePermission() {
        Permanent emry = addReadyEmry();
        Gingerbrute artifact = new Gingerbrute();
        harness.setGraveyard(player1, List.of(artifact));
        activate(emry, artifact);
        harness.setHand(player1, List.of(new ForeverYoung()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castSorcery(player1, 0, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
        harness.castCreature(player1, gd.playerHands.get(player1.getId()).indexOf(artifact));
        resolveAllTriggers();
        int artifactIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Gingerbrute"));
        harness.activateAbility(player1, artifactIndex, 1, null, null);
        resolveAllTriggers();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void permissionExpiresAtTheEndOfTheTurn() {
        Permanent emry = addReadyEmry();
        Gingerbrute artifact = new Gingerbrute();
        harness.setGraveyard(player1, List.of(artifact));
        activate(emry, artifact);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyEmry() {
        return addCreatureReady(player1, new EmryLurkerOfTheLoch());
    }

    private void activate(Permanent emry, Card target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(emry);
        harness.activateAbilityWithGraveyardTargets(player1, index, 0, List.of(target.getId()));
        harness.passBothPriorities();
    }
}
