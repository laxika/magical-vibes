package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SurvivalCacheTest extends BaseCardTest {

    @Test
    void gainsLifeAndDrawsWhenAheadAfterGainingLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SurvivalCache()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void doesNotDrawWhenLifeIsNotGreaterAfterGainingLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 22);
        harness.setHand(player1, List.of(new SurvivalCache()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

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
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Survival Cache");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
