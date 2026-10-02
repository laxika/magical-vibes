package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.d.DrudgeReavers;
import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({PrematureBurial.class, AshcoatBear.class, DrudgeReavers.class, Island.class})
class PrematureBurialTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a nonblack creature that entered this turn")
    void destroysCreatureThatEnteredThisTurn() {
        Card creature = new AshcoatBear();
        addEligibleCreature(creature, gd.permanentsEnteredBattlefieldThisTurn);

        castPrematureBurial(creature);

        harness.assertNotOnBattlefield(player2, "Ashcoat Bear");
    }

    @Test
    @DisplayName("Destroys a nonblack creature that entered the preceding turn")
    void destroysCreatureThatEnteredThePrecedingTurn() {
        Card creature = new AshcoatBear();
        addEligibleCreature(creature, gd.permanentsEnteredBattlefieldLastTurn);

        castPrematureBurial(creature);

        harness.assertNotOnBattlefield(player2, "Ashcoat Bear");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Card creature = new DrudgeReavers();
        addEligibleCreature(creature, gd.permanentsEnteredBattlefieldThisTurn);

        assertThatThrownBy(() -> castPrematureBurial(creature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Cannot target a creature that entered before the previous turn")
    void cannotTargetOlderCreature() {
        Card creature = new AshcoatBear();
        harness.addToBattlefield(player2, creature);

        assertThatThrownBy(() -> castPrematureBurial(creature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("entered since your last turn ended");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Card land = new Island();
        harness.addToBattlefield(player2, land);
        gd.permanentsEnteredBattlefieldThisTurn.put(player2.getId(), new ArrayList<>(List.of(land)));

        assertThatThrownBy(() -> castPrematureBurial(land))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");

        harness.assertOnBattlefield(player2, "Island");
    }

    @Test
    @DisplayName("Cannot target a creature before your first turn ends")
    void cannotTargetCreatureBeforeYourFirstTurnEnds() {
        Card creature = new AshcoatBear();
        addEligibleCreature(creature, gd.permanentsEnteredBattlefieldThisTurn);

        assertThatThrownBy(() -> castPrematureBurial(creature, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("entered since your last turn ended");
    }

    private void addEligibleCreature(Card creature,
                                     Map<UUID, List<Card>> entriesByController) {
        harness.addToBattlefield(player2, creature);
        entriesByController.put(player2.getId(), new ArrayList<>(List.of(creature)));
    }

    private void castPrematureBurial(Card creature) {
        castPrematureBurial(creature, 2);
    }

    private void castPrematureBurial(Card creature, int turnsTaken) {
        gd.turnsTakenByPlayer.put(player1.getId(), turnsTaken);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new PrematureBurial()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, creature.getName()));
    }
}
