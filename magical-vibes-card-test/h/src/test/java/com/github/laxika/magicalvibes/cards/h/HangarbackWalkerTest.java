package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HangarbackWalker.class, WrathOfGod.class})
class HangarbackWalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=2 enters with two +1/+1 counters and is a 2/2")
    void entersWithXPlusOneCounters() {
        harness.setHand(player1, List.of(new HangarbackWalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, 2);
        harness.passBothPriorities();

        Permanent walker = findPermanent(player1, "Hangarback Walker");
        assertThat(walker).isNotNull();
        assertThat(walker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, walker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, walker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activating {1}, {T} puts a +1/+1 counter on it and taps it")
    void activatedAbilityAddsCounter() {
        Permanent walker = addWalkerReady(player1, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(walker.isTapped()).isTrue();
        assertThat(walker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, walker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Dying creates one 1/1 flying Thopter per +1/+1 counter")
    void deathCreatesThopterPerCounter() {
        addWalkerReady(player1, 3);

        killWithWrath();

        List<Permanent> thopters = findPermanents(player1, "Thopter");
        assertThat(thopters).hasSize(3);
        assertThat(thopters).allSatisfy(thopter -> {
            assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
            assertThat(thopter.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(thopter.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(thopter.getCard().getSubtypes()).contains(CardSubtype.THOPTER);
            assertThat(thopter.getCard().getColor()).isNull();
        });
    }

    @Test
    @DisplayName("Only +1/+1 counters are counted for the Thopters")
    void otherCounterTypesDoNotMakeThopters() {
        Permanent walker = addWalkerReady(player1, 1);
        walker.setCounterCount(CounterType.CHARGE, 2);

        killWithWrath();

        assertThat(findPermanents(player1, "Thopter")).hasSize(1);
    }

    @Test
    void castingWithZeroDiesWithoutCreatingThopters() {
        harness.setHand(player1, List.of(new HangarbackWalker()));

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hangarback Walker");
        harness.assertInGraveyard(player1, "Hangarback Walker");
        assertThat(findPermanents(player1, "Thopter")).isEmpty();
    }

    @Test
    void bothXSymbolsMustBePaid() {
        harness.setHand(player1, List.of(new HangarbackWalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 2))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Hangarback Walker");
        harness.assertNotOnBattlefield(player1, "Hangarback Walker");
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent walker = addWalkerReady(player1, 1);
        walker.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(walker.isTapped()).isFalse();
        assertThat(walker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent walker = addWalkerReady(player1, 1);
        walker.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(walker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void activatedCounterIsNotAddedUntilResolution() {
        Permanent walker = addWalkerReady(player1, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(walker.isTapped()).isTrue();
        assertThat(walker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(walker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void deathCreatesTokensForTheDyingWalkersController() {
        addWalkerReady(player2, 2);

        killWithWrath();

        assertThat(findPermanents(player2, "Thopter")).hasSize(2);
        assertThat(findPermanents(player1, "Thopter")).isEmpty();
        harness.assertInGraveyard(player2, "Hangarback Walker");
    }

    private void killWithWrath() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player2, 0, 0);
        harness.passBothPriorities();
    }

    private Permanent addWalkerReady(Player player, int counters) {
        Permanent perm = addCreatureReady(player, new HangarbackWalker());
        perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        return perm;
    }
}
