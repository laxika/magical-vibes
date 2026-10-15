package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.p.PhoenixDown;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheGoldSaucer.class, Spellbook.class, PhoenixDown.class})
class TheGoldSaucerTest extends BaseCardTest {

    @Test
    @DisplayName("First ability adds one colorless mana")
    void addsColorlessMana() {
        Permanent saucer = harness.addToBattlefieldAndReturn(player1, new TheGoldSaucer());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(saucer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Second ability creates a Treasure only when the coin flip is won")
    void coinFlipCreatesTreasureOnWin() {
        Permanent saucer = harness.addToBattlefieldAndReturn(player1, new TheGoldSaucer());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        boolean won = gameLogContains("wins the coin flip");
        boolean lost = gameLogContains("loses the coin flip");
        assertThat(won != lost).isTrue();
        assertThat(findPermanents(player1, "Treasure")).hasSize(won ? 1 : 0);
        assertThat(saucer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Third ability sacrifices two artifacts and draws a card")
    void sacrificesTwoArtifactsAndDraws() {
        harness.addToBattlefield(player1, new TheGoldSaucer());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spellbook")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Spellbook")))
                .hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Third ability cannot activate without two artifacts")
    void cannotSacrificeTwoArtifactsWithoutTwoArtifacts() {
        harness.addToBattlefield(player1, new TheGoldSaucer());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Drawing pays the tap and sacrifice costs before the card is drawn")
    void drawAbilityPaysCostsBeforeResolution() {
        Permanent saucer = harness.addToBattlefieldAndReturn(player1, new TheGoldSaucer());
        harness.addToBattlefield(player1, new PhoenixDown());
        harness.addToBattlefield(player1, new PhoenixDown());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new TheGoldSaucer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(saucer.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Phoenix Down");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        harness.assertInHand(player1, "The Gold Saucer");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The draw ability cannot activate when The Gold Saucer is tapped")
    void cannotDrawWhileTapped() {
        Permanent saucer = harness.addToBattlefieldAndReturn(player1, new TheGoldSaucer());
        harness.addToBattlefield(player1, new PhoenixDown());
        harness.addToBattlefield(player1, new PhoenixDown());
        saucer.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player1, "Phoenix Down")).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's artifact cannot pay the draw ability's sacrifice cost")
    void cannotSacrificeOpponentsArtifact() {
        harness.addToBattlefield(player1, new TheGoldSaucer());
        harness.addToBattlefield(player1, new PhoenixDown());
        harness.addToBattlefield(player2, new PhoenixDown());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Phoenix Down");
        harness.assertOnBattlefield(player2, "Phoenix Down");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("The coin flip uses the stack and consumes two mana before resolution")
    void coinFlipWaitsForResolution() {
        harness.addToBattlefield(player1, new TheGoldSaucer());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gameLogContains("coin flip for")).isFalse();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("coin flip for")).isTrue();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }
}
