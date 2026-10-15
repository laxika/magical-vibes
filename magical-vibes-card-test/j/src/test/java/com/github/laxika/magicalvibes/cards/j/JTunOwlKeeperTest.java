package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.s.Sunscour;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JTunOwlKeeper.class, Sunscour.class})
class JTunOwlKeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Cumulative upkeep can be paid with blue mana")
    void cumulativeUpkeepCanBePaidWithBlueMana() {
        Permanent owlKeeper = harness.addToBattlefieldAndReturn(player1, new JTunOwlKeeper());
        owlKeeper.setCounterCount(CounterType.AGE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(owlKeeper.getCounterCount(CounterType.AGE)).isEqualTo(2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(owlKeeper);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Cumulative upkeep can be paid with white mana")
    void cumulativeUpkeepCanBePaidWithWhiteMana() {
        Permanent owlKeeper = harness.addToBattlefieldAndReturn(player1, new JTunOwlKeeper());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(owlKeeper.getCounterCount(CounterType.AGE)).isEqualTo(1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(owlKeeper);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Death trigger creates one flying Bird for each age counter")
    void deathCreatesBirdsForAgeCountersOnly() {
        Permanent owlKeeper = harness.addToBattlefieldAndReturn(player1, new JTunOwlKeeper());
        owlKeeper.setCounterCount(CounterType.AGE, 3);
        owlKeeper.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.castFromHand(player1, new Sunscour(), "{5}{W}{W}");
        resolveAllTriggers();

        List<Permanent> birds = findPermanents(player1, "Bird");
        assertThat(birds).hasSize(3);
        assertThat(birds).allSatisfy(bird -> {
            assertThat(bird.getCard().getPower()).isEqualTo(1);
            assertThat(bird.getCard().getToughness()).isEqualTo(1);
            assertThat(bird.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(bird.getCard().getSubtypes()).contains(CardSubtype.BIRD);
            assertThat(bird.getCard().getKeywords()).contains(Keyword.FLYING);
        });
    }

    @Test
    @DisplayName("Cumulative upkeep triggers only during its controller's upkeep")
    void cumulativeUpkeepTriggersOnlyDuringControllersUpkeep() {
        Permanent owlKeeper = harness.addToBattlefieldAndReturn(player1, new JTunOwlKeeper());

        advanceToUpkeep(player2);

        assertThat(owlKeeper.getCounterCount(CounterType.AGE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(owlKeeper);
    }

    @Test
    @DisplayName("Cumulative upkeep sacrifices Jötun Owl Keeper when its cost cannot be paid")
    void cannotPayCumulativeUpkeepSacrificesOwlKeeper() {
        Permanent owlKeeper = harness.addToBattlefieldAndReturn(player1, new JTunOwlKeeper());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(owlKeeper);
        harness.assertInGraveyard(player1, "Jötun Owl Keeper");
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Jötun Owl Keeper")
    void decliningCumulativeUpkeepSacrificesOwlKeeper() {
        Permanent owlKeeper = harness.addToBattlefieldAndReturn(player1, new JTunOwlKeeper());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(owlKeeper);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(owlKeeper.getCard());
    }

    @Test
    @DisplayName("Each age counter can be paid with a different permitted color")
    void cumulativeUpkeepCanBePaidWithMixedMana() {
        Permanent owlKeeper = harness.addToBattlefieldAndReturn(player1, new JTunOwlKeeper());
        owlKeeper.setCounterCount(CounterType.AGE, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(owlKeeper.getCounterCount(CounterType.AGE)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(owlKeeper);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(countPermanents(player1, "Bird")).isZero();
    }

    @Test
    @DisplayName("An insufficient upkeep payment consumes no mana and creates Birds after sacrifice")
    void insufficientPaymentCreatesBirdsWithoutPartialPayment() {
        Permanent owlKeeper = harness.addToBattlefieldAndReturn(player1, new JTunOwlKeeper());
        owlKeeper.setCounterCount(CounterType.AGE, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(owlKeeper);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(owlKeeper.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Bird")).isEqualTo(3);
        assertThat(countPermanents(player2, "Bird")).isZero();
    }

    @Test
    @DisplayName("Declining upkeep creates a Bird for the newly added age counter")
    void decliningFirstUpkeepCreatesOneBird() {
        Permanent owlKeeper = harness.addToBattlefieldAndReturn(player1, new JTunOwlKeeper());

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(owlKeeper);
        assertThat(countPermanents(player1, "Bird")).isEqualTo(1);
    }

    @Test
    @DisplayName("Dying without age counters creates no Birds")
    void deathWithoutAgeCountersCreatesNoBirds() {
        Permanent owlKeeper = harness.addToBattlefieldAndReturn(player1, new JTunOwlKeeper());
        owlKeeper.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.castFromHand(player1, new Sunscour(), "{5}{W}{W}");
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(owlKeeper.getCard());
        assertThat(countPermanents(player1, "Bird")).isZero();
    }
}
