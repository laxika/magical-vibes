package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.e.EnvironmentalSciences;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OvergrownArch.class, EnvironmentalSciences.class, Forest.class})
class OvergrownArchTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability gains 1 life")
    void tapAbilityGainsLife() {
        Permanent arch = addReadyArch();
        harness.setLife(player1, 10);
        prepareActivation();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 11);
        assertThat(arch.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrifice ability learns by searching for a Lesson")
    void sacrificeAbilitySearchesForLesson() {
        Card lesson = new EnvironmentalSciences();
        Card nonLesson = new Forest();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson, nonLesson)));
        addReadyArch();
        prepareActivation();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(lesson);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(nonLesson);
        harness.assertInGraveyard(player1, "Overgrown Arch");
    }

    @Test
    @DisplayName("Sacrifice ability can discard and draw")
    void sacrificeAbilityDiscardsAndDraws() {
        Card discarded = new Forest();
        Card drawn = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        addReadyArch();
        prepareActivation();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        harness.assertInGraveyard(player1, "Overgrown Arch");
    }

    @Test
    void summoningSicknessPreventsTapAbility() {
        harness.addToBattlefield(player1, new OvergrownArch());
        prepareActivation();
        harness.setLife(player1, 10);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        harness.assertLife(player1, 10);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedSummoningSickArchCanBeSacrificedAndLearnCanDoNothing() {
        Permanent arch = harness.addToBattlefieldAndReturn(player1, new OvergrownArch());
        arch.setTapped(true);
        harness.setHand(player1, List.of());
        gd.playerSideboards.put(player1.getId(), new ArrayList<>());
        prepareActivation();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(arch);
        harness.assertInGraveyard(player1, "Overgrown Arch");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void learnCanDeclineBothDiscardAndLesson() {
        Card retained = new Forest();
        Card lesson = new EnvironmentalSciences();
        harness.setHand(player1, List.of(retained));
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        addReadyArch();
        prepareActivation();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void lifeGainResolvesAfterArchIsSacrificedInResponse() {
        addReadyArch();
        harness.setHand(player1, List.of());
        gd.playerSideboards.put(player1.getId(), new ArrayList<>());
        harness.setLife(player1, 10);
        prepareActivation();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.assertLife(player1, 10);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertInGraveyard(player1, "Overgrown Arch");
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        harness.assertLife(player1, 11);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyArch() {
        return addCreatureReady(player1, new OvergrownArch());
    }

    private void prepareActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
