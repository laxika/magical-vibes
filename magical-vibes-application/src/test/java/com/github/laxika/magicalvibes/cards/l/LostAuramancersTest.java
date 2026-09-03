package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LostAuramancers.class, WrathOfGod.class})
class LostAuramancersTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three time counters")
    void entersWithTimeCounters() {
        harness.setHand(player1, List.of(new LostAuramancers()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent auramancers = findPermanent(player1, "Lost Auramancers");
        assertThat(auramancers.getCounterCount(CounterType.TIME)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removes a time counter during its controller's upkeep")
    void removesTimeCounterDuringUpkeep() {
        Permanent auramancers = addReadyAuramancers();
        auramancers.setCounterCount(CounterType.TIME, 2);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(auramancers.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(auramancers);
    }

    @Test
    @DisplayName("Sacrificing it after its last time counter is removed offers an enchantment search")
    void lastTimeCounterTriggersEnchantmentSearch() {
        addReadyAuramancers().setCounterCount(CounterType.TIME, 1);
        Card enchantment = enchantment("Test Enchantment");
        harness.setLibrary(player1, List.of(enchantment, creature("Test Creature")));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(enchantment);
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInGraveyard(player1, "Lost Auramancers");
        harness.assertOnBattlefield(player1, "Test Enchantment");
    }

    @Test
    @DisplayName("Does not trigger the search while dying with a time counter")
    void noSearchWhileDyingWithTimeCounter() {
        addReadyAuramancers().setCounterCount(CounterType.TIME, 1);
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.setLibrary(player1, List.of(enchantment("Test Enchantment")));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Test Enchantment"));
    }

    private Permanent addReadyAuramancers() {
        return addCreatureReady(player1, new LostAuramancers());
    }

    private Card enchantment(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ENCHANTMENT);
        return card;
    }

    private Card creature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        return card;
    }
}
