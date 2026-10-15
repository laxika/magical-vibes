package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.OgreSentry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SurvivalCache.class, OgreSentry.class, Forest.class})
class SurvivalCacheTest extends BaseCardTest {

    @Test
    void gainsLifeAndDrawsWhenAheadAfterGainingLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SurvivalCache()));
        harness.setLibrary(player1, List.of(new OgreSentry()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 22);
        harness.assertInHand(player1, "Ogre Sentry");
    }

    @Test
    void doesNotDrawWhenLifeIsNotGreaterAfterGainingLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 22);
        harness.setHand(player1, List.of(new SurvivalCache()));
        harness.setLibrary(player1, List.of(new OgreSentry()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 22);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void reboundOffersASecondFreeCastAtNextUpkeep() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        SurvivalCache card = new SurvivalCache();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(new OgreSentry(), new Forest()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        harness.assertInHand(player1, "Ogre Sentry");
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Survival Cache");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void reboundRechecksLifeTotalsAndCanDrawAfterTheFirstCastDidNot() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 22);
        harness.setHand(player1, List.of(new SurvivalCache()));
        harness.setLibrary(player1, List.of(new OgreSentry()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 22);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        harness.assertInHand(player1, "Ogre Sentry");
        harness.assertInGraveyard(player1, "Survival Cache");
    }

    @Test
    void reboundDoesNotDrawWhenLifeTotalsBecomeEqualOnTheSecondCast() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SurvivalCache()));
        harness.setLibrary(player1, List.of(new OgreSentry(), new Forest()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.setLife(player2, 24);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        harness.assertInHand(player1, "Ogre Sentry");
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Survival Cache");
    }

    @Test
    void decliningReboundLeavesTheCardExiledWithoutAnotherOffer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 22);
        SurvivalCache card = new SurvivalCache();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(new OgreSentry()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 22);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Survival Cache");

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
