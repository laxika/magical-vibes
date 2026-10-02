package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.Attrition;
import com.github.laxika.magicalvibes.cards.m.MetathranSoldier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IthilienKingfisher.class, Attrition.class, MetathranSoldier.class})
class IthilienKingfisherTest extends BaseCardTest {

    @Test
    @DisplayName("When Ithilien Kingfisher dies, its controller draws a card")
    void diesDrawsCard() {
        Permanent kingfisher = addCreatureReady(player1, new IthilienKingfisher());
        harness.setLibrary(player1, List.of(new MetathranSoldier()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        killWithAttrition(player2, kingfisher);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertInHand(player1, "Metathran Soldier");
    }

    private void killWithAttrition(Player abilityController, Permanent target) {
        Permanent attrition = harness.addToBattlefieldAndReturn(abilityController, new Attrition());
        addCreatureReady(abilityController, new MetathranSoldier());

        harness.forceActivePlayer(abilityController);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(abilityController, ManaColor.BLACK, 1);

        int attritionIndex = gd.playerBattlefields.get(abilityController.getId()).indexOf(attrition);
        harness.activateAbility(abilityController, attritionIndex, null, target.getId());
        resolveAllTriggers();
    }
}
