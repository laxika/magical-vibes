package com.github.laxika.magicalvibes.cards.p;

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

@CardUsed({PatronOfTheWild.class, FugitiveWizard.class})
class PatronOfTheWildTest extends BaseCardTest {

    @Test
    void turningFaceUpBoostsTargetCreatureUntilEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());
        harness.setHand(player1, List.of(new PatronOfTheWild()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent patron = findPermanent(player1, "Patron of the Wild");
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(patron));

        assertThat(patron.isFaceDown()).isFalse();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownCreature.getId(), opponentCreature.getId(), patron.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.getEffectivePower()).isEqualTo(4);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(4);
        assertThat(ownCreature.getEffectivePower()).isEqualTo(1);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(opponentCreature.getEffectivePower()).isEqualTo(1);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void canTargetItselfAndBoostOnlyWhenTheTriggerResolves() {
        harness.setHand(player1, List.of(new PatronOfTheWild()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent patron = findPermanent(player1, "Patron of the Wild");
        assertThat(patron.isFaceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(patron));
        harness.handlePermanentChosen(player1, patron.getId());

        assertThat(patron.isFaceDown()).isFalse();
        assertThat(patron.getPowerModifier()).isZero();
        assertThat(patron.getToughnessModifier()).isZero();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(patron.getPowerModifier()).isEqualTo(3);
        assertThat(patron.getToughnessModifier()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTurnFaceUpWithoutTheGreenManaInItsMorphCost() {
        harness.setHand(player1, List.of(new PatronOfTheWild()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent patron = findPermanent(player1, "Patron of the Wild");
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.turnFaceUp(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(patron)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(patron.isFaceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void castingItFaceUpDoesNotTriggerTurnedFaceUpAbility() {
        harness.setHand(player1, List.of(new PatronOfTheWild()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent patron = findPermanent(player1, "Patron of the Wild");
        assertThat(patron.isFaceDown()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
