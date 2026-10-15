package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
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

@CardUsed({GhituAmplifier.class, GrizzlyBears.class, FountainOfYouth.class, Shock.class, LavaAxe.class})
class GhituAmplifierTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, Ghitu Amplifier does not return a creature")
    void withoutKickerDoesNotReturnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GhituAmplifier()));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(target).isIn(gd.playerBattlefields.get(player2.getId()));
        harness.assertOnBattlefield(player1, "Ghitu Amplifier");
    }

    @Test
    @DisplayName("When kicked, Ghitu Amplifier returns a target creature an opponent controls")
    void kickedReturnsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GhituAmplifier()));
        addKickedMana();

        harness.castKickedCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Ghitu Amplifier");
    }

    @Test
    @DisplayName("The kicked ability cannot target a noncreature permanent")
    void kickedCannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new GhituAmplifier()));
        addKickedMana();

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Casting an instant boosts Ghitu Amplifier until end of turn")
    void instantBoostsUntilEndOfTurn() {
        Permanent amplifier = addAmplifier();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(amplifier.getPowerModifier()).isEqualTo(2);
        assertThat(amplifier.getToughnessModifier()).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(amplifier.getPowerModifier()).isZero();
        assertThat(amplifier.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Casting a creature does not boost Ghitu Amplifier")
    void creatureDoesNotBoost() {
        Permanent amplifier = addAmplifier();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(amplifier.getPowerModifier()).isZero();
        assertThat(amplifier.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Casting a sorcery boosts Ghitu Amplifier")
    void sorceryBoosts() {
        Permanent amplifier = addAmplifier();
        harness.setHand(player1, List.of(new LavaAxe()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(amplifier.getPowerModifier()).isEqualTo(2);
        assertThat(amplifier.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Each instant cast gives a separate cumulative boost")
    void repeatedCastsStackBoosts() {
        Permanent amplifier = addAmplifier();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(amplifier.getPowerModifier()).isEqualTo(4);
        assertThat(amplifier.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An opponent's instant does not boost Ghitu Amplifier")
    void opponentInstantDoesNotBoost() {
        Permanent amplifier = addAmplifier();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(amplifier.getPowerModifier()).isZero();
        assertThat(amplifier.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The kicked ability cannot target its controller's creature")
    void kickedCannotTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GhituAmplifier()));
        addKickedMana();

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ghitu Amplifier can be kicked when no opponent controls a creature")
    void kickedWithoutLegalTargetStillEnters() {
        harness.setHand(player1, List.of(new GhituAmplifier()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Ghitu Amplifier");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A kicked entry must choose a legal target when one exists")
    void kickedBounceCannotBeDeclined() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GhituAmplifier()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    private Permanent addAmplifier() {
        Permanent amplifier = harness.addToBattlefieldAndReturn(player1, new GhituAmplifier());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return amplifier;
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void addKickedMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
