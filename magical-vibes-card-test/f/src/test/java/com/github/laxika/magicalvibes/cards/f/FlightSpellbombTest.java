package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlightSpellbomb.class, CarapaceForger.class})
class FlightSpellbombTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability sacrifices spellbomb and prompts death trigger")
    void activateAbilitySacrificesAndPromptsMayAbility() {
        harness.addToBattlefield(player1, new FlightSpellbomb());
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new CarapaceForger()).getId();

        harness.activateAbility(player1, 0, null, bearsId);

        // Spellbomb should be sacrificed
        harness.assertNotOnBattlefield(player1, "Flight Spellbomb");
        harness.assertInGraveyard(player1, "Flight Spellbomb");

        // Resolve death trigger MayPayManaEffect (on top per CR 603.3) — shows may prompt
        harness.passBothPriorities();

        // Death trigger may ability should prompt
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Target creature gains flying after ability resolves")
    void targetCreatureGainsFlying() {
        harness.addToBattlefield(player1, new FlightSpellbomb());
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new CarapaceForger()).getId();

        harness.activateAbility(player1, 0, null, bearsId);

        // Resolve death trigger MayPayManaEffect (on top per CR 603.3)
        harness.passBothPriorities();

        // Decline the death trigger draw
        harness.handleMayAbilityChosen(player1, false);

        // Resolve flying ability
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Carapace Forger");
        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Can target opponent's creature with flying")
    void canTargetOpponentCreature() {
        harness.addToBattlefield(player1, new FlightSpellbomb());
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new CarapaceForger()).getId();

        harness.activateAbility(player1, 0, null, bearsId);

        // Resolve death trigger MayPayManaEffect (on top per CR 603.3)
        harness.passBothPriorities();

        // Decline the death trigger draw
        harness.handleMayAbilityChosen(player1, false);

        // Resolve flying ability
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Carapace Forger");
        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Accepting death trigger and paying {U} draws a card")
    void acceptDeathTriggerDrawsCard() {
        harness.addToBattlefield(player1, new FlightSpellbomb());
        harness.addToBattlefield(player1, new CarapaceForger());
        harness.addMana(player1, ManaColor.BLUE, 1);
        UUID bearsId = harness.getPermanentId(player1, "Carapace Forger");

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, bearsId);

        // Resolve death trigger MayPayManaEffect (on top per CR 603.3)
        harness.passBothPriorities();

        // Accept death trigger — pay {U}, draw resolves inline
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);

        // Blue mana should be spent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(0);

        // Resolve flying ability
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Declining death trigger does not draw a card")
    void declineDeathTriggerNoCard() {
        harness.addToBattlefield(player1, new FlightSpellbomb());
        harness.addToBattlefield(player1, new CarapaceForger());
        harness.addMana(player1, ManaColor.BLUE, 1);
        UUID bearsId = harness.getPermanentId(player1, "Carapace Forger");

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, bearsId);

        // Resolve death trigger MayPayManaEffect (on top per CR 603.3)
        harness.passBothPriorities();

        // Decline death trigger
        harness.handleMayAbilityChosen(player1, false);

        // No card drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);

        // Blue mana should not be spent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);

        // Resolve flying ability
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting death trigger without enough mana treats as decline")
    void acceptWithoutManaNoCard() {
        harness.addToBattlefield(player1, new FlightSpellbomb());
        harness.addToBattlefield(player1, new CarapaceForger());
        // No blue mana added
        UUID bearsId = harness.getPermanentId(player1, "Carapace Forger");

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, bearsId);

        // Resolve death trigger MayPayManaEffect (on top per CR 603.3)
        harness.passBothPriorities();

        // Accept but cannot pay {U} — auto-treated as decline
        harness.handleMayAbilityChosen(player1, true);

        // No card drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);

        // Resolve flying ability
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Both abilities work: creature gains flying AND controller draws a card")
    void bothAbilitiesWork() {
        harness.addToBattlefield(player1, new FlightSpellbomb());
        harness.addToBattlefield(player1, new CarapaceForger());
        harness.addMana(player1, ManaColor.BLUE, 1);
        UUID bearsId = harness.getPermanentId(player1, "Carapace Forger");

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, bearsId);

        // Resolve death trigger MayPayManaEffect (on top per CR 603.3)
        harness.passBothPriorities();

        // Accept death trigger — pay {U} to draw, resolves inline
        harness.handleMayAbilityChosen(player1, true);

        // Card drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);

        // Resolve flying ability
        harness.passBothPriorities();

        // Creature has flying
        Permanent bears = findPermanent(player1, "Carapace Forger");
        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying expires at end of turn after the source is sacrificed")
    void flyingExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new FlightSpellbomb());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CarapaceForger());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @CardUsed({Shatter.class})
    @DisplayName("Destruction triggers the draw without activating the Spellbomb")
    void destructionTriggersDraw() {
        harness.addToBattlefield(player1, new FlightSpellbomb());
        UUID spellbombId = harness.getPermanentId(player1, "Flight Spellbomb");
        harness.setLibrary(player1, List.of(new CarapaceForger()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, spellbombId);
        harness.assertInGraveyard(player1, "Flight Spellbomb");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Carapace Forger");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

}
