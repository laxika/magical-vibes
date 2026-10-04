package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BurningHands;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ElturgardRanger.class)
class ElturgardRangerTest extends BaseCardTest {

    @Test
    void enteringTheBattlefieldCreatesATwoTwoGreenWolfToken() {
        harness.setHand(player1, List.of(new ElturgardRanger()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Elturgard Ranger");
        List<Permanent> wolves = findPermanents(player1, "Wolf");
        assertThat(wolves).hasSize(1);
        Permanent wolf = wolves.getFirst();
        assertThat(wolf.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(wolf.getCard().getSubtypes()).contains(CardSubtype.WOLF);
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
    }

    @Test
    @CardUsed(BurningHands.class)
    void entryTriggerCreatesWolfEvenIfRangerDiesBeforeItResolves() {
        harness.setHand(player1, List.of(new ElturgardRanger()));
        harness.setHand(player2, List.of(new BurningHands()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Elturgard Ranger");
        assertThat(countPermanents(player1, "Wolf")).isZero();

        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Elturgard Ranger"));

        harness.assertInGraveyard(player1, "Elturgard Ranger");
        harness.assertNotOnBattlefield(player1, "Elturgard Ranger");
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Wolf")).isEqualTo(1);
        assertThat(countPermanents(player2, "Wolf")).isZero();
    }
}
