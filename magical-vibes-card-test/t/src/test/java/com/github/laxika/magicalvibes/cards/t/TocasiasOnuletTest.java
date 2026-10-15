package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TocasiasOnulet.class, Unsummon.class})
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
    @DisplayName("Dying triggers life gain for its controller")
    void dyingGainsLife() {
        Permanent onulet = harness.addToBattlefieldAndReturn(player1, new TocasiasOnulet());
        int lifeBefore = gd.getLife(player1.getId());

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, onulet);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Tocasia's Onulet");
        harness.assertLife(player1, lifeBefore + 2);
    }

    @Test
    @DisplayName("Bouncing an unearthed Onulet exiles it and triggers life gain")
    void bouncingUnearthedOnuletExilesAndGainsLife() {
        harness.setGraveyard(player1, List.of(new TocasiasOnulet()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Tocasia's Onulet"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tocasia's Onulet");
        harness.assertNotInHand(player1, "Tocasia's Onulet");
        harness.assertNotInGraveyard(player1, "Tocasia's Onulet");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Tocasia's Onulet"));
        harness.assertLife(player1, lifeBefore + 2);
    }

    @Test
    @DisplayName("Unearth cannot be activated outside a main phase")
    void unearthRequiresMainPhase() {
        harness.setGraveyard(player1, List.of(new TocasiasOnulet()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Tocasia's Onulet");
        harness.assertNotOnBattlefield(player1, "Tocasia's Onulet");
    }

    @Test
    @DisplayName("The next end step exile triggers life gain")
    void unearthEndStepExileGainsLife() {
        harness.setGraveyard(player1, List.of(new TocasiasOnulet()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        int lifeBefore = gd.getLife(player1.getId());

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tocasia's Onulet");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Tocasia's Onulet"));
        harness.assertLife(player1, lifeBefore + 2);
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

        harness.passUntil(TurnStep.END_STEP);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Tocasia's Onulet");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Tocasia's Onulet"));
    }
}
