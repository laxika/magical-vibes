package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AsgardianInspiration.class, GrizzlyBears.class, Shock.class})
class AsgardianInspirationTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top card with play permission until end of turn")
    void exilesTopCardWithPlayPermission() {
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new AsgardianInspiration()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

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

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
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

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
        harness.assertInGraveyard(player1, "Asgardian Inspiration");
    }
}
