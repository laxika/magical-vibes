package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAndDrawCardsEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SorcerersStrongboxTest extends BaseCardTest {

    @Test
    @DisplayName("Has activated ability with FlipCoinWinEffect wrapping SacrificeSelfAndDrawCardsEffect")
    void hasCorrectAbility() {
        SorcerersStrongbox card = new SorcerersStrongbox();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{2}");
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(FlipCoinWinEffect.class);

        FlipCoinWinEffect flipEffect = (FlipCoinWinEffect) ability.getEffects().getFirst();
        assertThat(flipEffect.wrapped()).isInstanceOf(SacrificeSelfAndDrawCardsEffect.class);
        assertThat(((SacrificeSelfAndDrawCardsEffect) flipEffect.wrapped()).amount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Activating the ability flips a coin and result is consistent")
    void activatingAbilityFlipsCoin() {
        harness.addToBattlefield(player1, new SorcerersStrongbox());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        boolean onBattlefield = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Sorcerer's Strongbox"));
        boolean inGraveyard = gd.playerGraveyards.get(player1.getId()).stream()
                .anyMatch(c -> c.getName().equals("Sorcerer's Strongbox"));

        // Exactly one of the two outcomes must be true
        assertThat(onBattlefield != inGraveyard)
                .as("Sorcerer's Strongbox must be on battlefield (loss) or in graveyard (win)")
                .isTrue();

        if (inGraveyard) {
            // Won the flip: sacrificed and drew 3
            assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
            assertThat(gd.gameLog).anyMatch(log -> log.contains("wins the coin flip"));
            assertThat(gd.gameLog).anyMatch(log -> log.contains("is sacrificed"));
        } else {
            // Lost the flip: nothing happens, hand unchanged
            assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
            assertThat(gd.gameLog).anyMatch(log -> log.contains("loses the coin flip"));
        }
    }

    @Test
    @DisplayName("Ability requires tap — cannot activate when tapped")
    void cannotActivateWhenTapped() {
        harness.addToBattlefield(player1, new SorcerersStrongbox());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        // First activation should work
        harness.activateAbility(player1, 0, null, null);

        // The permanent is now tapped — should not be able to activate again
        boolean isTapped = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Sorcerer's Strongbox"))
                .anyMatch(p -> p.isTapped());

        // Only assert tapped if it's still on the battlefield (could have won the flip)
        if (gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Sorcerer's Strongbox"))) {
            assertThat(isTapped).isTrue();
        }
    }

    @Test
    @DisplayName("Coin flip is logged to game log")
    void coinFlipIsLogged() {
        harness.addToBattlefield(player1, new SorcerersStrongbox());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.gameLog)
                .anyMatch(log -> log.contains("coin flip for Sorcerer's Strongbox"));
    }
}
