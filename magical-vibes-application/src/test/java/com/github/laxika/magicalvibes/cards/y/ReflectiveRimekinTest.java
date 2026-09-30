package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReflectiveRimekin.class, LavaAxe.class, Shock.class})
class ReflectiveRimekinTest extends BaseCardTest {

    @Test
    @DisplayName("Copies the next instant or sorcery with mana value 3 or less, even after a turn ends")
    void copiesNextSmallInstantOrSorceryOnceAcrossTurns() {
        harness.setHand(player1, List.of(new ReflectiveRimekin()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        Shock firstShock = new Shock();
        Shock secondShock = new Shock();
        harness.setHand(player1, List.of(firstShock, secondShock));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Does not consume the boon on an instant or sorcery with mana value greater than 3")
    void ignoresExpensiveSorcery() {
        harness.setHand(player1, List.of(new ReflectiveRimekin(), new LavaAxe(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.castSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(11);
    }
}
