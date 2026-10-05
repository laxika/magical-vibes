package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.h.HexgoldSlash;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.cards.s.SwoopingLookout;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NornsWellspring.class, SwoopingLookout.class, HexgoldSlash.class})
class NornsWellspringTest extends BaseCardTest {

    @Test
    @DisplayName("An ally creature's death scries before adding an oil counter")
    void allyDeathScriesBeforeAddingOilCounter() {
        Permanent wellspring = addReadyWellspring(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SwoopingLookout());
        Card topCard = gd.playerDecks.get(player1.getId()).getFirst();

        killWithSlash(player2, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(wellspring.getCounterCount(CounterType.OIL)).isZero();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        assertThat(wellspring.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's creature dying does not trigger")
    void opponentDeathDoesNotTrigger() {
        Permanent wellspring = addReadyWellspring(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SwoopingLookout());

        killWithSlash(player1, creature.getId());

        assertThat(wellspring.getCounterCount(CounterType.OIL)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("Removing two oil counters and paying one mana draws a card")
    void removingOilCountersDrawsCard() {
        Permanent wellspring = addReadyWellspring(player1);
        wellspring.setCounterCount(CounterType.OIL, 2);
        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(wellspring.getCounterCount(CounterType.OIL)).isZero();
        assertThat(wellspring.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    @DisplayName("The draw ability cannot be activated while tapped")
    void drawAbilityRequiresUntappedWellspring() {
        Permanent wellspring = addReadyWellspring(player1);
        wellspring.setCounterCount(CounterType.OIL, 2);
        wellspring.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("One oil counter cannot pay the draw ability's cost")
    void drawAbilityRequiresTwoOilCounters() {
        Permanent wellspring = addReadyWellspring(player1);
        wellspring.setCounterCount(CounterType.OIL, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(wellspring.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(wellspring.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Two oil counters do not replace the one-mana cost")
    void drawAbilityRequiresMana() {
        Permanent wellspring = addReadyWellspring(player1);
        wellspring.setCounterCount(CounterType.OIL, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(wellspring.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(wellspring.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Scrying an empty library still adds an oil counter")
    void emptyLibraryStillAddsOilCounter() {
        Permanent wellspring = addReadyWellspring(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SwoopingLookout());
        harness.setLibrary(player1, List.of());

        killWithSlash(player2, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(wellspring.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    @DisplayName("Putting the scried card on the bottom still adds an oil counter")
    void bottomingScriedCardStillAddsOilCounter() {
        Permanent wellspring = addReadyWellspring(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SwoopingLookout());
        Card topCard = new SwoopingLookout();
        Card secondCard = new HexgoldSlash();
        harness.setLibrary(player1, List.of(topCard, secondCard));

        killWithSlash(player2, creature.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard, topCard);
        assertThat(wellspring.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    @CardUsed({MarchOfTheMachines.class})
    @DisplayName("An animated Wellspring dying triggers its own scry ability")
    void animatedWellspringTriggersForItsOwnDeath() {
        Permanent wellspring = addReadyWellspring(player1);
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        Card topCard = new SwoopingLookout();
        Card secondCard = new HexgoldSlash();
        harness.setLibrary(player1, List.of(topCard, secondCard));

        killWithSlash(player2, wellspring.getId());
        harness.assertInGraveyard(player1, "Norn's Wellspring");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard, topCard);
        assertThat(wellspring.getCounterCount(CounterType.OIL)).isZero();
    }

    private Permanent addReadyWellspring(Player player) {
        return addCreatureReady(player, new NornsWellspring());
    }

    private void killWithSlash(Player caster, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new HexgoldSlash()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castAndResolveInstant(caster, 0, targetId);
    }
}
