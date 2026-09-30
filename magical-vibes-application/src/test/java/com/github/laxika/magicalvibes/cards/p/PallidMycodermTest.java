package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.Stingscourger;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PallidMycoderm.class, Stingscourger.class})
class PallidMycodermTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger adds a spore counter")
    void upkeepTriggerAddsSporeCounter() {
        Permanent mycoderm = addMycoderm();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(mycoderm.getCounterCount(CounterType.FUNGUS)).isOne();
    }

    @Test
    @DisplayName("Removing three spore counters creates a Saproling token")
    void removesThreeSporeCountersAndCreatesToken() {
        Permanent mycoderm = addMycoderm();
        mycoderm.setCounterCount(CounterType.FUNGUS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mycoderm.getCounterCount(CounterType.FUNGUS)).isOne();
        Permanent saproling = findPermanent(player1, "Saproling");
        assertThat(saproling.getCard().isToken()).isTrue();
        assertThat(saproling.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(saproling.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(saproling.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
        assertThat(gqs.getEffectivePower(gd, saproling)).isOne();
        assertThat(gqs.getEffectiveToughness(gd, saproling)).isOne();
    }

    @Test
    @DisplayName("Sacrificing a Saproling boosts Funguses but not other creatures")
    void sacrificingSaprolingBoostsFungusesOnly() {
        Permanent mycoderm = addMycoderm();
        Permanent otherCreature = addCreatureReady(player1, new Stingscourger());
        mycoderm.setCounterCount(CounterType.FUNGUS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThat(mycoderm.getPowerModifier()).isOne();
        assertThat(mycoderm.getToughnessModifier()).isOne();
        assertThat(otherCreature.getPowerModifier()).isZero();
        assertThat(otherCreature.getToughnessModifier()).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mycoderm.getPowerModifier()).isZero();
        assertThat(mycoderm.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost affects controlled Funguses and Saprolings, not an opponent's Fungus")
    void boostAffectsControlledFungusesAndSaprolingsOnly() {
        Permanent mycoderm = addMycoderm();
        Permanent otherFungus = addCreatureReady(player1, new PallidMycoderm());
        Permanent otherCreature = addCreatureReady(player1, new Stingscourger());
        Permanent opponentFungus = addCreatureReady(player2, new PallidMycoderm());
        mycoderm.setCounterCount(CounterType.FUNGUS, 6);

        int mycodermIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mycoderm);
        harness.activateAbility(player1, mycodermIndex, 0, null, null);
        harness.passBothPriorities();
        mycodermIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mycoderm);
        harness.activateAbility(player1, mycodermIndex, 0, null, null);
        harness.passBothPriorities();

        List<Permanent> saprolings = findPermanents(player1, "Saproling");
        assertThat(saprolings).hasSize(2);
        Permanent sacrificedSaproling = saprolings.getFirst();
        Permanent remainingSaproling = saprolings.get(1);

        mycodermIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mycoderm);
        harness.activateAbility(player1, mycodermIndex, 1, null, null);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, sacrificedSaproling.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).containsExactly(remainingSaproling);
        assertThat(mycoderm.getPowerModifier()).isOne();
        assertThat(mycoderm.getToughnessModifier()).isOne();
        assertThat(otherFungus.getPowerModifier()).isOne();
        assertThat(otherFungus.getToughnessModifier()).isOne();
        assertThat(remainingSaproling.getPowerModifier()).isOne();
        assertThat(remainingSaproling.getToughnessModifier()).isOne();
        assertThat(otherCreature.getPowerModifier()).isZero();
        assertThat(otherCreature.getToughnessModifier()).isZero();
        assertThat(opponentFungus.getPowerModifier()).isZero();
        assertThat(opponentFungus.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost ability requires a Saproling to sacrifice")
    void boostAbilityRequiresSaproling() {
        addMycoderm();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addMycoderm() {
        return addCreatureReady(player1, new PallidMycoderm());
    }
}
