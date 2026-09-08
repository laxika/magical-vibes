package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpectralSnatcher.class, Swamp.class, GrizzlyBears.class, GiantGrowth.class, Shock.class})
class SpectralSnatcherTest extends BaseCardTest {

    @Test
    @DisplayName("Swampcycling discards Spectral Snatcher and searches for a Swamp")
    void swampcyclingSearchesForSwamp() {
        harness.setHand(player1, List.of(new SpectralSnatcher()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Swamp()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spectral Snatcher");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(card -> card instanceof Swamp);

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Swamp");
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when they have no card to discard")
    void wardCountersSpellWithoutDiscard() {
        Permanent snatcher = addReadySnatcher(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, snatcher.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Giant Growth");
        assertThat(gqs.getEffectivePower(gd, snatcher)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, snatcher)).isEqualTo(5);
    }

    @Test
    @DisplayName("Discarding a card lets an opponent's targeted spell resolve through ward")
    void discardingCardPreventsWardCounter() {
        Permanent snatcher = addReadySnatcher(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GiantGrowth(), new Shock()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, snatcher.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gqs.getEffectivePower(gd, snatcher)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, snatcher)).isEqualTo(8);
    }

    private Permanent addReadySnatcher(Player player) {
        Permanent snatcher = new Permanent(new SpectralSnatcher());
        snatcher.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(snatcher);
        return snatcher;
    }
}
