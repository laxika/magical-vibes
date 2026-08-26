package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BlackChocobo;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
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
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(source.isTransformed()).isTrue();
        assertThat(source.getCard()).isInstanceOf(BlackChocobo.class);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(searchedForest);
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        Permanent searchedForestPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == searchedForest)
                .findFirst()
                .orElseThrow();
        assertThat(searchedForestPermanent.isTapped()).isTrue();

        harness.setHand(player1, List.of(new Forest()));
        int firstBirdPower = gqs.getEffectivePower(gd, firstBird);
        int secondBirdPower = gqs.getEffectivePower(gd, secondBird);
        int thirdBirdPower = gqs.getEffectivePower(gd, thirdBird);
        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, firstBird)).isEqualTo(firstBirdPower + 2);
        assertThat(gqs.getEffectivePower(gd, secondBird)).isEqualTo(secondBirdPower + 2);
        assertThat(gqs.getEffectivePower(gd, thirdBird)).isEqualTo(thirdBirdPower + 2);
    }

    private Permanent addSidequest() {
        harness.setHand(player1, List.of(new SidequestRaiseAChocobo()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SidequestRaiseAChocobo)
                .findFirst()
                .orElseThrow();
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
