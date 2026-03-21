package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ExileCast;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SqueeTheImmortalTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has GraveyardCast casting option")
    void hasGraveyardCastOption() {
        SqueeTheImmortal card = new SqueeTheImmortal();

        assertThat(card.getCastingOption(GraveyardCast.class)).isPresent();
    }

    @Test
    @DisplayName("Has ExileCast casting option")
    void hasExileCastOption() {
        SqueeTheImmortal card = new SqueeTheImmortal();

        assertThat(card.getCastingOption(ExileCast.class)).isPresent();
    }

    // ===== Casting from hand =====

    @Test
    @DisplayName("Can cast from hand normally")
    void castFromHand() {
        harness.setHand(player1, List.of(new SqueeTheImmortal()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Squee, the Immortal");
    }

    @Test
    @DisplayName("Resolves onto battlefield from hand")
    void resolvesFromHand() {
        harness.setHand(player1, List.of(new SqueeTheImmortal()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Squee, the Immortal");
    }

    // ===== Casting from graveyard =====

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

    // ===== Casting from exile =====

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

        assertThat(gd.playerExiledCards.get(player1.getId())).isEmpty();
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
