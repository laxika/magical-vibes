package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostPerCounterEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiaryOfDreamsTest extends BaseCardTest {

    // ===== Structure =====

    @Test
    @DisplayName("Has instant/sorcery cast trigger and a cost-reducing draw ability")
    void hasCorrectEffects() {
        DiaryOfDreams card = new DiaryOfDreams();

        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst())
                .isInstanceOf(SpellCastTriggerEffect.class);

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{5}");
        assertThat(ability.getEffects()).anyMatch(e -> e instanceof ReduceActivationCostPerCounterEffect);
        assertThat(ability.getEffects()).anyMatch(e -> e instanceof DrawCardEffect);

        ReduceActivationCostPerCounterEffect reduce = (ReduceActivationCostPerCounterEffect) ability.getEffects().stream()
                .filter(e -> e instanceof ReduceActivationCostPerCounterEffect).findFirst().orElseThrow();
        assertThat(reduce.counterType()).isEqualTo(CounterType.PAGE);
        assertThat(reduce.reductionPerCounter()).isEqualTo(1);
    }

    // ===== Page counter trigger =====

    @Test
    @DisplayName("Casting an instant puts a page counter on Diary of Dreams")
    void instantCastAddsPageCounter() {
        Permanent diary = harness.addToBattlefieldAndReturn(player1, new DiaryOfDreams());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve the page-counter cast trigger

        assertThat(diary.getCounterCount(CounterType.PAGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature does not add a page counter")
    void creatureCastAddsNoPageCounter() {
        Permanent diary = harness.addToBattlefieldAndReturn(player1, new DiaryOfDreams());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(diary.getCounterCount(CounterType.PAGE)).isZero();
    }

    // ===== Activated ability + cost reduction =====

    @Test
    @DisplayName("With no page counters the ability costs {5}")
    void abilityCostsFiveWithNoCounters() {
        Permanent diary = harness.addToBattlefieldAndReturn(player1, new DiaryOfDreams());
        setDeck(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(diary.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Three page counters reduce the ability to {2}")
    void threeCountersReduceCostToTwo() {
        Permanent diary = harness.addToBattlefieldAndReturn(player1, new DiaryOfDreams());
        diary.setCounterCount(CounterType.PAGE, 3);
        setDeck(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Cannot activate with less mana than the reduced cost")
    void cannotActivateWithoutEnoughReducedMana() {
        Permanent diary = harness.addToBattlefieldAndReturn(player1, new DiaryOfDreams());
        diary.setCounterCount(CounterType.PAGE, 3); // cost reduced to {2}
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1); // only {1}

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("More page counters than the cost floor the ability at {0}")
    void countersBeyondCostFloorAtZero() {
        Permanent diary = harness.addToBattlefieldAndReturn(player1, new DiaryOfDreams());
        diary.setCounterCount(CounterType.PAGE, 8); // more than the {5} generic cost
        setDeck(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        // No mana added — {0} activation cost

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(diary.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Forest"));
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
