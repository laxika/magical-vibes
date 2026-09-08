package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RallyThePeasants;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WashAway.class, GrizzlyBears.class, RallyThePeasants.class})
class WashAwayTest extends BaseCardTest {

    @Test
    @DisplayName("Normal cast cannot target a spell cast from its owner's hand")
    void normalCastCannotTargetSpellCastFromHand() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new WashAway()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("wasn't cast from its owner's hand");
    }

    @Test
    @DisplayName("Normal cast counters a spell cast from a graveyard")
    void normalCastCountersSpellCastFromGraveyard() {
        RallyThePeasants rally = new RallyThePeasants();
        harness.setGraveyard(player1, List.of(rally));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new WashAway()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castFlashback(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, rally.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Rally the Peasants");
        harness.assertInGraveyard(player2, "Wash Away");
    }

    @Test
    @DisplayName("Cleave cast counters a spell cast from its owner's hand")
    void cleaveCastCountersSpellCastFromHand() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new WashAway()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithAlternateCost(player2, 0, bears.getId(), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }
}
