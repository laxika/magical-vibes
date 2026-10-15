package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SkywatcherAdept;
import com.github.laxika.magicalvibes.cards.s.StarfieldOfNyx;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GravitationalShift.class, SkywatcherAdept.class})
class GravitationalShiftTest extends BaseCardTest {

    private static Card creature(boolean flying) {
        Card card = new Card();
        card.setName(flying ? "Flying Creature" : "Ground Creature");
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}");
        card.setColor(CardColor.GREEN);
        card.setPower(2);
        card.setToughness(2);
        card.setKeywords(flying ? Set.of(Keyword.FLYING) : Set.of());
        return card;
    }

    @Test
    @DisplayName("Boosts creatures with flying and shrinks creatures without flying")
    void modifiesCreaturesBasedOnFlying() {
        harness.addToBattlefield(player1, new GravitationalShift());
        Permanent ownFlyer = harness.addToBattlefieldAndReturn(player1, creature(true));
        Permanent ownGroundCreature = harness.addToBattlefieldAndReturn(player1, creature(false));
        Permanent opposingFlyer = harness.addToBattlefieldAndReturn(player2, creature(true));
        Permanent opposingGroundCreature = harness.addToBattlefieldAndReturn(player2, creature(false));

        assertThat(gqs.getEffectivePower(gd, ownFlyer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownFlyer)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownGroundCreature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, ownGroundCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingFlyer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opposingFlyer)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingGroundCreature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, opposingGroundCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gaining flying switches a creature from the penalty to the bonus")
    void reevaluatesFlyingAfterLevelUp() {
        harness.addToBattlefield(player1, new GravitationalShift());
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new SkywatcherAdept());

        assertThat(gqs.getEffectivePower(gd, adept)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, adept)).isEqualTo(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, adept)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, adept)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple copies stack for flyers and allow negative power for ground creatures")
    void multipleCopiesStack() {
        harness.addToBattlefield(player1, new GravitationalShift());
        harness.addToBattlefield(player2, new GravitationalShift());
        Permanent ground = harness.addToBattlefieldAndReturn(player1, new SkywatcherAdept());
        Permanent flyer = harness.addToBattlefieldAndReturn(player2, new SkywatcherAdept());
        flyer.setCounterCount(CounterType.LEVEL, 1);

        assertThat(gqs.getEffectivePower(gd, ground)).isEqualTo(-3);
        assertThat(gqs.getEffectiveToughness(gd, ground)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, flyer)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, flyer)).isEqualTo(2);
    }

    @Test
    @CardUsed({StarfieldOfNyx.class})
    @DisplayName("Gravitational Shift applies its own penalty when it becomes a creature")
    void affectsItselfWhenAnimated() {
        Permanent shift = harness.addToBattlefieldAndReturn(player1, new GravitationalShift());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new StarfieldOfNyx());
        }

        assertThat(gqs.getEffectivePower(gd, shift)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, shift)).isEqualTo(5);
    }
}
