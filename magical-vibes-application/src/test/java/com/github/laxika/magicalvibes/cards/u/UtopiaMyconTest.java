package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(UtopiaMycon.class)
class UtopiaMyconTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger adds a spore counter")
    void upkeepTriggerAddsSporeCounter() {
        Permanent mycon = addMycon();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(mycon.getCounterCount(CounterType.FUNGUS)).isOne();
    }

    @Test
    @DisplayName("Upkeep trigger does not add a counter during the opponent's upkeep")
    void upkeepTriggerOnlyWorksDuringControllerUpkeep() {
        Permanent mycon = addMycon();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(mycon.getCounterCount(CounterType.FUNGUS)).isZero();
    }

    @Test
    @DisplayName("Removing three spore counters creates a Saproling token")
    void removesThreeSporeCountersAndCreatesToken() {
        Permanent mycon = addMycon();
        mycon.setCounterCount(CounterType.FUNGUS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mycon.getCounterCount(CounterType.FUNGUS)).isOne();
        Permanent saproling = findPermanent(player1, "Saproling");
        assertThat(saproling.getEffectivePower()).isOne();
        assertThat(saproling.getEffectiveToughness()).isOne();
        assertThat(saproling.getEffectiveColor()).isEqualTo(CardColor.GREEN);
        assertThat(saproling.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
    }

    @Test
    @DisplayName("The token ability requires three spore counters")
    void tokenAbilityRequiresThreeSporeCounters() {
        addMycon().setCounterCount(CounterType.FUNGUS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrificing a Saproling adds one mana of the chosen color")
    void sacrificingSaprolingAddsChosenColorMana() {
        Permanent mycon = addMycon();
        mycon.setCounterCount(CounterType.FUNGUS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isOne();
    }

    @Test
    @DisplayName("The mana ability requires a Saproling to sacrifice")
    void manaAbilityRequiresSaproling() {
        addMycon();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The mana ability cannot sacrifice a non-Saproling creature")
    void manaAbilityCannotSacrificeNonSaprolingCreature() {
        addMycon();
        addMycon();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Spore counters are paid immediately while the token waits for resolution")
    void countersArePaidBeforeTokenResolves() {
        Permanent mycon = addMycon();
        mycon.setCounterCount(CounterType.FUNGUS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(mycon.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }

    @Test
    @DisplayName("Both abilities work while tapped and summoning sick, and the mana ability uses no stack")
    void abilitiesWorkWhileTappedAndSummoningSick() {
        Permanent mycon = addMycon();
        mycon.setSummoningSick(true);
        mycon.tap();
        mycon.setCounterCount(CounterType.FUNGUS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent saproling = findPermanent(player1, "Saproling");
        assertThat(saproling.isSummoningSick()).isTrue();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isOne();
        assertThat(gd.stack).isEmpty();
        assertThat(mycon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's Saproling cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsSaproling() {
        addMycon();
        Permanent opposingMycon = addCreatureReady(player2, new UtopiaMycon());
        opposingMycon.setCounterCount(CounterType.FUNGUS, 3);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player2, "Saproling")).hasSize(1);
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
    }
    private Permanent addMycon() {
        return addCreatureReady(player1, new UtopiaMycon());
    }
}
