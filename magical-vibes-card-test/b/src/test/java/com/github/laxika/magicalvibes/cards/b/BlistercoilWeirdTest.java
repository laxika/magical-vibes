package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AnnihilatingFire;
import com.github.laxika.magicalvibes.cards.d.Dispel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Ponder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlistercoilWeird.class, Shock.class, Ponder.class, GrizzlyBears.class,
        AnnihilatingFire.class, Dispel.class})
class BlistercoilWeirdTest extends BaseCardTest {

    private Permanent addWeird() {
        Permanent weird = harness.addToBattlefieldAndReturn(player1, new BlistercoilWeird());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return weird;
    }

    @Test
    @DisplayName("Gets +1/+1 and untaps when you cast an instant")
    void instantSpellPumpsAndUntaps() {
        Permanent weird = addWeird();
        weird.tap();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(weird.getPowerModifier()).isEqualTo(1);
        assertThat(weird.getToughnessModifier()).isEqualTo(1);
        assertThat(weird.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+1 when you cast a sorcery")
    void sorcerySpellPumps() {
        Permanent weird = addWeird();

        harness.setHand(player1, List.of(new Ponder()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(weird.getPowerModifier()).isEqualTo(1);
        assertThat(weird.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when you cast a creature spell")
    void creatureSpellDoesNotTrigger() {
        Permanent weird = addWeird();
        weird.tap();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(weird.getPowerModifier()).isEqualTo(0);
        assertThat(weird.getToughnessModifier()).isEqualTo(0);
        assertThat(weird.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Pump and untap wait for the cast trigger to resolve")
    void pumpAndUntapAreNotImmediate() {
        Permanent weird = addWeird();
        weird.tap();
        harness.setHand(player1, List.of(new AnnihilatingFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(weird.getPowerModifier()).isZero();
        assertThat(weird.getToughnessModifier()).isZero();
        assertThat(weird.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        assertThat(weird.getPowerModifier()).isEqualTo(1);
        assertThat(weird.getToughnessModifier()).isEqualTo(1);
        assertThat(weird.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A sorcery also untaps the creature")
    void sorcerySpellUntaps() {
        Permanent weird = addWeird();
        weird.tap();
        harness.setHand(player1, List.of(new Ponder()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(weird.getPowerModifier()).isEqualTo(1);
        assertThat(weird.getToughnessModifier()).isEqualTo(1);
        assertThat(weird.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Each cast pumps and untaps again, and the bonuses expire at cleanup")
    void repeatedCastsAccumulateUntilCleanup() {
        Permanent weird = addWeird();
        weird.tap();
        harness.setHand(player1, List.of(new AnnihilatingFire(), new AnnihilatingFire()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(weird.getPowerModifier()).isEqualTo(1);
        assertThat(weird.isTapped()).isFalse();
        weird.tap();
        harness.ensurePriority(player1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passUntil(TurnStep.END_STEP);

        assertThat(weird.getPowerModifier()).isEqualTo(2);
        assertThat(weird.getToughnessModifier()).isEqualTo(2);
        assertThat(weird.isTapped()).isFalse();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(weird.getPowerModifier()).isZero();
        assertThat(weird.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An opponent's instant does not pump or untap the creature")
    void opponentSpellDoesNotTrigger() {
        Permanent weird = addWeird();
        weird.tap();
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new AnnihilatingFire()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(weird.getPowerModifier()).isZero();
        assertThat(weird.getToughnessModifier()).isZero();
        assertThat(weird.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The controller's instant triggers during an opponent's turn")
    void controllerSpellTriggersDuringOpponentTurn() {
        Permanent weird = addWeird();
        weird.tap();
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new AnnihilatingFire()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.ensurePriority(player1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(weird.getPowerModifier()).isEqualTo(1);
        assertThat(weird.getToughnessModifier()).isEqualTo(1);
        assertThat(weird.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Each controlled Weird affects only itself")
    void multipleWeirdsResolveIndependently() {
        Permanent first = addWeird();
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BlistercoilWeird());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new BlistercoilWeird());
        first.tap();
        second.tap();
        opposing.tap();
        harness.setHand(player1, List.of(new AnnihilatingFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passUntil(TurnStep.END_STEP);

        assertThat(first.getPowerModifier()).isEqualTo(1);
        assertThat(first.getToughnessModifier()).isEqualTo(1);
        assertThat(first.isTapped()).isFalse();
        assertThat(second.getPowerModifier()).isEqualTo(1);
        assertThat(second.getToughnessModifier()).isEqualTo(1);
        assertThat(second.isTapped()).isFalse();
        assertThat(opposing.getPowerModifier()).isZero();
        assertThat(opposing.getToughnessModifier()).isZero();
        assertThat(opposing.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Countering the instant does not counter its cast trigger")
    void counteredSpellStillPumpsAndUntaps() {
        Permanent weird = addWeird();
        weird.tap();
        AnnihilatingFire fire = new AnnihilatingFire();
        harness.setHand(player1, List.of(fire));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new Dispel()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, fire.getId());
        harness.passUntil(TurnStep.END_STEP);

        harness.assertInGraveyard(player1, "Annihilating Fire");
        harness.assertLife(player2, 20);
        assertThat(weird.getPowerModifier()).isEqualTo(1);
        assertThat(weird.getToughnessModifier()).isEqualTo(1);
        assertThat(weird.isTapped()).isFalse();
    }
}
