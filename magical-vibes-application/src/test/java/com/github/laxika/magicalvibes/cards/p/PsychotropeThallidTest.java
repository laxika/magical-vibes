package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(PsychotropeThallid.class)
class PsychotropeThallidTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger adds a spore counter")
    void upkeepTriggerAddsSporeCounter() {
        Permanent psychotropeThallid = addPsychotropeThallid();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(psychotropeThallid.getCounterCount(CounterType.FUNGUS)).isOne();
    }

    @Test
    @DisplayName("Upkeep trigger does not fire during an opponent's upkeep")
    void upkeepTriggerDoesNotFireDuringOpponentsUpkeep() {
        Permanent psychotropeThallid = addPsychotropeThallid();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(psychotropeThallid.getCounterCount(CounterType.FUNGUS)).isZero();
    }

    @Test
    @DisplayName("Removing three spore counters creates a Saproling token")
    void removesThreeSporeCountersAndCreatesToken() {
        Permanent psychotropeThallid = addPsychotropeThallid();
        psychotropeThallid.setCounterCount(CounterType.FUNGUS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(psychotropeThallid.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }

    @Test
    @DisplayName("The token ability creates a 1/1 green Saproling creature token")
    void createdTokenHasSaprolingCharacteristics() {
        Permanent psychotropeThallid = addPsychotropeThallid();
        psychotropeThallid.setCounterCount(CounterType.FUNGUS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent saproling = findPermanent(player1, "Saproling");
        assertThat(saproling.getCard().isToken()).isTrue();
        assertThat(saproling.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(saproling.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(saproling.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
        assertThat(gqs.getEffectivePower(gd, saproling)).isOne();
        assertThat(gqs.getEffectiveToughness(gd, saproling)).isOne();
    }

    @Test
    @DisplayName("Removing three spore counters leaves additional counters")
    void removesExactlyThreeSporeCounters() {
        Permanent psychotropeThallid = addPsychotropeThallid();
        psychotropeThallid.setCounterCount(CounterType.FUNGUS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(psychotropeThallid.getCounterCount(CounterType.FUNGUS)).isOne();
    }

    @Test
    @DisplayName("The token ability requires three spore counters")
    void tokenAbilityRequiresThreeSporeCounters() {
        addPsychotropeThallid().setCounterCount(CounterType.FUNGUS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrificing a Saproling draws a card")
    void sacrificingSaprolingDrawsCard() {
        addSaproling();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of());
        PsychotropeThallid drawnCard = new PsychotropeThallid();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertNotOnBattlefield(player1, "Saproling");
        harness.assertNotInGraveyard(player1, "Saproling");
    }

    @Test
    @DisplayName("The draw ability requires one mana")
    void drawAbilityRequiresMana() {
        addSaproling();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }

    @Test
    @DisplayName("The draw ability requires a Saproling")
    void drawAbilityRequiresSaproling() {
        addPsychotropeThallid();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Spore counters are paid immediately and cannot fund a second activation")
    void sporeCountersArePaidBeforeResolution() {
        Permanent thallid = addPsychotropeThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isZero();
        harness.assertNotOnBattlefield(player1, "Saproling");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }

    @Test
    @DisplayName("Both abilities can be activated while tapped and summoning sick")
    void abilitiesDoNotRequireTappingOrHaste() {
        Permanent thallid = harness.addToBattlefieldAndReturn(player1, new PsychotropeThallid());
        thallid.setSummoningSick(true);
        thallid.setTapped(true);
        thallid.setCounterCount(CounterType.FUNGUS, 3);
        harness.setHand(player1, List.of());
        PsychotropeThallid drawnCard = new PsychotropeThallid();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Saproling");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(thallid.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's Saproling cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsSaproling() {
        Permanent saproling = addSaproling();
        gd.playerBattlefields.get(player1.getId()).remove(saproling);
        gd.playerBattlefields.get(player2.getId()).add(saproling);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Saproling");
    }

    private Permanent addPsychotropeThallid() {
        return addCreatureReady(player1, new PsychotropeThallid());
    }

    private Permanent addSaproling() {
        Permanent psychotropeThallid = addPsychotropeThallid();
        psychotropeThallid.setCounterCount(CounterType.FUNGUS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        return findPermanent(player1, "Saproling");
    }
}
