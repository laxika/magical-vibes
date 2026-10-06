package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BristlingBackwoods;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilverDeputy.class, Forest.class, BristlingBackwoods.class, SterlingHound.class})
class SilverDeputyTest extends BaseCardTest {

    @Test
    void etbSearchesForBasicLandOrDesertAndPutsItOnTop() {
        Card nonMatch = new SterlingHound();
        Card forest = new Forest();
        Card desert = new BristlingBackwoods();
        setLibrary(nonMatch, forest, desert);

        castSilverDeputy();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactlyInAnyOrder(forest, desert);
        assertThat(search.params().cards()).doesNotContain(nonMatch);

        int desertIndex = search.params().cards().indexOf(desert);
        harness.handleCardChosen(player1, desertIndex);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(desert);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(desert, nonMatch, forest);
    }

    @Test
    void etbSearchMayBeDeclined() {
        Card forest = new Forest();
        setLibrary(forest);

        castSilverDeputy();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void activatedAbilityTapsAndBoostsControlledCreatureUntilEndOfTurn() {
        Permanent deputy = addCreatureReady(player1, new SilverDeputy());
        Permanent target = addCreatureReady(player1, new SterlingHound());

        harness.activateAbility(player1, indexOf(deputy), null, target.getId());
        harness.passBothPriorities();

        assertThat(deputy.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
    }

    @Test
    void activatedAbilityCannotTargetOpponentCreature() {
        Permanent deputy = addCreatureReady(player1, new SilverDeputy());
        Permanent target = addCreatureReady(player2, new SterlingHound());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(deputy), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activatedAbilityRequiresSorcerySpeed() {
        Permanent deputy = addCreatureReady(player1, new SilverDeputy());
        Permanent target = addCreatureReady(player1, new SterlingHound());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(deputy), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void etbCanChooseAndRevealBasicLand() {
        Card forest = new Forest();
        Card nonMatch = new SterlingHound();
        setLibrary(nonMatch, forest);

        castSilverDeputy();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, nonMatch);
        assertThat(gameLogContains("reveals Forest")).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void etbMayFailToFindEvenWhenMatchingLandExists() {
        Card forest = new Forest();
        Card desert = new BristlingBackwoods();
        setLibrary(forest, desert);

        castSilverDeputy();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, desert);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("shuffled")).isTrue();
    }

    @Test
    void etbSearchWithNoMatchingCardsCompletesAndKeepsLibraryCards() {
        Card nonMatch = new SterlingHound();
        setLibrary(nonMatch);

        castSilverDeputy();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonMatch);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("shuffled")).isTrue();
    }

    @Test
    void etbSearchWithEmptyLibraryCompletes() {
        setLibrary();

        castSilverDeputy();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void activatedAbilityCanTargetDeputyItself() {
        Permanent deputy = addCreatureReady(player1, new SilverDeputy());

        harness.activateAbility(player1, indexOf(deputy), null, deputy.getId());
        harness.passBothPriorities();

        assertThat(deputy.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, deputy)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, deputy)).isEqualTo(2);
    }

    @Test
    void activatedAbilityCannotBeUsedWhileSummoningSick() {
        Permanent deputy = harness.addToBattlefieldAndReturn(player1, new SilverDeputy());
        Permanent target = addCreatureReady(player1, new SterlingHound());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(deputy), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(deputy.isTapped()).isFalse();
    }

    @Test
    void activatedAbilityCannotBeUsedWhileTapped() {
        Permanent deputy = addCreatureReady(player1, new SilverDeputy());
        deputy.setTapped(true);
        Permanent target = addCreatureReady(player1, new SterlingHound());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(deputy), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activatedAbilityCannotBeUsedDuringOpponentsMainPhase() {
        Permanent deputy = addCreatureReady(player1, new SilverDeputy());
        Permanent target = addCreatureReady(player1, new SterlingHound());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(deputy), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activatedAbilityCannotBeUsedWithSpellOnStack() {
        Permanent deputy = addCreatureReady(player1, new SilverDeputy());
        Permanent target = addCreatureReady(player1, new SterlingHound());
        castSilverDeputy();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(deputy), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(deputy.isTapped()).isFalse();
    }

    @Test
    void activatedAbilityCannotTargetNoncreatureLand() {
        Permanent deputy = addCreatureReady(player1, new SilverDeputy());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(deputy), null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castSilverDeputy() {
        harness.setHand(player1, List.of(new SilverDeputy()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
