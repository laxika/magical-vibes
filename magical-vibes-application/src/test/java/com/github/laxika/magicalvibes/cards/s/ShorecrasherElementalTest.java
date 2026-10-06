package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BreakOpen;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShorecrasherElemental.class, BreakOpen.class})
class ShorecrasherElementalTest extends BaseCardTest {

    @Test
    void megamorphPutsACounterOnItWhenTurnedFaceUp() {
        harness.setHand(player1, List.of(new ShorecrasherElemental()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent elemental = findPermanent(player1, "Shorecrasher Elemental");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(elemental));
        harness.passBothPriorities();

        assertThat(elemental.isFaceDown()).isFalse();
        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void blueAbilityReturnsItFaceDownUnderItsOwnersControl() {
        Permanent elemental = addCreatureReady(player1, new ShorecrasherElemental());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Shorecrasher Elemental");
        assertThat(returned).isNotSameAs(elemental);
        assertThat(returned.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(2);
    }

    @Test
    void firstModeGivesItPlusOneMinusOne() {
        Permanent elemental = addCreatureReady(player1, new ShorecrasherElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.handleListChoice(player1, "This creature gets +1/-1 until end of turn");

        assertThat(elemental.getPowerModifier()).isEqualTo(1);
        assertThat(elemental.getToughnessModifier()).isEqualTo(-1);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(elemental.getPowerModifier()).isZero();
        assertThat(elemental.getToughnessModifier()).isZero();
    }

    @Test
    void secondModeGivesItMinusOnePlusOne() {
        Permanent elemental = addCreatureReady(player1, new ShorecrasherElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.handleListChoice(player1, "This creature gets -1/+1 until end of turn");

        assertThat(elemental.getPowerModifier()).isEqualTo(-1);
        assertThat(elemental.getToughnessModifier()).isEqualTo(1);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(elemental.getPowerModifier()).isZero();
        assertThat(elemental.getToughnessModifier()).isZero();
    }

    @Test
    void turningFaceUpWithAnotherSpellDoesNotGiveAMegamorphCounter() {
        addCreatureReady(player1, new ShorecrasherElemental());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Shorecrasher Elemental");
        harness.setHand(player2, List.of(new BreakOpen()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, returned.getId());

        assertThat(returned.isFaceDown()).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void returnedFaceDownCreatureCanPayMegamorphAndGetsItsCounterImmediately() {
        Permanent elemental = addCreatureReady(player1, new ShorecrasherElemental());
        elemental.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        elemental.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Shorecrasher Elemental");
        assertThat(returned.isFaceDown()).isTrue();
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(returned));

        assertThat(returned.isFaceDown()).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void olderExileAbilityDoesNotExileTheNewFaceDownPermanent() {
        addCreatureReady(player1, new ShorecrasherElemental());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Shorecrasher Elemental");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Shorecrasher Elemental")).isSameAs(returned);
        assertThat(returned.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(2);
    }

    @Test
    void stolenElementalReturnsFaceDownToItsOwner() {
        Permanent elemental = addCreatureReady(player2, new ShorecrasherElemental());
        gd.stolenCreatures.put(elemental.getId(), player1.getId());
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Shorecrasher Elemental");
        Permanent returned = findPermanent(player1, "Shorecrasher Elemental");
        assertThat(returned).isNotSameAs(elemental);
        assertThat(returned.isFaceDown()).isTrue();
    }
}
