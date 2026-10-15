package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GarrisonCat.class, WrathOfGod.class})
class GarrisonCatTest extends BaseCardTest {

    @Test
    @DisplayName("When Garrison Cat dies, it creates a Human Soldier token")
    void deathTriggerCreatesHumanSoldierToken() {
        harness.addToBattlefield(player1, new GarrisonCat());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Human Soldier");
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.HUMAN, CardSubtype.SOLDIER);
    }

    @Test
    @CardUsed(GarrisonCat.class)
    @DisplayName("The death trigger creates exactly one token only when it resolves")
    void lethalDamageQueuesTokenCreation() {
        Permanent cat = harness.addToBattlefieldAndReturn(player1, new GarrisonCat());
        cat.setMarkedDamage(1);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Garrison Cat");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent token = findPermanent(player1, "Human Soldier");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @CardUsed(GarrisonCat.class)
    @DisplayName("Simultaneous deaths create one token for each Cat's controller")
    void simultaneousDeathsCreateTokensForBothControllers() {
        Permanent firstCat = harness.addToBattlefieldAndReturn(player1, new GarrisonCat());
        Permanent secondCat = harness.addToBattlefieldAndReturn(player2, new GarrisonCat());
        firstCat.setMarkedDamage(1);
        secondCat.setMarkedDamage(1);

        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Garrison Cat");
        harness.assertInGraveyard(player2, "Garrison Cat");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Human Soldier");
        harness.assertOnBattlefield(player2, "Human Soldier");
    }
}
