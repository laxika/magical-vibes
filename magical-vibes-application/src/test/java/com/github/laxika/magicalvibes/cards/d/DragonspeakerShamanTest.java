package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonspeakerShaman.class, DragonMage.class, GoblinBrigand.class})
class DragonspeakerShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Dragon spells cost {2} less with Dragonspeaker Shaman on the battlefield")
    void dragonSpellsCostTwoLess() {
        harness.addToBattlefield(player1, new DragonspeakerShaman());
        harness.setHand(player1, List.of(new DragonMage()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Non-Dragon spells are not reduced")
    void nonDragonSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new DragonspeakerShaman());
        harness.setHand(player1, List.of(new GoblinBrigand()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Dragon cost reduction does not apply to an opponent")
    void dragonSpellsCastByOpponentAreNotReduced() {
        harness.addToBattlefield(player1, new DragonspeakerShaman());
        harness.setHand(player2, List.of(new DragonMage()));
        harness.addMana(player2, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Dragon cost reduction does not reduce colored mana requirements")
    void dragonCostReductionDoesNotReduceColoredRequirements() {
        harness.addToBattlefield(player1, new DragonspeakerShaman());
        harness.setHand(player1, List.of(new DragonMage()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multiple Dragonspeaker Shamans stack their cost reductions")
    void multipleDragonspeakerShamansStackTheirCostReductions() {
        harness.addToBattlefield(player1, new DragonspeakerShaman());
        harness.addToBattlefield(player1, new DragonspeakerShaman());
        harness.setHand(player1, List.of(new DragonMage()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }
}
