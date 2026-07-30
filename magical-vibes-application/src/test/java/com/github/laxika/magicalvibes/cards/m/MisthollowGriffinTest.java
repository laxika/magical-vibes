package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MisthollowGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Resolves onto the battlefield when cast from hand")
    void castFromHand() {
        harness.setHand(player1, List.of(new MisthollowGriffin()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Misthollow Griffin");
    }

    @Test
    @DisplayName("Can be cast from exile and leaves the exile zone")
    void castFromExile() {
        MisthollowGriffin griffin = new MisthollowGriffin();
        harness.setExile(player1, List.of(griffin));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromExile(player1, griffin.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Misthollow Griffin");
    }

    @Test
    @DisplayName("Casting from exile requires sorcery-speed timing")
    void exileCastRequiresSorceryTiming() {
        MisthollowGriffin griffin = new MisthollowGriffin();
        harness.setExile(player1, List.of(griffin));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player1, griffin.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
