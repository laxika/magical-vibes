package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BorosSignet;
import com.github.laxika.magicalvibes.cards.c.CourierHawk;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HalcyonGlaze.class, CourierHawk.class, BorosSignet.class})
class HalcyonGlazeTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes a 4/4 Illusion with flying when you cast a creature spell")
    void animatesForCreatureSpell() {
        Permanent glaze = addGlaze();

        assertThat(gqs.isCreature(gd, glaze)).isFalse();
        assertThat(gqs.isEnchantment(gd, glaze)).isTrue();

        castCreatureSpell();

        assertThat(gqs.isCreature(gd, glaze)).isTrue();
        assertThat(gqs.isEnchantment(gd, glaze)).isTrue();
        assertThat(gqs.getEffectivePower(gd, glaze)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, glaze)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, glaze)).containsExactly(CardSubtype.ILLUSION);
        assertThat(gqs.hasKeyword(gd, glaze, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not animate when you cast a noncreature spell")
    void doesNotAnimateForNoncreatureSpell() {
        Permanent glaze = addGlaze();
        harness.castFromHand(player1, new BorosSignet(), "{2}");

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, glaze)).isFalse();
        assertThat(gqs.isEnchantment(gd, glaze)).isTrue();
    }

    @Test
    @DisplayName("Does not animate when an opponent casts a creature spell")
    void doesNotAnimateForOpponentCreatureSpell() {
        Permanent glaze = addGlaze();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new CourierHawk(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, glaze)).isFalse();
        assertThat(gqs.isEnchantment(gd, glaze)).isTrue();
    }

    @Test
    @DisplayName("Animation wears off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        Permanent glaze = addGlaze();
        castCreatureSpell();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, glaze)).isFalse();
        assertThat(gqs.isEnchantment(gd, glaze)).isTrue();
        assertThat(gqs.hasKeyword(gd, glaze, Keyword.FLYING)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, glaze)).isEmpty();
    }

    @Test
    @DisplayName("Animation resolves before the creature spell and not at cast time")
    void animationResolvesBeforeCreatureSpell() {
        Permanent glaze = addGlaze();
        harness.castFromHand(player1, new CourierHawk(), "{1}{W}");

        assertThat(gqs.isCreature(gd, glaze)).isFalse();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isCreature(gd, glaze)).isTrue();
        assertThat(gqs.getEffectivePower(gd, glaze)).isEqualTo(4);
        harness.assertNotOnBattlefield(player1, "Courier Hawk");

        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Courier Hawk");
    }

    @Test
    @DisplayName("A creature entering without being cast does not animate the enchantment")
    void creatureEnteringWithoutCastDoesNotAnimate() {
        Permanent glaze = addGlaze();
        harness.addToBattlefield(player1, new CourierHawk());
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, glaze)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each copy animates and repeated creature casts keep the base power and toughness at 4/4")
    void multipleCopiesAndRepeatedCasts() {
        Permanent first = addGlaze();
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HalcyonGlaze());
        castCreatureSpell();
        castCreatureSpell();

        for (Permanent glaze : java.util.List.of(first, second)) {
            assertThat(gqs.isCreature(gd, glaze)).isTrue();
            assertThat(gqs.isEnchantment(gd, glaze)).isTrue();
            assertThat(gqs.getEffectivePower(gd, glaze)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, glaze)).isEqualTo(4);
            assertThat(gqs.hasKeyword(gd, glaze, Keyword.FLYING)).isTrue();
        }
    }
    private Permanent addGlaze() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return harness.addToBattlefieldAndReturn(player1, new HalcyonGlaze());
    }

    private void castCreatureSpell() {
        harness.castFromHand(player1, new CourierHawk(), "{1}{W}");
        resolveAllTriggers();
    }
}
