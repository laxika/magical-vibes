package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MerfolkOfThePearlTrident;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmperorMihailII.class, MerfolkOfThePearlTrident.class, GrizzlyBears.class})
class EmperorMihailIITest extends BaseCardTest {

    @Test
    @DisplayName("Casts a Merfolk spell from the top of the library")
    void castsMerfolkFromLibraryTop() {
        harness.addToBattlefield(player1, new EmperorMihailII());
        Card merfolk = new MerfolkOfThePearlTrident();
        harness.setLibrary(player1, List.of(merfolk));
        harness.addMana(player1, ManaColor.BLUE, 1);
        prepareMainPhase();

        harness.castFromLibraryTop(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Merfolk of the Pearl Trident");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(merfolk);
    }

    @Test
    @DisplayName("Paying {1} after casting a Merfolk spell creates a blue Merfolk token")
    void payingCreatesMerfolkToken() {
        harness.addToBattlefield(player1, new EmperorMihailII());
        Card merfolk = new MerfolkOfThePearlTrident();
        harness.setLibrary(player1, List.of(merfolk));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase();

        harness.castFromLibraryTop(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Merfolk");
        assertThat(token.getCard().getColors()).containsExactly(CardColor.BLUE);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not cast a non-Merfolk card from the top of the library")
    void rejectsNonMerfolkFromLibraryTop() {
        harness.addToBattlefield(player1, new EmperorMihailII());
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        prepareMainPhase();

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(bears);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
