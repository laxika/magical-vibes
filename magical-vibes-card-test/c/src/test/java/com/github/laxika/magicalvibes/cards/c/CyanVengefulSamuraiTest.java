package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Reminisce;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CyanVengefulSamurai.class, GrizzlyBears.class, Reminisce.class, Shock.class})
class CyanVengefulSamuraiTest extends BaseCardTest {

    @Test
    void costsOneLessForEachCreatureCardInYourGraveyard() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new Shock()));
        harness.setHand(player1, List.of(new CyanVengefulSamurai()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    void putsOneCounterWhenMultipleCreatureCardsLeaveYourGraveyardTogether() {
        Permanent cyan = harness.addToBattlefieldAndReturn(player1, new CyanVengefulSamurai());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(cyan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotReduceCostForOpponentsCreaturesOrYourNoncreatures() {
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new CyanVengefulSamurai()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void reductionBeyondGenericCostStillRequiresWhiteMana() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears()));
        harness.setHand(player1, List.of(new CyanVengefulSamurai()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void doesNotTriggerWhenOnlyNoncreatureCardsLeaveYourGraveyard() {
        Permanent cyan = harness.addToBattlefieldAndReturn(player1, new CyanVengefulSamurai());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(cyan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotInGraveyard(player1, "Shock");
    }

    @Test
    void doesNotTriggerWhenCreatureCardsLeaveOpponentsGraveyard() {
        Permanent cyan = harness.addToBattlefieldAndReturn(player1, new CyanVengefulSamurai());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(cyan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void separateCreatureDepartureEventsEachPutACounterOnCyan() {
        Permanent cyan = harness.addToBattlefieldAndReturn(player1, new CyanVengefulSamurai());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Reminisce(), new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(cyan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void dealsDamageInBothCombatDamageSteps() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new CyanVengefulSamurai());

        declareAttackers(List.of(0));
        resolveCombat(player1);

        harness.assertLife(player2, 14);
    }
}
