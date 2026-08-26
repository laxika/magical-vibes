package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlleyAssailant.class, Shock.class})
class AlleyAssailantTest extends BaseCardTest {

    @Test
    @DisplayName("Disguise gives the face-down creature ward {2}")
    void disguiseGivesFaceDownCreatureWard() {
        harness.setHand(player1, List.of(new AlleyAssailant()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent assailant = findPermanent(player1, "Alley Assailant");
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, assailant.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(assailant.isFaceDown()).isTrue();
    }

    @Test
    @DisplayName("Enters tapped when cast face up")
    void entersTappedWhenCastFaceUp() {
        harness.setHand(player1, List.of(new AlleyAssailant()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent assailant = findPermanent(player1, "Alley Assailant");
        assertThat(assailant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Turning face up makes an opponent lose 3 life and its controller gain 3 life")
    void turningFaceUpDrainsTargetOpponent() {
        harness.setHand(player1, List.of(new AlleyAssailant()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent assailant = findPermanent(player1, "Alley Assailant");
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(assailant));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(assailant.isFaceDown()).isFalse();
        harness.assertLife(player1, 13);
        harness.assertLife(player2, 17);
    }
}
