package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GhostLitWarder.class, GhostLitRedeemer.class})
class GhostLitWarderTest extends BaseCardTest {

    @Test
    void battlefieldAbilityCountersSpellWhenControllerCannotPay() {
        Permanent warder = addCreatureReady(player1, new GhostLitWarder());

        harness.forceActivePlayer(player2);
        GhostLitRedeemer redeemer = new GhostLitRedeemer();
        harness.castFromHand(player2, redeemer, "{W}");
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, redeemer.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ghost-Lit Redeemer");
        assertThat(warder.isTapped()).isTrue();
    }

    @Test
    void battlefieldAbilityLetsSpellResolveWhenControllerPays() {
        Permanent warder = addCreatureReady(player1, new GhostLitWarder());

        harness.forceActivePlayer(player2);
        GhostLitRedeemer redeemer = new GhostLitRedeemer();
        harness.castFromHand(player2, redeemer, "{W}");
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, redeemer.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Ghost-Lit Redeemer");
        assertThat(warder.isTapped()).isTrue();
    }

    @Test
    void battlefieldAbilityCountersSpellWhenControllerDeclinesPayment() {
        Permanent warder = addCreatureReady(player1, new GhostLitWarder());

        harness.forceActivePlayer(player2);
        GhostLitRedeemer redeemer = new GhostLitRedeemer();
        harness.castFromHand(player2, redeemer, "{W}");
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, redeemer.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ghost-Lit Redeemer");
        assertThat(warder.isTapped()).isTrue();
    }

    @Test
    void channelCountersSpellWhenControllerCannotPay() {
        harness.setHand(player1, List.of(new GhostLitWarder()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.forceActivePlayer(player2);
        GhostLitRedeemer redeemer = new GhostLitRedeemer();
        harness.castFromHand(player2, redeemer, "{W}");

        harness.passPriority(player2);
        harness.activateHandAbility(player1, 0, redeemer.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ghost-Lit Warder");
        harness.assertInGraveyard(player2, "Ghost-Lit Redeemer");
    }

    @Test
    void channelLetsSpellResolveWhenControllerPays() {
        harness.setHand(player1, List.of(new GhostLitWarder()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.forceActivePlayer(player2);
        GhostLitRedeemer redeemer = new GhostLitRedeemer();
        harness.castFromHand(player2, redeemer, "{W}");
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.passPriority(player2);
        harness.activateHandAbility(player1, 0, redeemer.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ghost-Lit Warder");
        harness.assertOnBattlefield(player2, "Ghost-Lit Redeemer");
    }

    @Test
    void channelCountersSpellWhenControllerDeclinesPayment() {
        harness.setHand(player1, List.of(new GhostLitWarder()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.forceActivePlayer(player2);
        GhostLitRedeemer redeemer = new GhostLitRedeemer();
        harness.castFromHand(player2, redeemer, "{W}");
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.passPriority(player2);
        harness.activateHandAbility(player1, 0, redeemer.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ghost-Lit Warder");
        harness.assertInGraveyard(player2, "Ghost-Lit Redeemer");
    }
}
