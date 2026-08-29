package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TocasiasOnuletTest extends BaseCardTest {

    @Test
    @DisplayName("Leaving the battlefield by bounce makes its controller gain 2 life")
    void leavingBattlefieldGainsLife() {
        Permanent onABattlefield = harness.addToBattlefieldAndReturn(player1, new TocasiasOnulet());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        int lifeBefore = gd.getLife(player1.getId());

        harness.castInstant(player1, 0, onABattlefield.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Tocasia's Onulet");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Unearth returns it with haste and exiles it at the next end step")
    void unearthReturnsWithHasteAndExilesAtEndStep() {
        harness.setGraveyard(player1, List.of(new TocasiasOnulet()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent onABattlefield = findPermanent(player1, "Tocasia's Onulet");
        assertThat(onABattlefield.getGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player1, "Tocasia's Onulet");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tocasia's Onulet");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Tocasia's Onulet"));
    }
}
