package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TrespassersCurseTest extends BaseCardTest {

    // ===== Casting and attaching to a player =====

    @Test
    @DisplayName("Can be cast targeting an opponent, entering attached to that player")
    void castTargetingOpponent() {
        harness.setHand(player1, List.of(new TrespassersCurse()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve

        Permanent curse = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trespasser's Curse"))
                .findFirst().orElseThrow();
        assertThat(curse.getAttachedTo()).isEqualTo(player2.getId());
    }

    // ===== Trigger — creature the enchanted player controls enters =====

    @Test
    @DisplayName("Enchanted player loses 1 life and you gain 1 life when their creature enters")
    void drainsWhenEnchantedPlayerCreatureEnters() {
        addCurseOnPlayer2();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);

        harness.passBothPriorities(); // resolve creature spell → curse triggers
        harness.passBothPriorities(); // resolve the drain trigger

        harness.assertLife(player2, 19); // enchanted player loses 1
        harness.assertLife(player1, 21); // curse controller gains 1
    }

    // ===== No trigger — creature the curse controller controls enters =====

    @Test
    @DisplayName("Does not trigger when a creature the curse controller controls enters")
    void doesNotTriggerForCurseControllerCreature() {
        addCurseOnPlayer2();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty(); // no drain trigger queued
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    // ===== Helpers =====

    private void addCurseOnPlayer2() {
        Permanent curse = new Permanent(new TrespassersCurse());
        curse.setAttachedTo(player2.getId());
        gd.playerBattlefields.get(player1.getId()).add(curse);
    }
}
