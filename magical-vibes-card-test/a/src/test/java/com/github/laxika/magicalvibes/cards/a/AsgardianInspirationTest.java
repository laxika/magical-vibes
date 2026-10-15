package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AsgardianInspiration.class, Shock.class})
class AsgardianInspirationTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top card with play permission until end of turn")
    void exilesTopCardWithPlayPermission() {
        Shock topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new AsgardianInspiration()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
    }

    @Test
    @DisplayName("May pay {2} to return it after your source deals noncombat damage to an opponent")
    void mayPayToReturnAfterControlledSourceDealsNoncombatDamage() {
        AsgardianInspiration inspiration = new AsgardianInspiration();
        harness.setGraveyard(player1, List.of(inspiration));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Asgardian Inspiration");
    }

    @Test
    @DisplayName("An opponent's noncombat damage does not trigger it")
    void opponentSourceDoesNotTriggerIt() {
        AsgardianInspiration inspiration = new AsgardianInspiration();
        harness.setGraveyard(player1, List.of(inspiration));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.pendingMayAbilities).isEmpty();
        harness.assertInGraveyard(player1, "Asgardian Inspiration");
    }

    @Test
    @DisplayName("The exiled spell can be cast by paying its mana cost")
    void castsExiledSpellWithMana() {
        Shock topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new AsgardianInspiration()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castFromExile(player1, topCard.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Declining the payment leaves the card in the graveyard")
    void decliningPaymentLeavesCardInGraveyard() {
        harness.setGraveyard(player1, List.of(new AsgardianInspiration()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Asgardian Inspiration");
        harness.assertNotInHand(player1, "Asgardian Inspiration");
    }

    @Test
    @DisplayName("Damage to yourself does not trigger the graveyard ability")
    void selfDamageDoesNotTriggerIt() {
        harness.setGraveyard(player1, List.of(new AsgardianInspiration()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player1, "Asgardian Inspiration");
    }

    @Test
    @DisplayName("An empty library leaves nothing to exile and the spell resolves")
    void emptyLibraryStillResolves() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new AsgardianInspiration()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Asgardian Inspiration");
    }

    @Test
    @DisplayName("The exile permission does not waive the spell's mana cost")
    void cannotCastExiledSpellWithoutMana() {
        Shock topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new AsgardianInspiration()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Accepting with only one mana cannot pay the two-mana return cost")
    void insufficientManaDoesNotReturnCard() {
        harness.setGraveyard(player1, List.of(new AsgardianInspiration()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Asgardian Inspiration");
        harness.assertNotInHand(player1, "Asgardian Inspiration");
    }
}
