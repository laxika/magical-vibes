package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SandstormEidolon.class, AdelizTheCinderWind.class, GrizzlyBears.class})
class SandstormEidolonTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Sandstorm Eidolon makes a target creature unable to block this turn")
    void sacrificeAbilityPreventsBlocking() {
        addCreatureReady(player1, new SandstormEidolon());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sandstorm Eidolon");
        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Casting a multicolored spell may return Sandstorm Eidolon from the graveyard")
    void multicoloredSpellReturnsEidolonToHand() {
        SandstormEidolon eidolon = new SandstormEidolon();
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
    @DisplayName("Declining the multicolored spell trigger keeps Sandstorm Eidolon in the graveyard")
    void decliningReturnKeepsEidolonInGraveyard() {
        SandstormEidolon eidolon = new SandstormEidolon();
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
    @DisplayName("A monocolored spell does not trigger Sandstorm Eidolon's graveyard ability")
    void monocoloredSpellDoesNotTriggerReturn() {
        SandstormEidolon eidolon = new SandstormEidolon();
        harness.setGraveyard(player1, List.of(eidolon));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(eidolon);
    }
}
