package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OriginSpellbombTest extends BaseCardTest {

    // ===== Activated ability: create a 1/1 Myr artifact creature token =====

    @Test
    @DisplayName("Activating ability creates a 1/1 Myr artifact creature token")
    void activateAbilityCreatesMyrToken() {
        harness.addToBattlefield(player1, new OriginSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        // Death trigger prompt fires first
        harness.handleMayAbilityChosen(player1, false);

        // Resolve the token creation ability
        harness.passBothPriorities();

        assertThat(countMyrTokens()).isEqualTo(1);
    }

    @Test
    @DisplayName("Myr token has correct properties: 1/1 colorless artifact creature - Myr")
    void myrTokenHasCorrectProperties() {
        harness.addToBattlefield(player1, new OriginSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        Permanent myrToken = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Myr"))
                .findFirst().orElseThrow();

        assertThat(myrToken.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(myrToken.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(myrToken.getCard().getSubtypes()).contains(CardSubtype.MYR);
        assertThat(myrToken.getCard().getPower()).isEqualTo(1);
        assertThat(myrToken.getCard().getToughness()).isEqualTo(1);
        assertThat(myrToken.getCard().getColor()).isNull();
    }

    @Test
    @DisplayName("Activating ability sacrifices the spellbomb")
    void activateAbilitySacrificesSpellbomb() {
        harness.addToBattlefield(player1, new OriginSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        // Spellbomb should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Origin Spellbomb"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Origin Spellbomb"));
    }

    // ===== Death trigger: may pay {W} to draw =====

    @Test
    @DisplayName("Accepting death trigger and paying {W} draws a card")
    void acceptDeathTriggerDrawsCard() {
        harness.addToBattlefield(player1, new OriginSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        // Accept death trigger - pay {W}
        harness.handleMayAbilityChosen(player1, true);

        // Draw triggered ability should be on stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);

        // Resolve draw triggered ability
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);

        // White mana should be spent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Declining death trigger does not draw a card")
    void declineDeathTriggerNoCard() {
        harness.addToBattlefield(player1, new OriginSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        // Decline death trigger
        harness.handleMayAbilityChosen(player1, false);

        // Resolve token creation ability
        harness.passBothPriorities();

        // No card drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);

        // White mana unspent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Accepting death trigger without enough mana treats as decline")
    void acceptWithoutManaNoCard() {
        harness.addToBattlefield(player1, new OriginSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        // No white mana added

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        // Accept but cannot pay {W}
        harness.handleMayAbilityChosen(player1, true);

        // Resolve token creation ability
        harness.passBothPriorities();

        // No card drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    // ===== Both abilities interact correctly =====

    @Test
    @DisplayName("Both abilities work: Myr token created AND controller draws a card")
    void bothAbilitiesWork() {
        harness.addToBattlefield(player1, new OriginSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        // Accept death trigger - pay {W} to draw
        harness.handleMayAbilityChosen(player1, true);

        // Resolve draw (top of stack)
        harness.passBothPriorities();

        // Resolve token creation (next on stack)
        harness.passBothPriorities();

        // Card drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);

        // Myr token created
        assertThat(countMyrTokens()).isEqualTo(1);
    }

    // ===== Helper methods =====

    private int countMyrTokens() {
        return (int) gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Myr"))
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.MYR))
                .count();
    }
}
