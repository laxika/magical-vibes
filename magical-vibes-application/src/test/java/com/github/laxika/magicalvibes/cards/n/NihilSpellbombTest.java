package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
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
        // Stack: [ExileGraveyard (bottom), MayPayMana death trigger (top)]
        // Per CR 603.3, death triggers from sacrifice resolve first

        harness.passBothPriorities(); // resolve MayPayMana death trigger -> may prompt
        harness.handleMayAbilityChosen(player1, false); // decline death trigger

        harness.passBothPriorities(); // resolve ExileGraveyard ability

        // Graveyard should be empty
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        // Cards should be in exile
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
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
        // Stack: [ExileGraveyard (bottom), MayPayMana death trigger (top)]

        harness.passBothPriorities(); // resolve MayPayMana death trigger -> may prompt
        harness.handleMayAbilityChosen(player1, false); // decline

        harness.passBothPriorities(); // resolve ExileGraveyard

        // Entire graveyard is exiled (including the spellbomb which was sacrificed as cost)
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Nihil Spellbomb"));
    }

    @Test
    @DisplayName("Works when target player's graveyard is empty")
    void worksOnEmptyGraveyard() {
        harness.addToBattlefield(player1, new NihilSpellbomb());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        harness.activateAbility(player1, 0, null, player2.getId());
        // Stack: [ExileGraveyard (bottom), MayPayMana death trigger (top)]

        harness.passBothPriorities(); // resolve MayPayMana death trigger -> may prompt
        harness.handleMayAbilityChosen(player1, false); // decline

        harness.passBothPriorities(); // resolve ExileGraveyard - should not error

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
        // Stack: [ExileGraveyard (bottom), MayPayMana death trigger (top)]

        harness.passBothPriorities(); // resolve MayPayMana death trigger -> may prompt

        // Accept death trigger - pay {B}, inner DrawCardEffect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);

        // Black mana should be spent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);

        harness.passBothPriorities(); // resolve ExileGraveyard
    }

    @Test
    @DisplayName("Declining death trigger does not draw a card")
    void declineDeathTriggerNoCard() {
        harness.addToBattlefield(player1, new NihilSpellbomb());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        // Stack: [ExileGraveyard (bottom), MayPayMana death trigger (top)]

        harness.passBothPriorities(); // resolve MayPayMana death trigger -> may prompt

        // Decline death trigger
        harness.handleMayAbilityChosen(player1, false);

        // No card drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);

        // Black mana unspent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);

        harness.passBothPriorities(); // resolve ExileGraveyard
    }

    @Test
    @DisplayName("Accepting death trigger without enough mana treats as decline")
    void acceptWithoutManaNoCard() {
        harness.addToBattlefield(player1, new NihilSpellbomb());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        // No black mana added

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        // Stack: [ExileGraveyard (bottom), MayPayMana death trigger (top)]

        harness.passBothPriorities(); // resolve MayPayMana death trigger -> may prompt

        // Accept but cannot pay {B} — treated as decline
        harness.handleMayAbilityChosen(player1, true);

        // No card drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);

        harness.passBothPriorities(); // resolve ExileGraveyard
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
        // Stack: [ExileGraveyard (bottom), MayPayMana death trigger (top)]

        harness.passBothPriorities(); // resolve MayPayMana death trigger -> may prompt

        // Accept death trigger - pay {B} to draw, inner DrawCardEffect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        // Card drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);

        harness.passBothPriorities(); // resolve ExileGraveyard

        // Graveyard exiled
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }
}
