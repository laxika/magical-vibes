package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlightSpellbombTest extends BaseCardTest {

    // ===== Activated ability: grant flying =====

    @Test
    @DisplayName("Activating ability sacrifices spellbomb and prompts death trigger")
    void activateAbilitySacrificesAndPromptsMayAbility() {
        harness.addToBattlefield(player1, new FlightSpellbomb());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = findPermanent(player1, "Grizzly Bears").getId();

        harness.activateAbility(player1, 0, null, bearsId);

        // Spellbomb should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Flight Spellbomb"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Flight Spellbomb"));

        // Resolve death trigger MayPayManaEffect (on top per CR 603.3) — shows may prompt
        harness.passBothPriorities();

        // Death trigger may ability should prompt
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Target creature gains flying after ability resolves")
    void targetCreatureGainsFlying() {
        harness.addToBattlefield(player1, new FlightSpellbomb());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = findPermanent(player1, "Grizzly Bears").getId();

        harness.activateAbility(player1, 0, null, bearsId);

        // Resolve death trigger MayPayManaEffect (on top per CR 603.3)
        harness.passBothPriorities();

        // Decline the death trigger draw
        harness.handleMayAbilityChosen(player1, false);

        // Resolve flying ability
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Can target opponent's creature with flying")
    void canTargetOpponentCreature() {
        harness.addToBattlefield(player1, new FlightSpellbomb());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = findPermanent(player2, "Grizzly Bears").getId();

        harness.activateAbility(player1, 0, null, bearsId);

        // Resolve death trigger MayPayManaEffect (on top per CR 603.3)
        harness.passBothPriorities();

        // Decline the death trigger draw
        harness.handleMayAbilityChosen(player1, false);

        // Resolve flying ability
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();
    }

    // ===== Death trigger: may pay {U} to draw =====

    @Test
    @DisplayName("Accepting death trigger and paying {U} draws a card")
    void acceptDeathTriggerDrawsCard() {
        harness.addToBattlefield(player1, new FlightSpellbomb());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        UUID bearsId = findPermanent(player1, "Grizzly Bears").getId();

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
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        UUID bearsId = findPermanent(player1, "Grizzly Bears").getId();

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
        harness.addToBattlefield(player1, new GrizzlyBears());
        // No blue mana added
        UUID bearsId = findPermanent(player1, "Grizzly Bears").getId();

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

    // ===== Both abilities interact correctly =====

    @Test
    @DisplayName("Both abilities work: creature gains flying AND controller draws a card")
    void bothAbilitiesWork() {
        harness.addToBattlefield(player1, new FlightSpellbomb());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        UUID bearsId = findPermanent(player1, "Grizzly Bears").getId();

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
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();
    }

    private Permanent findPermanent(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(name + " not found"));
    }
}
