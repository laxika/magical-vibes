package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CavernStomper;
import com.github.laxika.magicalvibes.cards.p.PanickedAltisaur;
import com.github.laxika.magicalvibes.cards.s.SeekerOfSunlight;
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

@CardUsed({RunawayBoulder.class, PanickedAltisaur.class, SeekerOfSunlight.class, CavernStomper.class})
class RunawayBoulderTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 6 damage to a target creature an opponent controls")
    void etbDealsDamageToOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new PanickedAltisaur());
        harness.setHand(player1, List.of(new RunawayBoulder()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Panicked Altisaur");
    }

    @Test
    @DisplayName("ETB cannot target a creature controlled by its controller")
    void etbCannotTargetOwnCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new PanickedAltisaur());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new PanickedAltisaur());
        harness.setHand(player1, List.of(new RunawayBoulder()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Panicked Altisaur");
    }

    @Test
    @DisplayName("Cycling {2} discards Runaway Boulder and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new RunawayBoulder()));
        harness.setLibrary(player1, List.of(new SeekerOfSunlight()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Runaway Boulder");
        harness.assertInHand(player1, "Seeker of Sunlight");
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's upkeep")
    void canBeCastDuringOpponentsUpkeep() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new PanickedAltisaur());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new RunawayBoulder()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Runaway Boulder");
        harness.assertNotOnBattlefield(player2, "Panicked Altisaur");
    }

    @Test
    @DisplayName("ETB marks exactly six damage on a surviving creature")
    void etbDealsExactlySixDamage() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new CavernStomper());
        harness.setHand(player1, List.of(new RunawayBoulder()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Cavern Stomper");
        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(6);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Artifact resolves even when no opposing creature can be targeted")
    void resolvesWithoutLegalEtbTarget() {
        harness.addToBattlefield(player1, new PanickedAltisaur());
        harness.addToBattlefield(player2, new RunawayBoulder());
        harness.setHand(player1, List.of(new RunawayBoulder()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Runaway Boulder");
        harness.assertOnBattlefield(player1, "Panicked Altisaur");
        harness.assertOnBattlefield(player2, "Runaway Boulder");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Cycling pays the discard immediately and draws only on resolution")
    void cyclingDiscardsAsCostWithoutTriggeringEtb() {
        harness.addToBattlefield(player2, new PanickedAltisaur());
        harness.setHand(player1, List.of(new RunawayBoulder()));
        harness.setLibrary(player1, List.of(new SeekerOfSunlight()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Runaway Boulder");
        harness.assertNotInHand(player1, "Runaway Boulder");
        harness.assertNotInHand(player1, "Seeker of Sunlight");
        harness.assertNotOnBattlefield(player1, "Runaway Boulder");

        harness.passBothPriorities();

        harness.assertInHand(player1, "Seeker of Sunlight");
        harness.assertOnBattlefield(player2, "Panicked Altisaur");
        assertThat(gd.stack).isEmpty();
    }
}
