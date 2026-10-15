package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.cards.r.Rescind;
import com.github.laxika.magicalvibes.cards.m.Morphling;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GildedDrake.class, GorillaWarrior.class, RayOfCommand.class, Rescind.class, Morphling.class})
class GildedDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exchanges control of Gilded Drake and the targeted opposing creature")
    void exchangesControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GorillaWarrior());
        castDrake();

        harness.passBothPriorities();
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gorilla Warrior");
        harness.assertOnBattlefield(player2, "Gilded Drake");
    }

    @Test
    @DisplayName("Declining the optional target sacrifices Gilded Drake")
    void decliningTargetSacrificesDrake() {
        harness.addToBattlefieldAndReturn(player2, new GorillaWarrior());
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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GorillaWarrior());
        castDrake();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gilded Drake");
        harness.assertNotOnBattlefield(player1, "Gilded Drake");
    }

    @Test
    @DisplayName("If an opponent gains control of Gilded Drake, its controller cannot sacrifice it")
    void opponentGainsControlBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GorillaWarrior());
        castDrake();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Gilded Drake"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Gilded Drake");
        harness.assertOnBattlefield(player2, "Gorilla Warrior");
        harness.assertNotInGraveyard(player1, "Gilded Drake");
    }

    @Test
    @DisplayName("A target that gains shroud prevents the exchange and Gilded Drake is sacrificed")
    void targetGainsShroudBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Morphling());
        castDrake();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, 0, 2, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Morphling");
        harness.assertNotOnBattlefield(player1, "Morphling");
        harness.assertInGraveyard(player1, "Gilded Drake");
        harness.assertNotOnBattlefield(player2, "Gilded Drake");
    }

    @Test
    @DisplayName("A creature with shroud cannot be selected for the exchange")
    void shroudedCreatureIsNotALegalTarget() {
        harness.addToBattlefield(player2, new Morphling());
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, 0, 2, null, null);
        harness.passBothPriorities();

        castDrake();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Morphling");
        harness.assertInGraveyard(player1, "Gilded Drake");
    }

    @Test
    @DisplayName("Returning Gilded Drake to hand before resolution prevents either half of the exchange")
    void drakeLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GorillaWarrior());
        castDrake();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        harness.setHand(player1, List.of(new Rescind()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Gilded Drake"));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Gilded Drake");
        harness.assertNotInGraveyard(player1, "Gilded Drake");
        harness.assertOnBattlefield(player2, "Gorilla Warrior");
        harness.assertNotOnBattlefield(player1, "Gorilla Warrior");
        harness.assertNotOnBattlefield(player2, "Gilded Drake");
    }

    private void castDrake() {
        harness.castFromHand(player1, new GildedDrake(), "{1}{U}");
    }
}
