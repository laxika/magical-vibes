package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PentarchPaladin.class, GrizzlyBears.class, AirElemental.class})
class PentarchPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("Pentarch Paladin asks for a color as it enters")
    void choosesColorAsItEnters() {
        harness.setHand(player1, List.of(new PentarchPaladin()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");

        assertThat(findPermanent(player1, "Pentarch Paladin").getChosenColor()).isEqualTo(CardColor.GREEN);
    }

    @Test
    @DisplayName("Pentarch Paladin destroys a permanent of the chosen color")
    void destroysPermanentOfChosenColor() {
        Permanent paladin = addReadyPaladin(CardColor.GREEN);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(paladin.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Pentarch Paladin cannot target a permanent of another color")
    void rejectsPermanentOfAnotherColor() {
        Permanent paladin = addReadyPaladin(CardColor.GREEN);
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, elemental.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(paladin.isTapped()).isFalse();
    }

    private Permanent addReadyPaladin(CardColor chosenColor) {
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new PentarchPaladin());
        paladin.setChosenColor(chosenColor);
        paladin.setSummoningSick(false);
        return paladin;
    }
}
