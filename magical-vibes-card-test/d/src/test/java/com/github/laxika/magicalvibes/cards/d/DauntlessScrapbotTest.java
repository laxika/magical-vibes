package com.github.laxika.magicalvibes.cards.d;

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

@CardUsed({DauntlessScrapbot.class, Forest.class})
class DauntlessScrapbotTest extends BaseCardTest {

    @Test
    void exilesEachOpponentsGraveyardAndCreatesLanderOnEnter() {
        harness.setGraveyard(player1, List.of(new DauntlessScrapbot()));
        harness.setGraveyard(player2, List.of(new DauntlessScrapbot(), new DauntlessScrapbot()));

        harness.enterBattlefieldAndReturn(player1, new DauntlessScrapbot());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
        assertThat(findPermanents(player1, "Lander")).hasSize(1);
    }

    @Test
    void createsLanderEvenWhenOpponentsGraveyardIsEmpty() {
        harness.setGraveyard(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new DauntlessScrapbot());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Lander")).hasSize(1);
        assertThat(findPermanents(player2, "Lander")).isEmpty();
    }

    @Test
    void newlyCreatedLanderPaysTwoManaAndSacrificesToFindATappedBasicLand() {
        Forest forest = new Forest();
        DauntlessScrapbot nonland = new DauntlessScrapbot();
        harness.setLibrary(player1, List.of(nonland, forest));
        Permanent lander = createLander();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lander), null, null);

        assertThat(lander.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland, forest);

        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        PendingInteraction.LibrarySearch search = (PendingInteraction.LibrarySearch) gd.interaction.activeInteraction();
        assertThat(search.params().cards()).containsExactly(forest);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").getCard()).isSameAs(forest);
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void landerSearchMayFailToFindEvenWithABasicLandAvailable() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        Permanent lander = createLander();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lander), null, null);
        resolveAllTriggers();
        harness.handleCardChosen(player1, -1);

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(findPermanents(player1, "Forest")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void landerSearchWithNoBasicLandsStillConsumesTheToken() {
        DauntlessScrapbot nonland = new DauntlessScrapbot();
        harness.setLibrary(player1, List.of(nonland));
        Permanent lander = createLander();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lander), null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void landerCannotActivateWithoutTwoMana() {
        Permanent lander = createLander();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(lander), null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player1, "Lander")).containsExactly(lander);
        assertThat(lander.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void tappedLanderCannotActivate() {
        Permanent lander = createLander();
        lander.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(lander), null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player1, "Lander")).containsExactly(lander);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    private Permanent createLander() {
        harness.enterBattlefieldAndReturn(player1, new DauntlessScrapbot());
        resolveAllTriggers();
        return findPermanent(player1, "Lander");
    }
}
