package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.s.ShivanSandMage;
import com.github.laxika.magicalvibes.cards.r.RitesOfFlourishing;
import com.github.laxika.magicalvibes.cards.s.SlaughterPact;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LostAuramancers.class, RitesOfFlourishing.class, SlaughterPact.class, ShivanSandMage.class})
class LostAuramancersTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three time counters")
    void entersWithTimeCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new LostAuramancers(), "{2}{W}{W}");
        resolveAllTriggers();

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
        RitesOfFlourishing enchantment = new RitesOfFlourishing();
        harness.setLibrary(player1, List.of(enchantment, new SlaughterPact()));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(enchantment);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Lost Auramancers");
        harness.assertOnBattlefield(player1, "Rites of Flourishing");
    }

    @Test
    @DisplayName("Does not trigger the search while dying with a time counter")
    void noSearchWhileDyingWithTimeCounter() {
        Permanent auramancers = addReadyAuramancers();
        auramancers.setCounterCount(CounterType.TIME, 1);
        harness.setHand(player1, List.of(new SlaughterPact()));
        harness.setLibrary(player1, List.of(new RitesOfFlourishing()));

        harness.castAndResolveInstant(player1, 0, auramancers.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Rites of Flourishing"));
    }

    @Test
    @DisplayName("Declining the death search leaves the enchantment in the library")
    void declinesEnchantmentSearch() {
        addReadyAuramancers().setCounterCount(CounterType.TIME, 1);
        RitesOfFlourishing enchantment = new RitesOfFlourishing();
        harness.setLibrary(player1, List.of(enchantment));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(enchantment);
        harness.assertInGraveyard(player1, "Lost Auramancers");
        harness.assertNotOnBattlefield(player1, "Rites of Flourishing");
    }

    @Test
    @DisplayName("Accepting the death search with no enchantment finds nothing")
    void acceptsSearchWithNoEnchantment() {
        addReadyAuramancers().setCounterCount(CounterType.TIME, 1);
        SlaughterPact nonEnchantment = new SlaughterPact();
        harness.setLibrary(player1, List.of(nonEnchantment));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonEnchantment);
        harness.assertInGraveyard(player1, "Lost Auramancers");
        harness.assertNotOnBattlefield(player1, "Slaughter Pact");
    }

    @Test
    @DisplayName("Removing the last time counter with another spell triggers sacrifice and search")
    void externalLastCounterRemovalTriggersSacrifice() {
        Permanent auramancers = addReadyAuramancers();
        auramancers.setCounterCount(CounterType.TIME, 1);
        harness.setLibrary(player1, List.of(new RitesOfFlourishing()));
        harness.setHand(player1, List.of(new ShivanSandMage()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0, 0, auramancers.getId());
        resolveAllTriggers();

        assertThat(auramancers.getCounterCount(CounterType.TIME)).isZero();

        harness.assertInGraveyard(player1, "Lost Auramancers");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Rites of Flourishing");
    }

    @Test
    @DisplayName("A counterless Auramancers does not trigger during upkeep")
    void counterlessAuramancersDoesNotTriggerDuringUpkeep() {
        Permanent auramancers = addReadyAuramancers();

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(auramancers);
    }

    @Test
    @DisplayName("Does not remove time counters during the opponent's upkeep")
    void opponentUpkeepDoesNotRemoveCounter() {
        Permanent auramancers = addReadyAuramancers();
        auramancers.setCounterCount(CounterType.TIME, 2);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(auramancers.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(auramancers);
    }

    @Test
    @DisplayName("Dying without counters offers the search even when vanishing did not sacrifice it")
    void counterlessDeathFromDestructionTriggersSearch() {
        Permanent auramancers = addReadyAuramancers();
        harness.setHand(player1, List.of(new SlaughterPact()));
        harness.setLibrary(player1, List.of(new RitesOfFlourishing()));

        harness.castAndResolveInstant(player1, 0, auramancers.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Lost Auramancers");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Rites of Flourishing");
    }

    private Permanent addReadyAuramancers() {
        return addCreatureReady(player1, new LostAuramancers());
    }
}
