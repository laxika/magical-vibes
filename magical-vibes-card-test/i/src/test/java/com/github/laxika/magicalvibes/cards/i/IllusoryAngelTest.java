package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.StatuteOfDenial;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IllusoryAngel.class, RuneclawBear.class, StatuteOfDenial.class, Island.class})
class IllusoryAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast after its controller casts another spell")
    void castableAfterAnotherSpell() {
        harness.setHand(player1, List.of(new RuneclawBear(), new IllusoryAngel()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(IllusoryAngel.class);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof IllusoryAngel);
    }

    @Test
    @DisplayName("Cannot be cast without another spell cast this turn")
    void notCastableWithoutAnotherSpell() {
        harness.setHand(player1, List.of(new IllusoryAngel()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("An opponent's spell does not satisfy the cast condition")
    void opponentSpellDoesNotEnableCast() {
        harness.setHand(player2, List.of(new RuneclawBear()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new IllusoryAngel()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A countered spell still satisfies the cast condition")
    void counteredSpellEnablesCast() {
        RuneclawBear bear = new RuneclawBear();
        harness.setHand(player1, List.of(bear, new IllusoryAngel()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setHand(player2, List.of(new StatuteOfDenial()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bear.getId());
        harness.assertInGraveyard(player1, "Runeclaw Bear");
        assertThat(gd.stack).isEmpty();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Illusory Angel");
    }

    @Test
    @DisplayName("Playing a land does not satisfy the cast condition")
    void landPlayDoesNotEnableCast() {
        harness.setHand(player1, List.of(new Island(), new IllusoryAngel()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Putting a creature onto the battlefield does not satisfy the cast condition")
    void creatureEnteringWithoutBeingCastDoesNotEnableCast() {
        harness.enterBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new IllusoryAngel()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A spell cast on an earlier turn does not satisfy the cast condition")
    void earlierTurnSpellDoesNotEnableCast() {
        harness.setHand(player1, List.of(new RuneclawBear(), new IllusoryAngel()));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setLibrary(player2, List.of(new Island(), new Island(), new Island()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
