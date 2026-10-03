package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.SanitariumSkeleton;
import com.github.laxika.magicalvibes.cards.r.RavensCrime;
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

@CardUsed({AlchemistsGreeting.class, RavensCrime.class, SanitariumSkeleton.class})
class AlchemistsGreetingTest extends BaseCardTest {

    /** Force player1 to discard Alchemist's Greeting via Raven's Crime from player2. */
    private AlchemistsGreeting discardViaRavensCrime() {
        AlchemistsGreeting greeting = new AlchemistsGreeting();
        harness.setHand(player1, List.of(greeting));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        return greeting;
    }

    @Test
    @DisplayName("Deals 4 damage, killing a small creature")
    void killsSmallCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SanitariumSkeleton());
        harness.setHand(player1, List.of(new AlchemistsGreeting()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Sanitarium Skeleton");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new AlchemistsGreeting()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Discarding Alchemist's Greeting exiles it and offers madness cast")
    void discardTriggersMadness() {
        AlchemistsGreeting greeting = discardViaRavensCrime();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(greeting.getId()));
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getLast().getDescription()).contains("madness");

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Declining madness cast puts the card into the graveyard")
    void decliningMadnessGoesToGraveyard() {
        AlchemistsGreeting greeting = discardViaRavensCrime();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(greeting.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(greeting.getId()));
    }

    @Test
    @DisplayName("Accepting madness cast pays {1}{R} and deals 4 damage to target creature")
    void acceptingMadnessDealsDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SanitariumSkeleton());
        discardViaRavensCrime();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Sanitarium Skeleton");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Alchemist's Greeting");
    }

    @Test
    @DisplayName("Deals exactly 4 damage to a surviving creature you control")
    void dealsExactlyFourDamageToOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SanitariumSkeleton());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new AlchemistsGreeting()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertOnBattlefield(player1, "Sanitarium Skeleton");
        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertInGraveyard(player1, "Alchemist's Greeting");
    }

    @Test
    @DisplayName("An unpayable madness cast puts the card into the graveyard")
    void unpayableMadnessGoesToGraveyard() {
        harness.addToBattlefield(player2, new SanitariumSkeleton());
        AlchemistsGreeting greeting = discardViaRavensCrime();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(greeting.getId()));
        harness.assertInGraveyard(player1, "Alchemist's Greeting");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Madness without a legal target puts the card into the graveyard without spending mana")
    void madnessWithoutTargetsDoesNotSpendMana() {
        AlchemistsGreeting greeting = discardViaRavensCrime();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(greeting.getId()));
        harness.assertInGraveyard(player1, "Alchemist's Greeting");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }
}
