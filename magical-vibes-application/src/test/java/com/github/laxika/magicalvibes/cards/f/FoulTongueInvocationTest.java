package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FoulTongueInvocation.class, GrizzlyBears.class, ShivanDragon.class})
class FoulTongueInvocationTest extends BaseCardTest {

    @Test
    @DisplayName("Target player sacrifices a creature")
    void sacrificesTargetPlayersCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FoulTongueInvocation()));
        harness.setLife(player1, 10);
        addMana();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("Revealing a Dragon gains 4 life")
    void revealingDragonGainsLife() {
        ShivanDragon dragon = new ShivanDragon();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FoulTongueInvocation(), dragon));
        harness.setLife(player1, 10);
        addMana();

        harness.castInstantWithDiscard(player1, 0, player2.getId(), 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 14);
        assertThat(gd.playerHands.get(player1.getId())).contains(dragon);
    }

    @Test
    @DisplayName("Controlling a Dragon as cast gains 4 life")
    void controllingDragonAsCastGainsLife() {
        harness.addToBattlefield(player1, new ShivanDragon());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FoulTongueInvocation()));
        harness.setLife(player1, 10);
        addMana();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 14);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
