package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(SunbeamSpellbomb.class)
class SunbeamSpellbombTest extends BaseCardTest {

    @Test
    @DisplayName("Paying white mana and sacrificing it gains 5 life")
    void whiteAbilityGainsLife() {
        harness.addToBattlefield(player1, new SunbeamSpellbomb());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertNotOnBattlefield(player1, "Sunbeam Spellbomb");
        harness.assertInGraveyard(player1, "Sunbeam Spellbomb");
    }

    @Test
    @DisplayName("Sacrifice is paid when the white ability is activated")
    void sacrificeIsPaidAsActivationCost() {
        harness.addToBattlefield(player1, new SunbeamSpellbomb());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Sunbeam Spellbomb");
        harness.assertInGraveyard(player1, "Sunbeam Spellbomb");

        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Paying one mana and sacrificing it draws a card")
    void colorlessAbilityDrawsCard() {
        harness.addToBattlefield(player1, new SunbeamSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertNotOnBattlefield(player1, "Sunbeam Spellbomb");
        harness.assertInGraveyard(player1, "Sunbeam Spellbomb");
    }

    @Test
    @DisplayName("The white ability requires white mana")
    void whiteAbilityRequiresWhiteMana() {
        harness.addToBattlefield(player1, new SunbeamSpellbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Sunbeam Spellbomb");
    }

    @Test
    @DisplayName("The draw ability accepts colored mana and draws only on resolution")
    void drawAbilityAcceptsColoredManaAndUsesStack() {
        harness.addToBattlefield(player1, new SunbeamSpellbomb());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new SunbeamSpellbomb(), new SunbeamSpellbomb()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Sunbeam Spellbomb");
        harness.assertInGraveyard(player1, "Sunbeam Spellbomb");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Sunbeam Spellbomb");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A tapped Spellbomb can gain life, and life is gained only on resolution")
    void tappedSpellbombCanGainLife() {
        harness.addToBattlefieldAndReturn(player1, new SunbeamSpellbomb()).tap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertLife(player1, 10);
        harness.assertInGraveyard(player1, "Sunbeam Spellbomb");

        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 10);
    }

    @Test
    @DisplayName("The draw ability cannot be activated without mana")
    void drawAbilityRequiresMana() {
        harness.addToBattlefield(player1, new SunbeamSpellbomb());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Sunbeam Spellbomb");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
