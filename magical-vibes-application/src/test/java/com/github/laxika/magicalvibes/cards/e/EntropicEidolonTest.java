package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AzoriusFirstWing;
import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.cards.g.GnatAlleyCreeper;
import com.github.laxika.magicalvibes.cards.w.WreckingBall;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EntropicEidolon.class, AzoriusFirstWing.class, GnatAlleyCreeper.class, AzoriusSignet.class,
        WreckingBall.class})
class EntropicEidolonTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Entropic Eidolon causes target player to lose 1 life and its controller to gain 1 life")
    void sacrificeAbilityDrainsTargetPlayer() {
        harness.addToBattlefield(player1, new EntropicEidolon());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player1, "Entropic Eidolon");
    }

    @Test
    @DisplayName("Casting a multicolored spell may return Entropic Eidolon from the graveyard")
    void multicoloredSpellReturnsEidolonToHand() {
        EntropicEidolon eidolon = new EntropicEidolon();
        harness.setGraveyard(player1, List.of(eidolon));
        harness.castFromHand(player1, new AzoriusFirstWing(), "{W}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(eidolon);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(eidolon);
    }

    @Test
    @DisplayName("Declining the multicolored spell trigger keeps Entropic Eidolon in the graveyard")
    void decliningReturnKeepsEidolonInGraveyard() {
        EntropicEidolon eidolon = new EntropicEidolon();
        harness.setGraveyard(player1, List.of(eidolon));
        harness.castFromHand(player1, new AzoriusFirstWing(), "{W}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(eidolon);
    }

    @Test
    @DisplayName("A monocolored spell does not trigger Entropic Eidolon's graveyard ability")
    void monocoloredSpellDoesNotTriggerReturn() {
        EntropicEidolon eidolon = new EntropicEidolon();
        harness.setGraveyard(player1, List.of(eidolon));
        harness.castFromHand(player1, new GnatAlleyCreeper(), "{2}{R}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(eidolon);
    }

    @Test
    @DisplayName("An opponent's multicolored instant does not trigger Entropic Eidolon's graveyard ability")
    void opponentMulticoloredSpellDoesNotTriggerReturn() {
        EntropicEidolon eidolon = new EntropicEidolon();
        harness.setGraveyard(player1, List.of(eidolon));
        var target = harness.addToBattlefieldAndReturn(player1, new AzoriusFirstWing());

        harness.castFromHand(player1, new GnatAlleyCreeper(), "{2}{R}");
        harness.setHand(player2, List.of(new WreckingBall()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(eidolon);
    }

    @Test
    @DisplayName("A colorless spell does not trigger Entropic Eidolon's graveyard ability")
    void colorlessSpellDoesNotTriggerReturn() {
        EntropicEidolon eidolon = new EntropicEidolon();
        harness.setGraveyard(player1, List.of(eidolon));

        harness.castFromHand(player1, new AzoriusSignet(), "{2}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(eidolon);
    }
}
