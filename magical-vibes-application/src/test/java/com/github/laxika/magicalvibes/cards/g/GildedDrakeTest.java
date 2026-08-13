package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GildedDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exchanges control of Gilded Drake and the targeted opposing creature")
    void exchangesControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castDrake();

        harness.passBothPriorities();
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Gilded Drake");
    }

    @Test
    @DisplayName("Declining the optional target sacrifices Gilded Drake")
    void decliningTargetSacrificesDrake() {
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castDrake();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gilded Drake");
        harness.assertNotOnBattlefield(player1, "Gilded Drake");
    }

    @Test
    @DisplayName("With no legal target, Gilded Drake enters and is then sacrificed")
    void noLegalTargetSacrificesDrake() {
        castDrake();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gilded Drake");
    }

    @Test
    @DisplayName("If the target becomes illegal, Gilded Drake is sacrificed")
    void illegalTargetSacrificesDrake() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castDrake();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gilded Drake");
        harness.assertNotOnBattlefield(player1, "Gilded Drake");
    }

    private void castDrake() {
        harness.setHand(player1, List.of(new GildedDrake()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
    }
}
