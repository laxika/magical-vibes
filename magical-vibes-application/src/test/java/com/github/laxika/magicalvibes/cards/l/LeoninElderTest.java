package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GlazeFiend;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LeoninElderTest extends BaseCardTest {

    @Test
    @DisplayName("May gain 1 life when an artifact enters under its controller's control")
    void gainsLifeWhenOwnArtifactEnters() {
        harness.addToBattlefield(player1, new LeoninElder());
        harness.setHand(player1, List.of(new GlazeFiend()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("May decline life gain when an artifact enters")
    void mayDeclineLifeGain() {
        harness.addToBattlefield(player1, new LeoninElder());
        harness.setHand(player1, List.of(new GlazeFiend()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Triggers for an artifact entering under an opponent's control")
    void gainsLifeWhenOpponentArtifactEnters() {
        harness.addToBattlefield(player1, new LeoninElder());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GlazeFiend()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Does not trigger for a non-artifact permanent entering")
    void doesNotTriggerForNonArtifact() {
        harness.addToBattlefield(player1, new LeoninElder());
        harness.setHand(player1, List.of(new LeoninElder()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }
}
