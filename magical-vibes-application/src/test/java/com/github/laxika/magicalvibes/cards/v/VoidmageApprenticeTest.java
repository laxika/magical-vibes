package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoidmageApprentice.class, FugitiveWizard.class})
class VoidmageApprenticeTest extends BaseCardTest {

    @Test
    void turningFaceUpCountersTargetSpell() {
        VoidmageApprentice apprentice = new VoidmageApprentice();
        FugitiveWizard wizard = new FugitiveWizard();
        harness.setHand(player1, List.of(apprentice));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent faceDownApprentice = findPermanent(player1, "Voidmage Apprentice");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(wizard));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(faceDownApprentice));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(wizard.getId());
        harness.handlePermanentChosen(player1, wizard.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fugitive Wizard");
        assertThat(faceDownApprentice.isFaceDown()).isFalse();
    }

    @Test
    void turningFaceUpWithoutSpellDoesNotCreateTargetChoice() {
        harness.setHand(player1, List.of(new VoidmageApprentice()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent faceDownApprentice = findPermanent(player1, "Voidmage Apprentice");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(faceDownApprentice));

        assertThat(faceDownApprentice.isFaceDown()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void castingFaceUpDoesNotTriggerCounterAbility() {
        harness.setHand(player1, List.of(new VoidmageApprentice()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Voidmage Apprentice");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void turningFaceUpCanCounterOwnSpell() {
        harness.setHand(player1, List.of(new VoidmageApprentice()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        Permanent apprentice = findPermanent(player1, "Voidmage Apprentice");
        FugitiveWizard wizard = new FugitiveWizard();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(wizard));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(apprentice));

        assertThat(apprentice.isFaceDown()).isFalse();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(wizard.getId());
        harness.handlePermanentChosen(player1, wizard.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fugitive Wizard");
        harness.assertNotOnBattlefield(player1, "Fugitive Wizard");
        harness.assertOnBattlefield(player1, "Voidmage Apprentice");
    }

    @Test
    void turningFaceUpRequiresFullMorphCost() {
        harness.setHand(player1, List.of(new VoidmageApprentice()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        Permanent apprentice = findPermanent(player1, "Voidmage Apprentice");
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.turnFaceUp(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(apprentice)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(apprentice.isFaceDown()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
