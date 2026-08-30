package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnowledgeVault.class, GrizzlyBears.class})
class KnowledgeVaultTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top card of its controller's library face down and tracks it")
    void exilesTopCardFaceDownWithSource() {
        Permanent vault = harness.addToBattlefieldAndReturn(player1, new KnowledgeVault());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId()))
                .satisfies(exiled -> {
                    assertThat(exiled.faceDown()).isTrue();
                    assertThat(exiled.sourcePermanentId()).isEqualTo(vault.getId());
                });
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing discards the hand and returns all tracked cards to their owners")
    void sacrificeDiscardsHandAndReturnsExiledCards() {
        Permanent vault = harness.addToBattlefieldAndReturn(player1, new KnowledgeVault());
        Card firstExiledCard = new GrizzlyBears();
        Card secondExiledCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstExiledCard, secondExiledCard));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        vault.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Card discardedCard = new GrizzlyBears();
        harness.setHand(player1, List.of(discardedCard));
        vault.untap();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(firstExiledCard, secondExiledCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard);
        assertThat(gd.getCardsExiledByPermanent(vault.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Knowledge Vault");
    }

    @Test
    @DisplayName("Cards exiled with it go to their owners' graveyards when it leaves")
    void exiledCardsGoToGraveyardWhenVaultLeaves() {
        Permanent vault = harness.addToBattlefieldAndReturn(player1, new KnowledgeVault());
        Card exiledCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(exiledCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, vault));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        resolveAllTriggers();

        assertThat(gd.getCardsExiledByPermanent(vault.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(exiledCard);
    }
}
