package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FomoriVault.class, GrizzlyBears.class, Ornithopter.class})
class FomoriVaultTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Fomori Vault produces colorless mana")
    void producesColorlessMana() {
        Permanent vault = addVault(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(vault.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The activated ability discards a card and selects from as many top cards as artifacts controlled")
    void discardsAndSelectsBasedOnArtifactsControlled() {
        Permanent vault = addVault(player1);
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());

        Card discarded = new GrizzlyBears();
        Card chosen = new GrizzlyBears();
        Card bottomCard = new Ornithopter();
        Card unlookedCard = new Ornithopter();
        Card secondUnlookedCard = new Ornithopter();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(chosen, bottomCard, unlookedCard, secondUnlookedCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unlookedCard, secondUnlookedCard, bottomCard);
        assertThat(vault.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The activated ability cannot be paid without a card to discard")
    void requiresDiscardingACard() {
        addVault(player1);
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addVault(Player player) {
        return harness.addToBattlefieldAndReturn(player, new FomoriVault());
    }
}
