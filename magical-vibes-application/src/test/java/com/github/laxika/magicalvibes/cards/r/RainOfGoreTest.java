package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HealingHands;
import com.github.laxika.magicalvibes.cards.v.VampireNighthawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({RainOfGore.class, HealingHands.class, GrizzlyBears.class, VampireNighthawk.class})
class RainOfGoreTest extends BaseCardTest {

    @Test
    @DisplayName("A spell causing its controller to gain life causes life loss instead")
    void spellControllerLifeGainBecomesLoss() {
        harness.addToBattlefield(player2, new RainOfGore());
        harness.setHand(player1, List.of(new HealingHands()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("A spell causing another player to gain life is unaffected")
    void anotherPlayersLifeGainIsUnaffected() {
        harness.addToBattlefield(player2, new RainOfGore());
        harness.setHand(player1, List.of(new HealingHands()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 24);
    }

    @Test
    @DisplayName("Combat damage lifelink is unaffected")
    void combatDamageLifelinkIsUnaffected() {
        addCreatureReady(player1, new VampireNighthawk());
        harness.addToBattlefield(player2, new RainOfGore());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }
}
