package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZardaThePowerPrincess.class, GrizzlyBears.class})
class ZardaThePowerPrincessTest extends BaseCardTest {

    @Test
    @DisplayName("A Hero attacking alone gets +1/+1")
    void loneHeroGetsExaltedBonus() {
        addCreatureReady(player1, new ZardaThePowerPrincess());
        Permanent ownHero = addCreatureReady(player1, hero());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownHero)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownHero)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownHero)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownHero)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-Hero creatures do not gain exalted")
    void excludesNonHeroes() {
        addCreatureReady(player1, new ZardaThePowerPrincess());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Heroes controlled by an opponent do not gain exalted")
    void excludesOpponentsHeroes() {
        addCreatureReady(player1, new ZardaThePowerPrincess());
        Permanent opposingHero = addCreatureReady(player2, hero());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opposingHero)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingHero)).isEqualTo(2);
    }

    @Test
    @DisplayName("Zarda does not grant exalted to itself or non-Hero creatures")
    void excludesSourceAndNonHeroes() {
        Permanent zarda = addCreatureReady(player1, new ZardaThePowerPrincess());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, zarda)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, zarda)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
    }

    private static Card hero() {
        Card card = new Card();
        card.setName("Test Hero");
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(CardSubtype.HERO));
        return card;
    }
}
