package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GalacticWayfarer.class, Forest.class})
class GalacticWayfarerTest extends BaseCardTest {

    @Test
    void createsALanderWhenItEnters() {
        harness.setHand(player1, List.of(new GalacticWayfarer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Lander")).hasSize(1);
        assertThat(findPermanents(player2, "Lander")).isEmpty();
    }

    @Test
    void landerSacrificesImmediatelyAndFindsOneBasicLandTapped() {
        Permanent lander = createLander();
        Forest forest = new Forest();
        GalacticWayfarer nonland = new GalacticWayfarer();
        harness.setLibrary(player1, List.of(nonland, forest));
        Forest opponentsForest = new Forest();
        harness.setLibrary(player2, List.of(opponentsForest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, landerIndex(lander), null, null);

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player1, "Forest")).isEmpty();
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(forest);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsForest);
        assertThat(findPermanents(player2, "Forest")).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    void landerCanFailToFindEvenWithABasicLandAvailable() {
        Permanent lander = createLander();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, landerIndex(lander), null, null);
        resolveAllTriggers();
        harness.handleCardChosen(player1, -1);

        assertThat(findPermanents(player1, "Forest")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    void landerResolvesWhenLibraryHasNoBasicLands() {
        Permanent lander = createLander();
        GalacticWayfarer nonland = new GalacticWayfarer();
        harness.setLibrary(player1, List.of(nonland));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, landerIndex(lander), null, null);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    void tappedLanderCannotBeActivated() {
        Permanent lander = createLander();
        lander.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, landerIndex(lander), null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player1, "Lander")).containsExactly(lander);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void landerCannotBeActivatedWithoutTwoMana() {
        Permanent lander = createLander();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, landerIndex(lander), null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player1, "Lander")).containsExactly(lander);
        assertThat(lander.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent createLander() {
        harness.enterBattlefieldAndReturn(player1, new GalacticWayfarer());
        resolveAllTriggers();
        return findPermanent(player1, "Lander");
    }

    private int landerIndex(Permanent lander) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(lander);
    }
}
