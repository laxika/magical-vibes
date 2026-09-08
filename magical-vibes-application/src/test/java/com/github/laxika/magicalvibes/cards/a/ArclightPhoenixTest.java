package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArclightPhoenixTest extends BaseCardTest {

    @Test
    @DisplayName("Returns from the graveyard after casting three instant spells")
    void returnsAfterThreeInstantSpells() {
        ArclightPhoenix phoenix = new ArclightPhoenix();
        harness.setGraveyard(player1, List.of(phoenix));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));

        for (int i = 0; i < 3; i++) {
            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();
        }

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(phoenix.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(phoenix.getId()));
    }

    @Test
    @DisplayName("Does not return before three matching spells have been cast")
    void doesNotReturnBeforeThreshold() {
        ArclightPhoenix phoenix = new ArclightPhoenix();
        harness.setGraveyard(player1, List.of(phoenix));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player1, List.of(new Shock(), new Shock()));

        for (int i = 0; i < 2; i++) {
            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();
        }

        advanceToCombat(player1);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(phoenix.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(phoenix.getId()));
    }

    @Test
    @DisplayName("Counts only instant and sorcery spells")
    void countsOnlyInstantAndSorcerySpells() {
        ArclightPhoenix phoenix = new ArclightPhoenix();
        harness.setGraveyard(player1, List.of(phoenix));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new Shock(), new Shock()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        for (int i = 0; i < 2; i++) {
            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();
        }

        advanceToCombat(player1);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(phoenix.getId()));
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
