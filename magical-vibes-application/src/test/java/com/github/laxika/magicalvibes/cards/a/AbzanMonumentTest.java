package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbzanMonument.class, Forest.class, GrizzlyBears.class, Island.class, Mountain.class, Plains.class, Swamp.class, WallOfAir.class})
class AbzanMonumentTest extends BaseCardTest {

    @Test
    @DisplayName("The enter-the-battlefield ability searches for a Plains, Swamp, or Forest")
    void searchesForAnAbzanBasicLand() {
        harness.setHand(player1, List.of(new AbzanMonument()));
        harness.setLibrary(player1, new java.util.ArrayList<>(List.of(
                new Plains(), new Swamp(), new Forest(), new Island(), new Mountain())));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Plains", "Swamp", "Forest");

        String chosenName = search.params().cards().getFirst().getName();
        harness.handleCardChosen(player1, 0);
        harness.assertInHand(player1, chosenName);
    }

    @Test
    @DisplayName("Sacrificing the monument creates one Spirit whose size is your greatest creature toughness")
    void sacrificeCreatesSpiritUsingControlledGreatestToughness() {
        harness.addToBattlefield(player1, new AbzanMonument());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new WallOfAir());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Abzan Monument");
        harness.assertNotOnBattlefield(player1, "Abzan Monument");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Spirit"))
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().getPower()).isEqualTo(2);
                    assertThat(token.getCard().getToughness()).isEqualTo(2);
                });
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
    }

    @Test
    void canFailToFindEvenWithAnEligibleLand() {
        harness.setHand(player1, List.of(new AbzanMonument()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        harness.handleCardChosen(player1, -1);

        harness.assertNotInHand(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Plains");
        assertThat(gameLogContains("shuffled")).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void searchWithNoEligibleLandStillShuffles() {
        harness.setHand(player1, List.of(new AbzanMonument()));
        harness.setLibrary(player1, List.of(new Island(), new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Island", "Mountain");
        assertThat(gameLogContains("shuffled")).isTrue();
    }

    @Test
    void tokenUsesEffectiveToughnessAtResolution() {
        prepareActivation();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);

        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.passBothPriorities();

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(5);
    }

    @Test
    void tokenUsesToughnessRatherThanPowerAndChoosesTheGreatestCreature() {
        prepareActivation();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new WallOfAir());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(5);
    }

    @Test
    void searchRevealsTheChosenLandAndShufflesTheRemainingLibrary() {
        harness.setHand(player1, List.of(new AbzanMonument()));
        harness.setLibrary(player1, List.of(new Swamp(), new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Swamp");
        harness.assertNotOnBattlefield(player1, "Swamp");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Island");
        assertThat(gameLogContains("reveals Swamp")).isTrue();
        assertThat(gameLogContains("shuffled")).isTrue();
    }

    @Test
    void zeroToughnessTokenDiesWhenNoCreaturesAreControlled() {
        prepareActivation();
        harness.addToBattlefield(player2, new WallOfAir());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spirit");
        assertThat(gameLogContains("0/0 White Spirit creature token enters the battlefield")).isTrue();
        harness.assertInGraveyard(player1, "Abzan Monument");
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        prepareActivation();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Abzan Monument");
    }

    @Test
    void cannotActivateOnOpponentsTurn() {
        prepareActivation();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Abzan Monument");
    }

    @Test
    void cannotActivateWithNonemptyStack() {
        prepareActivation();
        harness.setHand(player1, List.of(new AbzanMonument()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Abzan Monument");
    }

    @Test
    void cannotActivateWhileTapped() {
        prepareActivation();
        findPermanent(player1, "Abzan Monument").tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Abzan Monument");
    }

    private void prepareActivation() {
        harness.addToBattlefield(player1, new AbzanMonument());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
