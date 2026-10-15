package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SqueeTheImmortal.class})
class SqueeTheImmortalTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast from hand normally")
    void castFromHand() {
        harness.castFromHand(player1, new SqueeTheImmortal(), "{1}{R}{R}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Squee, the Immortal");
    }

    @Test
    void graveyardCastRequiresFullManaCost() {
        SqueeTheImmortal squee = new SqueeTheImmortal();
        harness.setGraveyard(player1, List.of(squee));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Squee, the Immortal");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, 0);
        harness.assertNotInGraveyard(player1, "Squee, the Immortal");
        assertThat(gd.stack.getFirst().getCard()).isSameAs(squee);
    }

    @Test
    void exileCastRequiresRedManaAndKeepsCardInExileOnFailure() {
        SqueeTheImmortal squee = new SqueeTheImmortal();
        harness.setExile(player1, List.of(squee));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromExile(player1, squee.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(squee);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.castFromExile(player1, squee.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack.getFirst().getCard()).isSameAs(squee);
    }

    @Test
    void cannotCastOpponentsSqueeFromExileWithoutSeparatePermission() {
        SqueeTheImmortal squee = new SqueeTheImmortal();
        harness.setExile(player2, List.of(squee));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, squee.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(squee);
    }

    @Test
    void graveyardCastRequiresEmptyStack() {
        harness.castFromHand(player1, new SqueeTheImmortal(), "{1}{R}{R}");
        SqueeTheImmortal squee = new SqueeTheImmortal();
        harness.setGraveyard(player1, List.of(squee));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player1, "Squee, the Immortal");
    }

    @Test
    void exileCastRequiresEmptyStack() {
        harness.castFromHand(player1, new SqueeTheImmortal(), "{1}{R}{R}");
        SqueeTheImmortal squee = new SqueeTheImmortal();
        harness.setExile(player1, List.of(squee));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, squee.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(squee);
    }

    @Test
    @DisplayName("Resolves onto battlefield from hand")
    void resolvesFromHand() {
        harness.castFromHand(player1, new SqueeTheImmortal(), "{1}{R}{R}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Squee, the Immortal");
    }

    @Test
    @DisplayName("Can cast from graveyard")
    void castFromGraveyard() {
        SqueeTheImmortal squee = new SqueeTheImmortal();
        harness.setGraveyard(player1, List.of(squee));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromGraveyard(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Squee, the Immortal");
    }

    @Test
    @DisplayName("Resolves onto battlefield from graveyard")
    void resolvesFromGraveyard() {
        SqueeTheImmortal squee = new SqueeTheImmortal();
        harness.setGraveyard(player1, List.of(squee));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Squee, the Immortal");
    }

    @Test
    @DisplayName("Casting from graveyard requires sorcery-speed timing")
    void graveyardCastRequiresSorceryTiming() {
        SqueeTheImmortal squee = new SqueeTheImmortal();
        harness.setGraveyard(player1, List.of(squee));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can cast from exile")
    void castFromExile() {
        SqueeTheImmortal squee = new SqueeTheImmortal();
        harness.setExile(player1, List.of(squee));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromExile(player1, squee.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Squee, the Immortal");
    }

    @Test
    @DisplayName("Resolves onto battlefield from exile")
    void resolvesFromExile() {
        SqueeTheImmortal squee = new SqueeTheImmortal();
        harness.setExile(player1, List.of(squee));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromExile(player1, squee.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Squee, the Immortal");
    }

    @Test
    @DisplayName("Casting from exile does not require permission")
    void castFromExileNoPermissionNeeded() {
        SqueeTheImmortal squee = new SqueeTheImmortal();
        harness.setExile(player1, List.of(squee));
        // Do NOT set any exilePlayPermissions — ExileCast should bypass permission
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Should not throw
        harness.castFromExile(player1, squee.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Squee, the Immortal");
    }

    @Test
    @DisplayName("Casting from exile removes card from exile zone")
    void castFromExileRemovesFromExileZone() {
        SqueeTheImmortal squee = new SqueeTheImmortal();
        harness.setExile(player1, List.of(squee));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromExile(player1, squee.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Casting from exile requires sorcery-speed timing")
    void exileCastRequiresSorceryTiming() {
        SqueeTheImmortal squee = new SqueeTheImmortal();
        harness.setExile(player1, List.of(squee));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player1, squee.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
