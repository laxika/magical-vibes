package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SanguineSavior.class, GrizzlyBears.class, Shock.class})
class SanguineSaviorTest extends BaseCardTest {

    @Test
    void turningFaceUpGivesAnotherCreatureYouControlLifelinkUntilEndOfTurn() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent savior = castFaceDown();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(savior));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(target.getId())
                .doesNotContain(savior.getId(), opponentCreature.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void disguiseWardCountersAnOpponentSpellWhileFaceDown() {
        Permanent savior = castFaceDown();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, savior.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(savior.isFaceDown()).isTrue();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void payingDisguiseWardOnceAllowsTheSpellToResolve() {
        Permanent savior = castFaceDown();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, savior.getId());
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Sanguine Savior");
        harness.assertInGraveyard(player1, "Sanguine Savior");
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLACK"})
    void eitherHybridColorCanPayTheEntireDisguiseCostWithoutALegalTarget(ManaColor color) {
        Permanent savior = castFaceDown();
        harness.addMana(player1, color, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(savior));
        resolveAllTriggers();

        assertThat(savior.isFaceDown()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Sanguine Savior");
    }

    @Test
    void lifelinkAbilityDoesNotAffectOtherCreaturesWhenItsTargetLeaves() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        Permanent savior = castFaceDown();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(savior));
        harness.handlePermanentChosen(player1, target.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, other, Keyword.LIFELINK)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void faceUpSaviorDoesNotHaveDisguiseWard() {
        Permanent savior = castFaceDown();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(savior));
        resolveAllTriggers();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, savior.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Sanguine Savior");
        harness.assertInGraveyard(player1, "Sanguine Savior");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void disguiseWardDoesNotCounterItsControllersSpell() {
        Permanent savior = castFaceDown();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, savior.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Sanguine Savior");
        harness.assertInGraveyard(player1, "Sanguine Savior");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new SanguineSavior()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player1, "Sanguine Savior");
    }
}
