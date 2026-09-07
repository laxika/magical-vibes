package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PaladinOfPrahv.class, GrizzlyBears.class, Forest.class})
class PaladinOfPrahvTest extends BaseCardTest {

    @Test
    @DisplayName("The battlefield ability gains life equal to damage dealt")
    void battlefieldAbilityGainsLifeFromDamage() {
        addCreatureReady(player1, new PaladinOfPrahv());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(player1, java.util.List.of(0));
        resolveCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Forecast keeps the card in hand and gains life from the target creature's damage")
    void forecastWatchesTargetCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        PaladinOfPrahv paladin = new PaladinOfPrahv();
        harness.setHand(player1, java.util.List.of(paladin));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, bears.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(paladin);
        harness.passBothPriorities();

        declareAttackers(player2, java.util.List.of(0));
        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Forecast can be activated only once each turn")
    void forecastIsLimitedToOncePerTurn() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(new PaladinOfPrahv()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, bears.getId());

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Forecast cannot be activated outside your upkeep")
    void forecastRequiresYourUpkeep() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(new PaladinOfPrahv()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your upkeep");
    }

    @Test
    @DisplayName("Forecast targets creatures only")
    void forecastRejectsNoncreatureTarget() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, java.util.List.of(new PaladinOfPrahv()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
