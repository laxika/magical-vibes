package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
