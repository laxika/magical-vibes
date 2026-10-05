package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OriginSpellbomb.class, Shatter.class})
class OriginSpellbombTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability creates a 1/1 Myr artifact creature token")
    void activateAbilityCreatesMyrToken() {
        harness.addToBattlefield(player1, new OriginSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        // Resolve the death trigger MayEffect (on top per CR 603.3)
        harness.passBothPriorities();

        // Decline death trigger
        harness.handleMayAbilityChosen(player1, false);

        // Resolve the token creation ability
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Myr")).isEqualTo(1);
    }

    @Test
    @DisplayName("Myr token has correct properties: 1/1 colorless artifact creature - Myr")
    void myrTokenHasCorrectProperties() {
        harness.addToBattlefield(player1, new OriginSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // Resolve death trigger MayEffect (on top per CR 603.3)
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities(); // Resolve token creation ability

        Permanent myrToken = findPermanent(player1, "Myr");

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
        harness.assertNotOnBattlefield(player1, "Origin Spellbomb");
        harness.assertInGraveyard(player1, "Origin Spellbomb");
    }

    @Test
    @DisplayName("Accepting death trigger and paying {W} draws a card")
    void acceptDeathTriggerDrawsCard() {
        harness.addToBattlefield(player1, new OriginSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        // Resolve the death trigger MayEffect (on top per CR 603.3)
        harness.passBothPriorities();

        // Accept death trigger - pay {W} (draw resolves inline)
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);

        // White mana should be spent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(0);

        // Resolve the token creation ability
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Declining death trigger does not draw a card")
    void declineDeathTriggerNoCard() {
        harness.addToBattlefield(player1, new OriginSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        // Resolve the death trigger MayEffect (on top per CR 603.3)
        harness.passBothPriorities();

        // Decline death trigger
        harness.handleMayAbilityChosen(player1, false);

        // No card drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);

        // White mana unspent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);

        // Resolve the token creation ability
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting death trigger without enough mana treats as decline")
    void acceptWithoutManaNoCard() {
        harness.addToBattlefield(player1, new OriginSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        // No white mana added

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        // Resolve the death trigger MayEffect (on top per CR 603.3)
        harness.passBothPriorities();

        // Accept but cannot pay {W}
        harness.handleMayAbilityChosen(player1, true);

        // No card drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);

        // Resolve the token creation ability
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Both abilities work: Myr token created AND controller draws a card")
    void bothAbilitiesWork() {
        harness.addToBattlefield(player1, new OriginSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        // Resolve the death trigger MayEffect (on top per CR 603.3)
        harness.passBothPriorities();

        // Accept death trigger - pay {W} to draw (resolves inline)
        harness.handleMayAbilityChosen(player1, true);

        // Card drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);

        // Resolve the token creation ability
        harness.passBothPriorities();

        // Myr token created
        assertThat(countPermanents(player1, "Myr")).isEqualTo(1);
    }

    @Test
    @DisplayName("Destruction triggers the draw for the spellbomb's controller without creating a Myr")
    void destructionTriggersDrawForController() {
        Permanent spellbomb = harness.addToBattlefieldAndReturn(player2, new OriginSpellbomb());
        harness.setHand(player1, List.of(new Shatter()));
        harness.setLibrary(player2, List.of(new OriginSpellbomb()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        int handSizeBefore = gd.playerHands.get(player2.getId()).size();

        harness.castAndResolveInstant(player1, 0, spellbomb.getId());
        harness.assertInGraveyard(player2, "Origin Spellbomb");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(countPermanents(player1, "Myr")).isZero();
        assertThat(countPermanents(player2, "Myr")).isZero();
    }

    @Test
    @DisplayName("A tapped spellbomb cannot activate its ability")
    void tappedSpellbombCannotActivate() {
        harness.addToBattlefieldAndReturn(player1, new OriginSpellbomb()).setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Origin Spellbomb");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The spellbomb cannot activate without paying its generic mana cost")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new OriginSpellbomb());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Origin Spellbomb");
        assertThat(gd.stack).isEmpty();
    }
}
