package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RalCracklingWit.class, Divination.class, GrizzlyBears.class})
class RalCracklingWitTest extends BaseCardTest {

    @Test
    @DisplayName("+1 creates an Otter that gets +1/+1 when its controller casts a noncreature spell")
    void plusOneCreatesOtterWithProwess() {
        Permanent ral = addReadyRal(2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent otter = findPermanent(player1, "Otter");
        assertThat(gqs.getEffectivePower(gd, otter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, otter)).isEqualTo(1);

        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, otter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otter)).isEqualTo(2);
        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not put a loyalty counter on Ral for a creature spell")
    void doesNotTriggerForCreatureSpell() {
        Permanent ral = addReadyRal(3);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("-3 draws three cards then discards two cards")
    void minusThreeDrawsThenDiscards() {
        Permanent ral = addReadyRal(5);
        Card first = new Divination();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second, third));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third);
    }

    @Test
    @DisplayName("-10 draws three cards and creates an emblem that gives instant and sorcery spells storm")
    void minusTenCreatesStormEmblem() {
        addReadyRal(10);
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        gd.recordSpellCast(player2.getId(), new GrizzlyBears());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
        assertThat(gd.stack.stream().filter(StackEntry::isCopy))
                .allMatch(entry -> entry.getCard().getName().equals("Divination"));
    }

    private Permanent addReadyRal(int loyalty) {
        Permanent ral = new Permanent(new RalCracklingWit());
        ral.setCounterCount(CounterType.LOYALTY, loyalty);
        ral.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ral);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return ral;
    }
}
