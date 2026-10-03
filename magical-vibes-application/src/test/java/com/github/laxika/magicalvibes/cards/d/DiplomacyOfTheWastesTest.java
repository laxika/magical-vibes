package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EnhancedAwareness;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FrontierMastodon;
import com.github.laxika.magicalvibes.cards.m.MarduWoeReaper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiplomacyOfTheWastes.class, EnhancedAwareness.class, Forest.class,
        FrontierMastodon.class, MarduWoeReaper.class})
class DiplomacyOfTheWastesTest extends BaseCardTest {

    @Test
    void choosesAndDiscardsOnlyNonlandCards() {
        harness.setHand(player2, List.of(new EnhancedAwareness(), new Forest(), new FrontierMastodon()));
        castDiplomacy();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0, 2);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Enhanced Awareness");
        harness.assertInHand(player2, "Forest");
        harness.assertInHand(player2, "Frontier Mastodon");
        harness.assertLife(player2, 20);
    }

    @Test
    void warriorAddsLifeLoss() {
        harness.addToBattlefield(player1, new MarduWoeReaper());
        harness.setHand(player2, List.of(new EnhancedAwareness()));
        castDiplomacy();

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Enhanced Awareness");
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    void emptyHandStillLosesLifeWhenControllerHasWarrior() {
        harness.addToBattlefield(player1, new MarduWoeReaper());
        harness.setHand(player2, List.of());

        castDiplomacy();

        harness.assertLife(player2, 18);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Diplomacy of the Wastes");
    }

    @Test
    void landOnlyHandStillLosesLifeWithoutDiscardingLand() {
        harness.addToBattlefield(player1, new MarduWoeReaper());
        harness.setHand(player2, List.of(new Forest()));

        castDiplomacy();

        harness.assertInHand(player2, "Forest");
        harness.assertNotInGraveyard(player2, "Forest");
        harness.assertLife(player2, 18);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentsWarriorDoesNotCauseLifeLoss() {
        harness.addToBattlefield(player2, new MarduWoeReaper());
        harness.addToBattlefield(player1, new FrontierMastodon());
        harness.setHand(player2, List.of(new EnhancedAwareness()));

        castDiplomacy();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Enhanced Awareness");
        harness.assertLife(player2, 20);
    }

    @Test
    void multipleWarriorsStillCauseOnlyTwoLifeLoss() {
        harness.addToBattlefield(player1, new MarduWoeReaper());
        harness.addToBattlefield(player1, new MarduWoeReaper());
        harness.setHand(player2, List.of(new EnhancedAwareness()));

        castDiplomacy();
        harness.handleCardChosen(player1, 0);

        harness.assertLife(player2, 18);
    }

    @Test
    void casterCanChooseCreatureButCannotChooseLand() {
        harness.setHand(player2, List.of(new Forest(), new FrontierMastodon()));
        castDiplomacy();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player2, "Frontier Mastodon");
        harness.assertInHand(player2, "Forest");
        harness.assertLife(player2, 20);
    }

    @Test
    void targetsOnlyOpponents() {
        harness.setHand(player1, List.of(new DiplomacyOfTheWastes()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castDiplomacy() {
        harness.setHand(player1, List.of(new DiplomacyOfTheWastes()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }
}
