package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.l.LeafGilder;
import com.github.laxika.magicalvibes.cards.s.SendToSleep;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.e.ElementalBond;
import com.github.laxika.magicalvibes.cards.m.MacabreWaltz;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NecromanticSummons.class, LeafGilder.class, SendToSleep.class, MacabreWaltz.class, ElementalBond.class})
class NecromanticSummonsTest extends BaseCardTest {

    @Test
    @DisplayName("Reanimates without counters when spell mastery is not active")
    void reanimatesWithoutSpellMastery() {
        Card creature = new LeafGilder();
        harness.setGraveyard(player1, List.of(creature, new SendToSleep()));
        harness.setHand(player1, List.of(new NecromanticSummons()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        Permanent returned = findPermanent(player1, creature.getName());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Spell mastery adds two +1/+1 counters with two instants in the graveyard")
    void spellMasteryAddsCounters() {
        Card creature = new LeafGilder();
        harness.setGraveyard(player1, List.of(creature, new SendToSleep(), new SendToSleep()));
        harness.setHand(player1, List.of(new NecromanticSummons()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(findPermanent(player1, creature.getName()).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Reanimates a creature from an opponent's graveyard under your control")
    void reanimatesFromOpponentGraveyard() {
        Card creature = new LeafGilder();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new NecromanticSummons()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(findPermanent(player1, creature.getName())).isNotNull();
    }

    @Test
    @DisplayName("Cannot target a non-creature card in a graveyard")
    void cannotTargetNonCreatureCard() {
        Card instant = new SendToSleep();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new NecromanticSummons()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void spellMasteryCountsInstantsAndSorceriesInControllersGraveyard() {
        Card creature = new LeafGilder();
        harness.setGraveyard(player2, List.of(creature));
        harness.setGraveyard(player1, List.of(new SendToSleep(), new MacabreWaltz()));
        harness.setHand(player1, List.of(new NecromanticSummons()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(findPermanent(player1, creature.getName()).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
        harness.assertNotOnBattlefield(player2, creature.getName());
        harness.assertNotInGraveyard(player2, creature.getName());
    }

    @Test
    void opponentsSpellsDoNotEnableSpellMastery() {
        Card creature = new LeafGilder();
        harness.setGraveyard(player2, List.of(creature, new SendToSleep(), new MacabreWaltz()));
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new NecromanticSummons()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(findPermanent(player1, creature.getName()).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    @Test
    void checksSpellMasteryAtResolution() {
        Card creature = new LeafGilder();
        harness.setGraveyard(player1, List.of(creature, new SendToSleep()));
        harness.setHand(player1, List.of(new NecromanticSummons()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0, creature.getId());

        harness.setGraveyard(player1, List.of(creature, new SendToSleep(), new MacabreWaltz()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, creature.getName()).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    void spellMasteryCountersArePresentWhenEntryTriggersAreChecked() {
        Card creature = new LeafGilder();
        Card drawnCard = new MacabreWaltz();
        harness.addToBattlefield(player1, new ElementalBond());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setGraveyard(player1, List.of(creature, new SendToSleep(), new MacabreWaltz()));
        harness.setHand(player1, List.of(new NecromanticSummons()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(findPermanent(player1, creature.getName()).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }
}
