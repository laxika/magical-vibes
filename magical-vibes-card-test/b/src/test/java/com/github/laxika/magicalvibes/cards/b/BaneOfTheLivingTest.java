package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BaneOfTheLiving.class, EnormousBaloth.class, FugitiveWizard.class})
class BaneOfTheLivingTest extends BaseCardTest {

    @Test
    @DisplayName("Turning Bane of the Living face up gives all creatures -X/-X")
    void turnsFaceUpAndWeakensAllCreaturesByPaidX() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player2, new FugitiveWizard());
        Permanent opponentBaloth = harness.addToBattlefieldAndReturn(player2, new EnormousBaloth());
        Permanent bane = castFaceDown();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bane));
        harness.handleXValueChosen(player1, 2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fugitive Wizard");
        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        assertThat(gqs.getEffectivePower(gd, opponentBaloth)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, opponentBaloth)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, bane)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bane)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bane of the Living's face-up debuff wears off at end of turn")
    void faceUpDebuffWearsOffAtEndOfTurn() {
        Permanent opponentBaloth = harness.addToBattlefieldAndReturn(player2, new EnormousBaloth());
        Permanent bane = castFaceDown();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bane));
        harness.handleXValueChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opponentBaloth)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, opponentBaloth)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opponentBaloth)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, opponentBaloth)).isEqualTo(7);
    }

    @Test
    @DisplayName("Choosing zero for morph leaves creatures unchanged")
    void zeroXDoesNotWeakenCreatures() {
        Permanent wizard = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());
        Permanent bane = castFaceDown();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bane));
        harness.handleXValueChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(bane.isFaceDown()).isFalse();
        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Bane of the Living");
    }

    @Test
    @DisplayName("Bane can die to its own debuff without sparing other creatures")
    void lethalXAlsoKillsBane() {
        harness.addToBattlefield(player2, new FugitiveWizard());
        Permanent baloth = harness.addToBattlefieldAndReturn(player2, new EnormousBaloth());
        Permanent bane = castFaceDown();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bane));
        harness.handleXValueChosen(player1, 3);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bane of the Living");
        harness.assertInGraveyard(player2, "Fugitive Wizard");
        assertThat(gqs.getEffectivePower(gd, baloth)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, baloth)).isEqualTo(4);
    }

    @Test
    @DisplayName("Creatures entering after the trigger resolves are not weakened")
    void laterCreaturesAreNotAffected() {
        Permanent baloth = harness.addToBattlefieldAndReturn(player2, new EnormousBaloth());
        Permanent bane = castFaceDown();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bane));
        harness.handleXValueChosen(player1, 2);
        harness.passBothPriorities();

        Permanent wizard = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());
        assertThat(gqs.getEffectivePower(gd, baloth)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, baloth)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Fugitive Wizard");
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new BaneOfTheLiving()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Bane of the Living");
    }
}
