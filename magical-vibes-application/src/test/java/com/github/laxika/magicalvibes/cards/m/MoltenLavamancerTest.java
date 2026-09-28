package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoltenLavamancer.class, Shock.class})
class MoltenLavamancerTest extends BaseCardTest {

    @Test
    @DisplayName("Prowess gives Molten Lavamancer +1/+1 for a noncreature spell")
    void prowessPumpsForNoncreatureSpell() {
        harness.addToBattlefield(player1, new MoltenLavamancer());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveStack();

        Permanent lavamancer = findPermanent(player1, "Molten Lavamancer");
        assertThat(lavamancer.getPowerModifier()).isEqualTo(1);
        assertThat(lavamancer.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates only one Elemental token from noncombat damage each turn")
    void createsOneElementalPerTurn() {
        harness.addToBattlefield(player1, new MoltenLavamancer());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        resolveStack();

        harness.castInstant(player1, 0, player2.getId());
        resolveStack();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Elemental"))
                .hasSize(1);
    }

    @Test
    @DisplayName("The Elemental trigger does not fire during an opponent's turn")
    void doesNotCreateTokenDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new MoltenLavamancer());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, player1.getId());
        resolveStack();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Elemental"));
    }

    private void resolveStack() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
