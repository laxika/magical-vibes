package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OscorpIndustries.class})
class OscorpIndustriesTest extends BaseCardTest {

    @Test
    @DisplayName("Oscorp Industries enters tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new OscorpIndustries()));

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(permanent -> assertThat(permanent.isTapped()).isTrue());
    }

    @Test
    @DisplayName("The mana ability offers blue, black, and red")
    void manaAbilityOffersThreeColors() {
        harness.addToBattlefield(player1, new OscorpIndustries());

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("BLUE", "BLACK", "RED");
    }

    @Test
    @DisplayName("Choosing a mana color adds one mana and taps the land")
    void choosingManaColorAddsManaAndTapsSource() {
        var land = harness.addToBattlefieldAndReturn(player1, new OscorpIndustries());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mayhem plays Oscorp Industries from the graveyard and causes its life loss")
    void mayhemPlaysFromGraveyard() {
        OscorpIndustries card = new OscorpIndustries();
        harness.setGraveyard(player1, List.of(card));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(card.getId())));
        prepareMainPhase();

        harness.playLandFromGraveyard(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(permanent -> assertThat(permanent.isTapped()).isTrue());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Mayhem cannot play Oscorp Industries before it was discarded")
    void mayhemRequiresDiscardThisTurn() {
        harness.setGraveyard(player1, List.of(new OscorpIndustries()));
        prepareMainPhase();

        assertThatThrownBy(() -> harness.playLandFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Entering from hand does not cause life loss")
    void enteringFromHandDoesNotLoseLife() {
        harness.setHand(player1, List.of(new OscorpIndustries()));
        prepareMainPhase();

        harness.playLand(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Oscorp Industries");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"BLUE", "BLACK"})
    @DisplayName("Each remaining mana choice produces exactly one mana without using the stack")
    void producesChosenManaImmediately(ManaColor color) {
        var land = harness.addToBattlefieldAndReturn(player1, new OscorpIndustries());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        for (ManaColor poolColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(poolColor))
                    .isEqualTo(poolColor == color ? 1 : 0);
        }
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = TurnStep.class, names = {"UPKEEP", "BEGINNING_OF_COMBAT", "END_STEP"})
    @DisplayName("Mayhem does not allow a land play outside a main phase")
    void mayhemRequiresMainPhase(TurnStep step) {
        OscorpIndustries card = new OscorpIndustries();
        harness.setGraveyard(player1, List.of(card));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(card.getId())));
        prepareMainPhase();
        harness.forceStep(step);

        assertThatThrownBy(() -> harness.playLandFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Mayhem does not allow playing the land during an opponent's turn")
    void mayhemRequiresOwnTurn() {
        OscorpIndustries card = new OscorpIndustries();
        harness.setGraveyard(player1, List.of(card));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(card.getId())));
        prepareMainPhase();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.playLandFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
    }

    @Test
    @DisplayName("A Mayhem land play uses the normal land allowance")
    void mayhemUsesLandPlayAllowance() {
        OscorpIndustries first = new OscorpIndustries();
        OscorpIndustries second = new OscorpIndustries();
        harness.setGraveyard(player1, List.of(first, second));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(),
                new HashSet<>(Set.of(first.getId(), second.getId())));
        prepareMainPhase();

        harness.playLandFromGraveyard(player1, 0);
        resolveAllTriggers();
        prepareMainPhase();

        assertThatThrownBy(() -> harness.playLandFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Discarding a different copy does not grant Mayhem to this card")
    void mayhemRequiresThisSpecificCardToBeDiscarded() {
        OscorpIndustries discarded = new OscorpIndustries();
        OscorpIndustries other = new OscorpIndustries();
        harness.setGraveyard(player1, List.of(other, discarded));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(discarded.getId())));
        prepareMainPhase();

        assertThatThrownBy(() -> harness.playLandFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other, discarded);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
