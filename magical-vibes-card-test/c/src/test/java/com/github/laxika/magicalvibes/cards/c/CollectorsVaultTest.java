package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CollectorsVault.class, Forest.class})
class CollectorsVaultTest extends BaseCardTest {

    @Test
    void drawsThenDiscardsBeforeCreatingTreasure() {
        harness.addToBattlefield(player1, new CollectorsVault());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(countPermanents(player1, "Treasure")).isZero();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void emptyStartingHandDiscardsTheDrawnCardAndCreatesTreasure() {
        harness.addToBattlefield(player1, new CollectorsVault());
        harness.setHand(player1, List.of());
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(countPermanents(player1, "Treasure")).isZero();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawnCard);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(countPermanents(player2, "Treasure")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void activationPaysTwoManaAndTapsBeforeResolving() {
        var vault = harness.addToBattlefieldAndReturn(player1, new CollectorsVault());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(vault.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void activatedAbilityResolvesAfterVaultLeavesBattlefield() {
        CollectorsVault vault = new CollectorsVault();
        harness.addToBattlefield(player1, vault);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.setGraveyard(player1, List.of(vault));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(countPermanents(player1, "Collector's Vault")).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
