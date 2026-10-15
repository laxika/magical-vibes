package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SoulSummons;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DefenestratedPhantom.class, Shock.class, Murder.class, SoulSummons.class})
class DefenestratedPhantomTest extends BaseCardTest {

    @Test
    @DisplayName("Disguise casts Defenestrated Phantom face down with ward")
    void disguiseCastsFaceDownWithWard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DefenestratedPhantom()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent phantom = findPermanent(player1, "Defenestrated Phantom");
        assertThat(phantom.isFaceDown()).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, phantom.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Defenestrated Phantom").isFaceDown()).isTrue();
    }

    @Test
    @DisplayName("Disguise's ward is absent after Defenestrated Phantom is cast face up")
    void faceUpPhantomDoesNotHaveDisguiseWard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DefenestratedPhantom()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent phantom = findPermanent(player1, "Defenestrated Phantom");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, phantom.getId());

        harness.assertInGraveyard(player1, "Defenestrated Phantom");
    }

    @Test
    @DisplayName("Ward still counters after the disguised creature turns face up")
    void wardStillCountersAfterTurningFaceUp() {
        Permanent phantom = castDisguisedPhantom();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, phantom.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(phantom));
        assertThat(phantom.isFaceDown()).isFalse();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Defenestrated Phantom");
        harness.assertInGraveyard(player2, "Murder");
    }

    @Test
    @DisplayName("Paying ward allows the opposing spell to resolve")
    void payingWardAllowsShockToResolve() {
        Permanent phantom = castDisguisedPhantom();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, phantom.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Defenestrated Phantom");
    }

    @Test
    @DisplayName("Ward does not trigger for its controller's spell")
    void ownSpellDoesNotTriggerWard() {
        Permanent phantom = castDisguisedPhantom();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, phantom.getId());

        harness.assertInGraveyard(player1, "Defenestrated Phantom");
    }

    @Test
    @CardUsed({DefenestratedPhantom.class, SoulSummons.class, Shock.class})
    @DisplayName("Manifesting a disguise card does not grant ward")
    void manifestedPhantomDoesNotHaveWard() {
        harness.setHand(player1, List.of(new SoulSummons()));
        harness.setLibrary(player1, List.of(new DefenestratedPhantom()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent phantom = findPermanent(player1, "Defenestrated Phantom");
        assertThat(phantom.isManifested()).isTrue();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, phantom.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Defenestrated Phantom");
    }

    private Permanent castDisguisedPhantom() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DefenestratedPhantom()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Defenestrated Phantom");
    }
}
