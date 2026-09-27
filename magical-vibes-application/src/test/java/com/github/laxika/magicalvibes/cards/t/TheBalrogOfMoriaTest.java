package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheBalrogOfMoria.class, GrizzlyBears.class, Murder.class})
class TheBalrogOfMoriaTest extends BaseCardTest {

    @Test
    void cyclingCreatesTwoTreasureTokensAndDraws() {
        harness.setHand(player1, List.of(new TheBalrogOfMoria()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
        harness.assertInGraveyard(player1, "The Balrog of Moria");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void deathTriggerMayExileBalrogAndOneCreaturePerOpponent() {
        Permanent balrog = harness.addToBattlefieldAndReturn(player1, new TheBalrogOfMoria());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        destroyBalrog(balrog);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(balrog.getCard().getId()));
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void decliningDeathTriggerLeavesBalrogInGraveyard() {
        Permanent balrog = harness.addToBattlefieldAndReturn(player1, new TheBalrogOfMoria());
        harness.addToBattlefield(player2, new GrizzlyBears());
        destroyBalrog(balrog);

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "The Balrog of Moria");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void destroyBalrog(Permanent balrog) {
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, balrog.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
