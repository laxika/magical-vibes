package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AetherBurst;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarkwaterEgg.class, AetherBurst.class, DuskImp.class})
class DarkwaterEggTest extends BaseCardTest {

    @Test
    void activationSacrificesAddsBothManaAndDraws() {
        harness.addToBattlefield(player1, new DarkwaterEgg());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Darkwater Egg");
        harness.assertInGraveyard(player1, "Darkwater Egg");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void activationRequiresTwoGenericMana() {
        harness.addToBattlefield(player1, new DarkwaterEgg());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () ->
                harness.activateAbility(player1, 0, null, null));

        harness.assertOnBattlefield(player1, "Darkwater Egg");
    }

    @Test
    void activationRequiresAnUntappedSource() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new DarkwaterEgg());
        egg.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () ->
                harness.activateAbility(player1, 0, null, null));

        harness.assertOnBattlefield(player1, "Darkwater Egg");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    void activationCanRespondToAnotherSpell() {
        Permanent target = addCreatureReady(player2, new DuskImp());
        harness.addToBattlefield(player1, new DarkwaterEgg());
        harness.setHand(player1, java.util.List.of(new AetherBurst()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Darkwater Egg");
        assertThat(gd.stack).hasSize(2);

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player2, "Dusk Imp");
    }

    @Test
    void manaAndDrawWaitUntilAbilityResolves() {
        harness.addToBattlefield(player1, new DarkwaterEgg());
        harness.setHand(player1, java.util.List.of());
        harness.setLibrary(player1, java.util.List.of(new DuskImp()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Darkwater Egg");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();

        harness.passBothPriorities();

        harness.assertInHand(player1, "Dusk Imp");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    void canActivateDuringOpponentsTurnWithColoredManaPayingGenericCost() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new DarkwaterEgg());
        harness.setHand(player1, java.util.List.of());
        harness.setLibrary(player1, java.util.List.of(new DuskImp()));
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Darkwater Egg");
        harness.assertInHand(player1, "Dusk Imp");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isZero();
    }
}
