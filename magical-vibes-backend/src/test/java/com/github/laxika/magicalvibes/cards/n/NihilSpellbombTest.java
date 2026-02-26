package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NihilSpellbombTest extends BaseCardTest {

    // ===== Activated ability: exile target player's graveyard =====

    @Test
    @DisplayName("Activating ability exiles target player's graveyard")
    void activateAbilityExilesGraveyard() {
        harness.addToBattlefield(player1, new NihilSpellbomb());
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new Shock()));

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);

        harness.activateAbility(player1, 0, null, player2.getId());

        // Death trigger prompt should fire first
        harness.handleMayAbilityChosen(player1, false);

        // Resolve the exile ability
        harness.passBothPriorities();

        // Graveyard should be empty
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        // Cards should be in exile
        assertThat(gd.playerExiledCards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Activating ability sacrifices the spellbomb")
    void activateAbilitySacrificesSpellbomb() {
        harness.addToBattlefield(player1, new NihilSpellbomb());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player2.getId());

        // Spellbomb should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Nihil Spellbomb"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Nihil Spellbomb"));
    }

    @Test
    @DisplayName("Can target own graveyard")
    void canTargetOwnGraveyard() {
        harness.addToBattlefield(player1, new NihilSpellbomb());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player1.getId());

        // Death trigger prompt
        harness.handleMayAbilityChosen(player1, false);

        // Resolve
        harness.passBothPriorities();

        // Entire graveyard is exiled (including the spellbomb which was sacrificed as cost)
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Nihil Spellbomb"));
    }

    @Test
    @DisplayName("Works when target player's graveyard is empty")
    void worksOnEmptyGraveyard() {
        harness.addToBattlefield(player1, new NihilSpellbomb());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        harness.activateAbility(player1, 0, null, player2.getId());

        // Death trigger prompt
        harness.handleMayAbilityChosen(player1, false);

        // Resolve - should not error
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    // ===== Death trigger: may pay {B} to draw =====

    @Test
    @DisplayName("Accepting death trigger and paying {B} draws a card")
    void acceptDeathTriggerDrawsCard() {
        harness.addToBattlefield(player1, new NihilSpellbomb());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());

        // Accept death trigger - pay {B}
        harness.handleMayAbilityChosen(player1, true);

        // Draw triggered ability should be on stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);

        // Resolve draw triggered ability
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);

        // Black mana should be spent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
    }

    @Test
    @DisplayName("Declining death trigger does not draw a card")
    void declineDeathTriggerNoCard() {
        harness.addToBattlefield(player1, new NihilSpellbomb());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());

        // Decline death trigger
        harness.handleMayAbilityChosen(player1, false);

        // Resolve exile ability
        harness.passBothPriorities();

        // No card drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);

        // Black mana unspent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Accepting death trigger without enough mana treats as decline")
    void acceptWithoutManaNoCard() {
        harness.addToBattlefield(player1, new NihilSpellbomb());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        // No black mana added

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());

        // Accept but cannot pay {B}
        harness.handleMayAbilityChosen(player1, true);

        // Resolve exile ability
        harness.passBothPriorities();

        // No card drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    // ===== Both abilities interact correctly =====

    @Test
    @DisplayName("Both abilities work: graveyard exiled AND controller draws a card")
    void bothAbilitiesWork() {
        harness.addToBattlefield(player1, new NihilSpellbomb());
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());

        // Accept death trigger - pay {B} to draw
        harness.handleMayAbilityChosen(player1, true);

        // Resolve draw (top of stack)
        harness.passBothPriorities();

        // Resolve exile (next on stack)
        harness.passBothPriorities();

        // Card drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);

        // Graveyard exiled
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerExiledCards.get(player2.getId())).hasSize(2);
    }
}
