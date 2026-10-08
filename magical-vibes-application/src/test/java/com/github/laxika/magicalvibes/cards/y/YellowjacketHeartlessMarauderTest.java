package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.k.KreeCommandos;
import com.github.laxika.magicalvibes.cards.h.HydraulicHelper;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YellowjacketHeartlessMarauder.class, KreeCommandos.class, HydraulicHelper.class})
class YellowjacketHeartlessMarauderTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 and lifelink when another Villain enters under your control")
    void triggersForAnotherVillainYouControl() {
        Permanent yellowjacket = harness.addToBattlefieldAndReturn(player1, new YellowjacketHeartlessMarauder());
        harness.setHand(player1, List.of(new KreeCommandos()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(yellowjacket.getPowerModifier()).isEqualTo(1);
        assertThat(yellowjacket.getToughnessModifier()).isZero();
        assertThat(yellowjacket.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger for a non-Villain creature")
    void doesNotTriggerForNonVillain() {
        Permanent yellowjacket = harness.addToBattlefieldAndReturn(player1, new YellowjacketHeartlessMarauder());
        harness.setHand(player1, List.of(new HydraulicHelper()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(yellowjacket.getPowerModifier()).isZero();
        assertThat(yellowjacket.hasKeyword(Keyword.LIFELINK)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for an opponent's Villain")
    void doesNotTriggerForOpponentsVillain() {
        Permanent yellowjacket = harness.addToBattlefieldAndReturn(player1, new YellowjacketHeartlessMarauder());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new KreeCommandos()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(yellowjacket.getPowerModifier()).isZero();
        assertThat(yellowjacket.hasKeyword(Keyword.LIFELINK)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The boost and lifelink wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent yellowjacket = harness.addToBattlefieldAndReturn(player1, new YellowjacketHeartlessMarauder());
        harness.setHand(player1, List.of(new KreeCommandos()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(yellowjacket.getPowerModifier()).isZero();
        assertThat(yellowjacket.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger when Yellowjacket itself enters")
    void doesNotTriggerForItself() {
        harness.setHand(player1, List.of(new YellowjacketHeartlessMarauder()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent yellowjacket = findPermanent(player1, "Yellowjacket, Heartless Marauder");
        assertThat(gd.stack).isEmpty();
        assertThat(yellowjacket.getPowerModifier()).isZero();
        assertThat(yellowjacket.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Each Villain entry adds a separate boost")
    void multipleVillainsStackTheBoost() {
        Permanent yellowjacket = harness.addToBattlefieldAndReturn(player1, new YellowjacketHeartlessMarauder());
        harness.enterBattlefieldAndReturn(player1, new KreeCommandos());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new KreeCommandos());
        resolveAllTriggers();

        assertThat(yellowjacket.getPowerModifier()).isEqualTo(2);
        assertThat(yellowjacket.getToughnessModifier()).isZero();
        assertThat(yellowjacket.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Granted lifelink gains life from boosted combat damage")
    void grantedLifelinkGainsLife() {
        addCreatureReady(player1, new YellowjacketHeartlessMarauder());
        harness.enterBattlefieldAndReturn(player1, new KreeCommandos());
        resolveAllTriggers();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }
}
