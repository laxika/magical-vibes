package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.SpringsageRitual;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Terrarion.class, SpringsageRitual.class})
class TerrarionTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.castFromHand(player1, new Terrarion(), "{1}");
        harness.passBothPriorities();

        Permanent terrarion = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(terrarion.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing it adds two independently chosen mana and draws a card")
    void activationAddsManaAndDraws() {
        harness.addToBattlefield(player1, new Terrarion());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "RED");

        harness.assertNotOnBattlefield(player1, "Terrarion");
        harness.assertInGraveyard(player1, "Terrarion");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Can add both mana as the same color")
    void activationCanAddBothManaAsSameColor() {
        harness.addToBattlefield(player1, new Terrarion());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Enters tapped when put onto the battlefield without casting")
    void entersTappedWithoutCasting() {
        Permanent terrarion = harness.enterBattlefieldAndReturn(player1, new Terrarion());

        assertThat(terrarion.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate while tapped and does not sacrifice or spend mana")
    void cannotActivateWhileTapped() {
        harness.castFromHand(player1, new Terrarion(), "{1}");
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Terrarion");
        harness.assertNotInGraveyard(player1, "Terrarion");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate with less than two mana")
    void cannotActivateWithInsufficientMana() {
        harness.addToBattlefield(player1, new Terrarion());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Terrarion");
        harness.assertNotInGraveyard(player1, "Terrarion");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mana is available immediately but drawing waits for the graveyard trigger")
    void manaResolvesBeforeDrawTrigger() {
        harness.addToBattlefield(player1, new Terrarion());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Terrarion()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "WHITE");
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Destruction draws for Terrarion's controller without activating its mana ability")
    void destructionDrawsForController() {
        Permanent terrarion = harness.addToBattlefieldAndReturn(player2, new Terrarion());
        terrarion.tap();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Terrarion()));
        harness.setHand(player1, List.of(new SpringsageRitual()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, terrarion.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Terrarion");
        harness.assertInGraveyard(player2, "Terrarion");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
    }
}
