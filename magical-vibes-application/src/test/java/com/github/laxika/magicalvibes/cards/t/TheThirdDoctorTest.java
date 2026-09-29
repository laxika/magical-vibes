package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.n.NestInvader;
import com.github.laxika.magicalvibes.cards.n.NoviceInspector;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheThirdDoctor.class, NestInvader.class, NoviceInspector.class})
class TheThirdDoctorTest extends BaseCardTest {

    private static final String CLUE_MODE = "Create a Clue token";
    private static final String FOOD_MODE = "Create a Food token";
    private static final String TREASURE_MODE = "Create a Treasure token";

    @Test
    void createsAClueToken() {
        castDoctor(CLUE_MODE);

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Food")).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void createsAFoodToken() {
        castDoctor(FOOD_MODE);

        assertThat(findPermanents(player1, "Food")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void createsATreasureToken() {
        castDoctor(TREASURE_MODE);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player1, "Food")).isEmpty();
    }

    @Test
    void getsPlusOnePlusOneForEachNoncreatureTokenYouControl() {
        Permanent doctor = castDoctor(TREASURE_MODE);
        assertThat(gqs.getEffectivePower(gd, doctor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, doctor)).isEqualTo(3);

        harness.enterBattlefieldAndReturn(player1, new NoviceInspector());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, doctor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, doctor)).isEqualTo(4);

        harness.enterBattlefieldAndReturn(player1, new NestInvader());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, doctor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, doctor)).isEqualTo(4);

        harness.enterBattlefieldAndReturn(player2, new NoviceInspector());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, doctor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, doctor)).isEqualTo(4);
    }

    private Permanent castDoctor(String mode) {
        harness.setHand(player1, List.of(new TheThirdDoctor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int modeIndex = List.of(CLUE_MODE, FOOD_MODE, TREASURE_MODE).indexOf(mode);
        harness.castCreature(player1, 0, modeIndex);
        harness.passBothPriorities();
        harness.passBothPriorities();

        return findPermanent(player1, "The Third Doctor");
    }
}
