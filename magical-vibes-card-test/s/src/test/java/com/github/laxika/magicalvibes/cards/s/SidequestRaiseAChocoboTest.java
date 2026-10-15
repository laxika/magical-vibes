package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BlackChocobo;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SidequestRaiseAChocobo.class, BlackChocobo.class, SazhsChocobo.class, Forest.class})
class SidequestRaiseAChocoboTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a Bird token whose landfall boost lasts until cleanup")
    void entersWithBirdTokenAndLandfallBoost() {
        addSidequest();
        Permanent bird = birdToken();
        harness.setHand(player1, List.of(new Forest()));

        int powerBefore = gqs.getEffectivePower(gd, bird);
        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(powerBefore + 1);
    }

    @Test
    @DisplayName("Does not transform with fewer than four Birds")
    void doesNotTransformWithFewerThanFourBirds() {
        Permanent source = addSidequest();
        harness.addToBattlefield(player1, new SazhsChocobo());
        harness.addToBattlefield(player1, new SazhsChocobo());

        advanceToPrecombatMain();
        resolveAllTriggers();

        assertThat(source.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Transforms with four Birds, searches for a tapped land, and boosts Birds on landfall")
    void transformsAndResolvesBlackChocoboAbilities() {
        Permanent source = addSidequest();
        Permanent firstBird = birdToken();
        Permanent secondBird = harness.addToBattlefieldAndReturn(player1, new SazhsChocobo());
        Permanent thirdBird = harness.addToBattlefieldAndReturn(player1, new SazhsChocobo());
        harness.addToBattlefield(player1, new SazhsChocobo());

        Forest searchedForest = new Forest();
        harness.setLibrary(player1, List.of(searchedForest));
        advanceToPrecombatMain();
        resolveAllTriggers();

        assertThat(source.isTransformed()).isTrue();
        assertThat(source.getCard()).isInstanceOf(BlackChocobo.class);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(searchedForest);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();
        Permanent searchedForestPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == searchedForest)
                .findFirst()
                .orElseThrow();
        assertThat(searchedForestPermanent.isTapped()).isTrue();

        harness.setHand(player1, List.of(new Forest()));
        int firstBirdPower = gqs.getEffectivePower(gd, firstBird);
        int secondBirdPower = gqs.getEffectivePower(gd, secondBird);
        int thirdBirdPower = gqs.getEffectivePower(gd, thirdBird);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, firstBird)).isEqualTo(firstBirdPower + 2);
        assertThat(gqs.getEffectivePower(gd, secondBird)).isEqualTo(secondBirdPower + 2);
        assertThat(gqs.getEffectivePower(gd, thirdBird)).isEqualTo(thirdBirdPower + 2);
    }

    @Test
    @DisplayName("Opponent Birds do not count toward the transformation condition")
    void opponentBirdsDoNotCount() {
        Permanent source = addSidequest();
        harness.addToBattlefield(player1, new SazhsChocobo());
        harness.addToBattlefield(player1, new SazhsChocobo());
        harness.addToBattlefield(player2, new SazhsChocobo());

        advanceToPrecombatMain();

        assertThat(gd.stack).isEmpty();
        assertThat(source.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Transformation condition is checked again when the ability resolves")
    void losingFourthBirdPreventsTransformation() {
        Permanent source = addSidequest();
        harness.addToBattlefield(player1, new SazhsChocobo());
        harness.addToBattlefield(player1, new SazhsChocobo());
        Permanent fourthBird = harness.addToBattlefieldAndReturn(player1, new SazhsChocobo());

        advanceToPrecombatMain();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(fourthBird);
        gd.playerGraveyards.get(player1.getId()).add(fourthBird.getCard());
        resolveAllTriggers();

        assertThat(source.isTransformed()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Only the controller's first main phase can trigger transformation")
    void opponentFirstMainPhaseDoesNotTransform() {
        Permanent source = addSidequest();
        harness.addToBattlefield(player1, new SazhsChocobo());
        harness.addToBattlefield(player1, new SazhsChocobo());
        harness.addToBattlefield(player1, new SazhsChocobo());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(source.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Opponent landfall does not boost the Bird token")
    void opponentLandDoesNotBoostToken() {
        addSidequest();
        Permanent bird = birdToken();
        int powerBefore = gqs.getEffectivePower(gd, bird);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Forest()));

        harness.playLand(player2, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(powerBefore);
    }

    @Test
    @DisplayName("Black Chocobo boosts itself and Birds present at resolution, then the boost expires")
    void backFaceBoostIncludesSelfAndBirdEnteringBeforeResolution() {
        Permanent source = addSidequest();
        Permanent token = birdToken();
        harness.addToBattlefield(player1, new SazhsChocobo());
        harness.addToBattlefield(player1, new SazhsChocobo());
        harness.addToBattlefield(player1, new SazhsChocobo());
        harness.setLibrary(player1, List.of());
        advanceToPrecombatMain();
        resolveAllTriggers();
        assertThat(source.isTransformed()).isTrue();
        Permanent opponentBird = harness.addToBattlefieldAndReturn(player2, new SazhsChocobo());
        int sourcePower = gqs.getEffectivePower(gd, source);
        int tokenPower = gqs.getEffectivePower(gd, token);
        int opponentPower = gqs.getEffectivePower(gd, opponentBird);
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        Permanent lateBird = harness.addToBattlefieldAndReturn(player1, new SazhsChocobo());
        int lateBirdPower = gqs.getEffectivePower(gd, lateBird);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(sourcePower + 1);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(tokenPower + 2);
        assertThat(gqs.getEffectivePower(gd, lateBird)).isEqualTo(lateBirdPower + 1);
        assertThat(gqs.getEffectivePower(gd, opponentBird)).isEqualTo(opponentPower);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(sourcePower);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(tokenPower);
        assertThat(gqs.getEffectivePower(gd, lateBird)).isEqualTo(lateBirdPower);
    }

    private Permanent addSidequest() {
        harness.setHand(player1, List.of(new SidequestRaiseAChocobo()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        return findPermanent(player1, "Sidequest: Raise a Chocobo");
    }

    private void advanceToPrecombatMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent birdToken() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }
}
