package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BriselaVoiceOfNightmaresTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent can't cast a spell with mana value 3 or less")
    void blocksOpponentLowManaValueSpell() {
        harness.addToBattlefield(player1, new BriselaVoiceOfNightmares());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player2, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Opponent can cast a spell with mana value 4 or greater")
    void allowsOpponentHighManaValueSpell() {
        harness.addToBattlefield(player1, new BriselaVoiceOfNightmares());
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
    }

    @Test
    @DisplayName("Controller can still cast low mana value spells")
    void controllerUnrestricted() {
        harness.addToBattlefield(player1, new BriselaVoiceOfNightmares());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Opponent creature spell with mana value 3 or less is blocked")
    void blocksOpponentLowManaValueCreature() {
        harness.addToBattlefield(player1, new BriselaVoiceOfNightmares());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Opponent creature spell with mana value 4 is still castable")
    void allowsOpponentHighManaValueCreature() {
        harness.addToBattlefield(player1, new BriselaVoiceOfNightmares());
        harness.setHand(player2, List.of(new HillGiant()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Opponent X spell is blocked when chosen X makes mana value 3 or less")
    void blocksOpponentXSpellWithLowX() {
        harness.addToBattlefield(player1, new BriselaVoiceOfNightmares());
        harness.setHand(player2, List.of(new Hurricane()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Hurricane {X}{G}: X=2 → mana value 3 — illegal under Brisela
        assertThatThrownBy(() -> harness.castSorcery(player2, 0, 2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Opponent X spell is allowed when chosen X makes mana value 4 or greater")
    void allowsOpponentXSpellWithHighX() {
        harness.addToBattlefield(player1, new BriselaVoiceOfNightmares());
        harness.setHand(player2, List.of(new Hurricane()));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Hurricane {X}{G}: X=3 → mana value 4 — legal under Brisela
        harness.castSorcery(player2, 0, 3);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
    }
}
