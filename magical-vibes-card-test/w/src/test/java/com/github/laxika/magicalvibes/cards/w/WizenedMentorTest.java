package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WizenedMentor.class, ProdigalPyromancer.class, Forest.class})
class WizenedMentorTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 1/1 white Zombie when an opponent activates a non-mana ability")
    void createsWhiteZombieForOpponentNonManaAbility() {
        addCreatureReady(player1, new WizenedMentor());
        addCreatureReady(player2, new ProdigalPyromancer());

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(zombie.getCard().getPower()).isEqualTo(1);
        assertThat(zombie.getCard().getToughness()).isEqualTo(1);
        assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
    }

    @Test
    @DisplayName("Does not trigger for a mana ability")
    void doesNotTriggerForManaAbility() {
        addCreatureReady(player1, new WizenedMentor());
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);

        harness.assertNotOnBattlefield(player1, "Zombie");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers only once each turn")
    void triggersOnlyOnceEachTurn() {
        addCreatureReady(player1, new WizenedMentor());
        addCreatureReady(player2, new ProdigalPyromancer());
        addCreatureReady(player2, new ProdigalPyromancer());

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);

        harness.activateAbility(player2, 1, null, player1.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
    }
}
