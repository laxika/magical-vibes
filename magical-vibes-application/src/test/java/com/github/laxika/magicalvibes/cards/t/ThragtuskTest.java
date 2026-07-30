package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThragtuskTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield gains 5 life")
    void entryGainsFiveLife() {
        harness.setHand(player1, List.of(new Thragtusk()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // creature resolves, ETB trigger goes on stack
        harness.passBothPriorities(); // ETB trigger resolves

        harness.assertLife(player1, 25);
    }

    @Test
    @DisplayName("Leaving the battlefield creates a 3/3 green Beast token")
    void leavingCreatesBeastToken() {
        harness.addToBattlefield(player1, new Thragtusk());

        Permanent thragtusk = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof Thragtusk)
                .findFirst().orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, thragtusk));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // LTB trigger resolves

        List<Permanent> beasts = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.BEAST))
                .toList();

        assertThat(beasts).hasSize(1);
        assertThat(beasts.getFirst().getCard().getPower()).isEqualTo(3);
        assertThat(beasts.getFirst().getCard().getToughness()).isEqualTo(3);
    }
}
