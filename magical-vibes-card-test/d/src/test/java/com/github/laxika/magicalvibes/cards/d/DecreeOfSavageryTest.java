package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.ScornfulEgotist;
import com.github.laxika.magicalvibes.cards.t.TempleOfTheFalseGod;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DecreeOfSavagery.class, ScornfulEgotist.class, TempleOfTheFalseGod.class})
class DecreeOfSavageryTest extends BaseCardTest {

    @Test
    @DisplayName("Casting it puts four +1/+1 counters on each creature you control")
    void castingPutsCountersOnEachControlledCreature() {
        Permanent ownFirst = harness.addToBattlefieldAndReturn(player1, new ScornfulEgotist());
        Permanent ownSecond = harness.addToBattlefieldAndReturn(player1, new ScornfulEgotist());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new TempleOfTheFalseGod());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ScornfulEgotist());

        harness.castFromHand(player1, new DecreeOfSavagery(), "{7}{G}{G}");
        harness.passBothPriorities();

        assertThat(ownFirst.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(ownSecond.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(ownLand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cycling may put four +1/+1 counters on any target creature and draws a card")
    void cyclingMayPutCountersOnTargetCreatureAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ScornfulEgotist());
        harness.setHand(player1, List.of(new DecreeOfSavagery()));
        harness.setLibrary(player1, List.of(new ScornfulEgotist()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        harness.assertInGraveyard(player1, "Decree of Savagery");
        harness.assertInHand(player1, "Scornful Egotist");
    }

    @Test
    @DisplayName("Declining the cycling trigger still draws a card")
    void decliningCyclingTriggerStillDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ScornfulEgotist());
        harness.setHand(player1, List.of(new DecreeOfSavagery()));
        harness.setLibrary(player1, List.of(new ScornfulEgotist()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Decree of Savagery");
        harness.assertInHand(player1, "Scornful Egotist");
    }

    @Test
    @DisplayName("Cycling without a creature target still draws a card")
    void cyclingWithoutCreatureTargetStillDraws() {
        harness.addToBattlefield(player2, new TempleOfTheFalseGod());
        harness.setHand(player1, List.of(new DecreeOfSavagery()));
        harness.setLibrary(player1, List.of(new ScornfulEgotist()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Decree of Savagery");
        harness.assertInHand(player1, "Scornful Egotist");
    }

    @Test
    @DisplayName("The spell affects creatures present at resolution and adds to existing counters")
    void spellUsesCreaturesAtResolution() {
        harness.castFromHand(player1, new DecreeOfSavagery(), "{7}{G}{G}");
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ScornfulEgotist());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        harness.assertInGraveyard(player1, "Decree of Savagery");
    }

    @Test
    @DisplayName("A cycling trigger whose target leaves does not prevent the cycling draw")
    void removedCyclingTargetStillAllowsDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ScornfulEgotist());
        harness.setHand(player1, List.of(new DecreeOfSavagery()));
        harness.setLibrary(player1, List.of(new ScornfulEgotist()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        harness.assertNotInHand(player1, "Scornful Egotist");
        harness.assertInGraveyard(player1, "Decree of Savagery");
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInHand(player1, "Scornful Egotist");
    }

    private void addCyclingMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);
    }
}
