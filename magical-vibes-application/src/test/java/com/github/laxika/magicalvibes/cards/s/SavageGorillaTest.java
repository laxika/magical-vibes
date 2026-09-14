package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GaeasSkyfolk;
import com.github.laxika.magicalvibes.cards.k.KavuMauler;
import com.github.laxika.magicalvibes.cards.y.YavimayaCoast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SavageGorilla.class, KavuMauler.class, GaeasSkyfolk.class, YavimayaCoast.class})
class SavageGorillaTest extends BaseCardTest {

    @Test
    void sacrificesItselfDebuffsTargetAndDraws() {
        addReadyGorilla();
        Permanent target = addFourFourKavu(player2);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLibrary(player1, List.of(new SavageGorilla()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Savage Gorilla");
        harness.assertInGraveyard(player1, "Savage Gorilla");

        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Savage Gorilla");
    }

    @Test
    void debuffWearsOffAtCleanup() {
        addReadyGorilla();
        Permanent target = addFourFourKavu(player2);
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void lethalDebuffStillDrawsBeforeStateBasedActions() {
        addReadyGorilla();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GaeasSkyfolk());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLibrary(player1, List.of(new SavageGorilla()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Gaea's Skyfolk");
        harness.assertInGraveyard(player2, "Gaea's Skyfolk");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Savage Gorilla");
    }

    @Test
    void selfTargetFizzlesAfterSacrificeCost() {
        Permanent gorilla = addReadyGorilla();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLibrary(player1, List.of(new SavageGorilla()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, gorilla.getId());
        harness.assertInGraveyard(player1, "Savage Gorilla");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent gorilla = harness.addToBattlefieldAndReturn(player1, new SavageGorilla());
        Permanent target = addFourFourKavu(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Savage Gorilla");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gorilla.isTapped()).isFalse();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        addReadyGorilla();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new YavimayaCoast());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Savage Gorilla");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private Permanent addReadyGorilla() {
        return addCreatureReady(player1, new SavageGorilla());
    }

    private Permanent addFourFourKavu(Player player) {
        return harness.addToBattlefieldAndReturn(player, new KavuMauler());
    }
}
