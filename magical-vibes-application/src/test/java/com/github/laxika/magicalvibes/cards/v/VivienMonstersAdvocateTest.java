package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VivienMonstersAdvocate.class, GrizzlyBears.class, LlanowarElves.class, Shock.class})
class VivienMonstersAdvocateTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast a creature spell from the top of the library")
    void castsCreatureFromLibraryTop() {
        addReadyVivien(3);
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castFromLibraryTop(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Cannot cast a noncreature spell from the top of the library")
    void cannotCastNoncreatureFromLibraryTop() {
        addReadyVivien(3);
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(shock));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(shock);
    }

    @Test
    @DisplayName("Plus one creates a Beast with a reach counter when chosen")
    void plusOneCreatesBeastWithReachCounter() {
        Permanent vivien = addReadyVivien(3);

        activatePlusOneAndChoose("Reach");

        Permanent beast = findPermanent(player1, "Beast");
        assertThat(beast.getCounterCount(CounterType.REACH)).isEqualTo(1);
        assertThat(beast.getCounterCount(CounterType.VIGILANCE)).isZero();
        assertThat(beast.getCounterCount(CounterType.TRAMPLE)).isZero();
        assertThat(beast.getEffectivePower()).isEqualTo(3);
        assertThat(beast.getEffectiveToughness()).isEqualTo(3);
        assertThat(vivien.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Plus one creates a Beast with a vigilance counter when chosen")
    void plusOneCreatesBeastWithVigilanceCounter() {
        addReadyVivien(3);

        activatePlusOneAndChoose("Vigilance");

        Permanent beast = findPermanent(player1, "Beast");
        assertThat(beast.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
        assertThat(beast.getCounterCount(CounterType.REACH)).isZero();
        assertThat(beast.getCounterCount(CounterType.TRAMPLE)).isZero();
    }

    @Test
    @DisplayName("Plus one creates a Beast with a trample counter when chosen")
    void plusOneCreatesBeastWithTrampleCounter() {
        addReadyVivien(3);

        activatePlusOneAndChoose("Trample");

        Permanent beast = findPermanent(player1, "Beast");
        assertThat(beast.getCounterCount(CounterType.TRAMPLE)).isEqualTo(1);
        assertThat(beast.getCounterCount(CounterType.REACH)).isZero();
        assertThat(beast.getCounterCount(CounterType.VIGILANCE)).isZero();
    }

    @Test
    @DisplayName("Minus two puts a creature with lesser mana value onto the battlefield for the next creature cast")
    void minusTwoSearchesForLesserManaValueCreatureOnce() {
        addReadyVivien(3);
        LlanowarElves cheaperCreature = new LlanowarElves();
        GrizzlyBears equalManaValueCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(cheaperCreature, equalManaValueCreature));
        GrizzlyBears creatureToCast = new GrizzlyBears();
        harness.setHand(player1, List.of(creatureToCast));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(cheaperCreature);

        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerDecks.get(player1.getId())).contains(equalManaValueCreature);
    }

    @Test
    @DisplayName("Minus two triggers only for the next creature spell")
    void minusTwoTriggersOnlyOnce() {
        addReadyVivien(3);
        LlanowarElves cheaperCreature = new LlanowarElves();
        harness.setLibrary(player1, List.of(cheaperCreature));
        GrizzlyBears firstCreature = new GrizzlyBears();
        GrizzlyBears secondCreature = new GrizzlyBears();
        harness.setHand(player1, List.of(firstCreature, secondCreature));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .hasSize(2);
    }

    private Permanent addReadyVivien(int loyalty) {
        Permanent vivien = harness.addToBattlefieldAndReturn(player1, new VivienMonstersAdvocate());
        vivien.setCounterCount(CounterType.LOYALTY, loyalty);
        vivien.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return vivien;
    }

    private void activatePlusOneAndChoose(String choice) {
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, choice);
    }
}
