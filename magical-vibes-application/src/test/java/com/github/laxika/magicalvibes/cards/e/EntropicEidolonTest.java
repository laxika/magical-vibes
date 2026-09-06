package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EntropicEidolon.class, AdelizTheCinderWind.class, GrizzlyBears.class})
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
        harness.setHand(player1, List.of(new AdelizTheCinderWind()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
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
        harness.setHand(player1, List.of(new AdelizTheCinderWind()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(eidolon);
    }

    @Test
    @DisplayName("A monocolored spell does not trigger Entropic Eidolon's graveyard ability")
    void monocoloredSpellDoesNotTriggerReturn() {
        EntropicEidolon eidolon = new EntropicEidolon();
        harness.setGraveyard(player1, List.of(eidolon));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(eidolon);
    }
}
