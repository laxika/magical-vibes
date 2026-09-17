package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DaruSpiritualist;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MistformWarchief.class, DaruSpiritualist.class})
class MistformWarchiefTest extends BaseCardTest {

    @Test
    @DisplayName("Creature spells sharing this creature's type cost {1} less")
    void reducesCreatureSpellsSharingType() {
        harness.addToBattlefield(player1, new MistformWarchief());
        harness.setHand(player1, List.of(new MistformWarchief()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(MistformWarchief.class);
    }

    @Test
    @DisplayName("Creature spells without a shared type are not reduced")
    void doesNotReduceCreatureSpellsWithoutSharedType() {
        harness.addToBattlefield(player1, new MistformWarchief());
        harness.setHand(player1, List.of(new DaruSpiritualist()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Changing this creature's type changes which creature spells are reduced")
    void usesCurrentCreatureTypeForCostReduction() {
        addCreatureReady(player1, new MistformWarchief());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.CLERIC.name());

        harness.setHand(player1, List.of(new DaruSpiritualist()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(DaruSpiritualist.class);
    }

    @Test
    @DisplayName("Changing this creature's type stops reducing spells of its former type")
    void changedCreatureTypeNoLongerMatchesFormerType() {
        addCreatureReady(player1, new MistformWarchief());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.CLERIC.name());

        harness.setHand(player1, List.of(new MistformWarchief()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The temporary creature type no longer affects cost reduction after end of turn")
    void changedCreatureTypeWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new MistformWarchief());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.CLERIC.name());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new DaruSpiritualist()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction does not apply to matching creature spells controlled by an opponent")
    void doesNotReduceOpponentCreatureSpells() {
        harness.addToBattlefield(player1, new MistformWarchief());
        harness.setHand(player2, List.of(new MistformWarchief()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
