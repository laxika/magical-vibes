package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PanicSpellbombTest extends BaseCardTest {

    // ===== Activated ability: target creature can't block =====

    @Test
    @DisplayName("Activating ability sacrifices spellbomb and prompts death trigger")
    void activateAbilitySacrificesAndPromptsMayAbility() {
        harness.addToBattlefield(player1, new PanicSpellbomb());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = findPermanent(player2, "Grizzly Bears").getId();

        harness.activateAbility(player1, 0, null, bearsId);

        // Spellbomb should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Panic Spellbomb"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Panic Spellbomb"));

        // Resolve the death trigger MayEffect (on top of stack per CR 603.3)
        harness.passBothPriorities();

        // Death trigger may ability should prompt
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Target creature can't block after ability resolves")
    void targetCreatureCantBlock() {
        harness.addToBattlefield(player1, new PanicSpellbomb());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = findPermanent(player2, "Grizzly Bears").getId();

        harness.activateAbility(player1, 0, null, bearsId);

        // Resolve the death trigger MayEffect (on top per CR 603.3)
        harness.passBothPriorities();

        // Decline the death trigger draw
        harness.handleMayAbilityChosen(player1, false);

        // Resolve the can't-block ability
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Can target own creature")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new PanicSpellbomb());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = findPermanent(player1, "Grizzly Bears").getId();

        harness.activateAbility(player1, 0, null, bearsId);

        // Resolve the death trigger MayEffect (on top per CR 603.3)
        harness.passBothPriorities();

        // Decline the death trigger draw
        harness.handleMayAbilityChosen(player1, false);

        // Resolve the can't-block ability
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.isCantBlockThisTurn()).isTrue();
    }

    // ===== Death trigger: may pay {R} to draw =====

    @Test
    @DisplayName("Accepting death trigger and paying {R} draws a card")
    void acceptDeathTriggerDrawsCard() {
        harness.addToBattlefield(player1, new PanicSpellbomb());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        UUID bearsId = findPermanent(player2, "Grizzly Bears").getId();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, bearsId);

        // Resolve the death trigger MayEffect (on top per CR 603.3)
        harness.passBothPriorities();

        // Accept death trigger — pay {R} (draw resolves inline)
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);

        // Red mana should be spent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);

        // Resolve the can't-block ability
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Declining death trigger does not draw a card")
    void declineDeathTriggerNoCard() {
        harness.addToBattlefield(player1, new PanicSpellbomb());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        UUID bearsId = findPermanent(player2, "Grizzly Bears").getId();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, bearsId);

        // Resolve the death trigger MayEffect (on top per CR 603.3)
        harness.passBothPriorities();

        // Decline death trigger
        harness.handleMayAbilityChosen(player1, false);

        // No card drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);

        // Red mana should not be spent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);

        // Resolve the can't-block ability
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting death trigger without enough mana treats as decline")
    void acceptWithoutManaNoCard() {
        harness.addToBattlefield(player1, new PanicSpellbomb());
        harness.addToBattlefield(player2, new GrizzlyBears());
        // No red mana added
        UUID bearsId = findPermanent(player2, "Grizzly Bears").getId();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, bearsId);

        // Resolve the death trigger MayEffect (on top per CR 603.3)
        harness.passBothPriorities();

        // Accept but cannot pay {R}
        harness.handleMayAbilityChosen(player1, true);

        // No card drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);

        // Resolve the can't-block ability
        harness.passBothPriorities();
    }

    // ===== Both abilities interact correctly =====

    @Test
    @DisplayName("Both abilities work: creature can't block AND controller draws a card")
    void bothAbilitiesWork() {
        harness.addToBattlefield(player1, new PanicSpellbomb());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        UUID bearsId = findPermanent(player2, "Grizzly Bears").getId();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, bearsId);

        // Resolve the death trigger MayEffect (on top per CR 603.3)
        harness.passBothPriorities();

        // Accept death trigger — pay {R} to draw (resolves inline)
        harness.handleMayAbilityChosen(player1, true);

        // Card drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);

        // Resolve the can't-block ability
        harness.passBothPriorities();

        // Creature can't block
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.isCantBlockThisTurn()).isTrue();
    }

    private Permanent findPermanent(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(name + " not found"));
    }
}
