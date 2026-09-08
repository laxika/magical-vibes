package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(UndercoverCrocodelf.class)
class UndercoverCrocodelfTest extends BaseCardTest {

    @Test
    @DisplayName("Investigates when it deals combat damage to a player")
    void investigatesOnCombatDamageToPlayer() {
        Permanent crocodelf = addReadyCrocodelf();
        crocodelf.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Does not investigate when it deals no combat damage to a player")
    void doesNotInvestigateWithoutCombatDamageToPlayer() {
        addReadyCrocodelf();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Disguise casts it face down and turns it face up")
    void disguiseCastsAndTurnsFaceUp() {
        UndercoverCrocodelf card = new UndercoverCrocodelf();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent crocodelf = findPermanentForCard(card);
        assertThat(crocodelf.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(crocodelf));

        assertThat(crocodelf.isFaceDown()).isFalse();
    }

    private Permanent addReadyCrocodelf() {
        Permanent crocodelf = new Permanent(new UndercoverCrocodelf());
        crocodelf.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(crocodelf);
        return crocodelf;
    }

    private Permanent findPermanentForCard(UndercoverCrocodelf card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
