package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PoisonersApprentice.class, HillGiant.class})
class PoisonersApprenticeTest extends BaseCardTest {

    @Test
    @DisplayName("With life gained, the ETB gives an opponent's creature -4/-4 (killing a 3/3)")
    void withLifeGainWeakensOpponentCreature() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new PoisonersApprentice()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.getGameData().lifeGainedThisTurn.put(player1.getId(), 1);

        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature -> ETB on stack
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities(); // resolve ETB

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Without life gained, the ETB does nothing and the creature survives")
    void withoutLifeGainDoesNothing() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new PoisonersApprentice()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Life gained after entry still enables the ETB effect")
    void lifeGainedAfterEntryEnablesEffect() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new PoisonersApprentice()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        gd.lifeGainedThisTurn.put(player1.getId(), 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Cannot target your own creature")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new PoisonersApprentice()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID ownGiant = harness.getPermanentId(player1, "Hill Giant");
        UUID opposingGiant = harness.getPermanentId(player2, "Hill Giant");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).containsExactly(opposingGiant);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownGiant))
                .isInstanceOf(IllegalStateException.class);
    }
}
