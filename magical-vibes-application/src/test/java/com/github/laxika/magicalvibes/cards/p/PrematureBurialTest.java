package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MassOfGhouls;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrematureBurial.class, GrizzlyBears.class, MassOfGhouls.class})
class PrematureBurialTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a nonblack creature that entered this turn")
    void destroysCreatureThatEnteredThisTurn() {
        Card creature = new GrizzlyBears();
        addEligibleCreature(creature, gd.permanentsEnteredBattlefieldThisTurn);

        castPrematureBurial(creature);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroys a nonblack creature that entered the preceding turn")
    void destroysCreatureThatEnteredThePrecedingTurn() {
        Card creature = new GrizzlyBears();
        addEligibleCreature(creature, gd.permanentsEnteredBattlefieldLastTurn);

        castPrematureBurial(creature);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Card creature = new MassOfGhouls();
        addEligibleCreature(creature, gd.permanentsEnteredBattlefieldThisTurn);

        assertThatThrownBy(() -> castPrematureBurial(creature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Cannot target a creature that entered before the previous turn")
    void cannotTargetOlderCreature() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player2, creature);

        assertThatThrownBy(() -> castPrematureBurial(creature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("entered since your last turn ended");
    }

    private void addEligibleCreature(Card creature,
                                     Map<UUID, List<Card>> entriesByController) {
        harness.addToBattlefield(player2, creature);
        entriesByController.put(player2.getId(), new ArrayList<>(List.of(creature)));
    }

    private void castPrematureBurial(Card creature) {
        gd.turnsTakenByPlayer.put(player1.getId(), 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new PrematureBurial()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.getGameService().playCard(
                harness.getGameData(), player1, 0, 0, harness.getPermanentId(player2, creature.getName()), null);
        harness.passBothPriorities();
    }
}
