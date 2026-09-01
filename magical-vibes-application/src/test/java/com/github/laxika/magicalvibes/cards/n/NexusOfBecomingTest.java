package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NexusOfBecoming.class, GrizzlyBears.class, Spellbook.class, Forest.class})
class NexusOfBecomingTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card, then creates a 3/3 Golem artifact creature copy of a chosen creature")
    void drawsAndCopiesChosenCreature() {
        harness.addToBattlefield(player1, new NexusOfBecoming());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));

        resolveBeginningOfCombatTrigger();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileCardFromHandAndCreateTokenCopyChoice.class);

        harness.handleCardChosen(player1, 0);

        Permanent token = findPermanent(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card instanceof GrizzlyBears);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.BEAR, CardSubtype.GOLEM);
        assertThat(gqs.isArtifact(gd, token)).isTrue();
        assertThat(gqs.isCreature(gd, token)).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
    }

    @Test
    @DisplayName("Only artifact and creature cards can be exiled for the copy")
    void filtersHandChoice() {
        harness.addToBattlefield(player1, new NexusOfBecoming());
        harness.setHand(player1, List.of(new Forest(), new Spellbook()));
        harness.setLibrary(player1, List.of(new Forest()));

        resolveBeginningOfCombatTrigger();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileCardFromHandAndCreateTokenCopyChoice.class);
        harness.handleCardChosen(player1, 1);

        Permanent token = findPermanent(player1, "Spellbook");
        assertThat(gqs.isArtifact(gd, token)).isTrue();
        assertThat(gqs.isCreature(gd, token)).isTrue();
    }

    @Test
    @DisplayName("Declining the may ability does not exile a card or create a token")
    void decliningMayAbilityDoesNothing() {
        harness.addToBattlefield(player1, new NexusOfBecoming());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));

        resolveBeginningOfCombatTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
    }

    private void resolveBeginningOfCombatTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
