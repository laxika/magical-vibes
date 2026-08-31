package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RaccoonRallier;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({BrambleguardVeteran.class, GrizzlyBears.class, RaccoonRallier.class, Shock.class})
class BrambleguardVeteranTest extends BaseCardTest {

    @Test
    @DisplayName("Gives your Raccoons +1/+1 and vigilance when you expend four")
    void boostsRaccoonsWhenControllerExpendsFour() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent raccoon = harness.addToBattlefieldAndReturn(player1, new RaccoonRallier());
        Permanent nonRaccoon = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BrambleguardVeteran(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent veteran = findPermanent(player1, "Brambleguard Veteran");
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(veteran.getPowerModifier()).isEqualTo(1);
        assertThat(veteran.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, veteran, Keyword.VIGILANCE)).isTrue();
        assertThat(raccoon.getPowerModifier()).isEqualTo(1);
        assertThat(raccoon.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, raccoon, Keyword.VIGILANCE)).isTrue();
        assertThat(nonRaccoon.getPowerModifier()).isZero();
        assertThat(nonRaccoon.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, nonRaccoon, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger before four mana is spent")
    void doesNotTriggerBelowExpendThreshold() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent veteran = harness.addToBattlefieldAndReturn(player1, new BrambleguardVeteran());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(veteran.getPowerModifier()).isZero();
        assertThat(veteran.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, veteran, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The expend boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent veteran = harness.addToBattlefieldAndReturn(player1, new BrambleguardVeteran());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 4);

        for (int i = 0; i < 4; i++) {
            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();
        }

        assertThat(veteran.getPowerModifier()).isEqualTo(1);
        assertThat(veteran.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, veteran, Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(veteran.getPowerModifier()).isZero();
        assertThat(veteran.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, veteran, Keyword.VIGILANCE)).isFalse();
    }
}
