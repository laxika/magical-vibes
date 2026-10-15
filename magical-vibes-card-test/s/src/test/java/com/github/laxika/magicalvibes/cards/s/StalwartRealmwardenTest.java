package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StalwartRealmwarden.class, GrizzlyBears.class, Opt.class})
class StalwartRealmwardenTest extends BaseCardTest {

    @Test
    @DisplayName("taxes the targeted opponent's next noncreature spell")
    void taxesTargetedOpponentsNextNoncreatureSpell() {
        harness.setHand(player1, List.of(new StalwartRealmwarden()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(harness.getGameData().currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Opt()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("does not tax creature spells and is consumed only once")
    void ignoresCreatureSpellsAndIsConsumedOnce() {
        harness.setHand(player1, List.of(new StalwartRealmwarden()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(harness.getGameData().currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears(), new Opt(), new Opt()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0);
        harness.castInstant(player2, 0);
        assertThat(gd.stack).hasSize(2);
    }
}
