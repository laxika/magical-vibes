package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PriestOfTheBloodRiteTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 5/5 flying black Demon token when it enters")
    void etbCreatesDemonToken() {
        harness.setHand(player1, List.of(new PriestOfTheBloodRite()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the creature, ETB trigger goes on the stack
        harness.passBothPriorities(); // resolve the ETB trigger

        Permanent demon = findDemon();
        assertThat(demon).isNotNull();
        assertThat(demon.getCard().getPower()).isEqualTo(5);
        assertThat(demon.getCard().getToughness()).isEqualTo(5);
        assertThat(demon.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Controller loses 2 life at the beginning of their upkeep")
    void upkeepTriggerLosesTwoLife() {
        harness.addToBattlefield(player1, new PriestOfTheBloodRite());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not lose life during the opponent's upkeep")
    void noLifeLossOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new PriestOfTheBloodRite());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private Permanent findDemon() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> "Demon".equals(p.getCard().getName()))
                .findFirst()
                .orElse(null);
    }
}
