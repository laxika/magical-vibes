package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(FireNationAttacks.class)
class FireNationAttacksTest extends BaseCardTest {

    @Test
    void createsTwoSoldiersWithFirebending() {
        harness.setHand(player1, List.of(new FireNationAttacks()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> soldiers = findPermanents(player1, "Soldier");
        assertThat(soldiers).hasSize(2);

        soldiers.forEach(soldier -> soldier.setSummoningSick(false));
        declareAttackers(List.of(0, 1));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    void flashbackCreatesSoldiersAndExilesTheSpell() {
        harness.setGraveyard(player1, List.of(new FireNationAttacks()));
        harness.addMana(player1, ManaColor.RED, 9);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier")).hasSize(2);
        harness.assertNotInGraveyard(player1, "Fire Nation Attacks");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Fire Nation Attacks"));
    }
}
