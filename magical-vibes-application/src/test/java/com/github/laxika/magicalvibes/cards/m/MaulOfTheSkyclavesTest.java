package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.ExpeditionHealer;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaulOfTheSkyclaves.class, ExpeditionHealer.class})
class MaulOfTheSkyclavesTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsBoostFlyingAndFirstStrike() {
        Permanent creature = addCreatureReady(player1, new ExpeditionHealer());
        Permanent maul = harness.addToBattlefieldAndReturn(player1, new MaulOfTheSkyclaves());
        maul.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        maul.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void enteringMaulAttachesToTargetCreatureYouControl() {
        Permanent creature = addCreatureReady(player1, new ExpeditionHealer());
        harness.setHand(player1, List.of(new MaulOfTheSkyclaves()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        Permanent maul = findPermanent(player1, "Maul of the Skyclaves");
        assertThat(maul.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void enteringMaulCannotTargetOpponentCreature() {
        Permanent ownCreature = addCreatureReady(player1, new ExpeditionHealer());
        Permanent opponentCreature = addCreatureReady(player2, new ExpeditionHealer());
        harness.setHand(player1, List.of(new MaulOfTheSkyclaves()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Maul of the Skyclaves").getAttachedTo())
                .isEqualTo(ownCreature.getId());
    }

    @Test
    void equipMovesMaulAndItsAbilitiesToAnotherCreature() {
        Permanent maul = harness.addToBattlefieldAndReturn(player1, new MaulOfTheSkyclaves());
        Permanent firstCreature = addCreatureReady(player1, new ExpeditionHealer());
        Permanent secondCreature = addCreatureReady(player1, new ExpeditionHealer());
        maul.setAttachedTo(firstCreature.getId());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(maul.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, secondCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void canEnterWithoutAnyCreatureYouControl() {
        addCreatureReady(player2, new ExpeditionHealer());
        harness.setHand(player1, List.of(new MaulOfTheSkyclaves()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Maul of the Skyclaves").getAttachedTo()).isNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void enteringWithoutBeingCastStillAttachesToChosenCreature() {
        Permanent creature = addCreatureReady(player1, new ExpeditionHealer());

        Permanent maul = harness.enterBattlefieldAndReturn(player1, new MaulOfTheSkyclaves());
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        assertThat(maul.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void entryTriggerDoesNotAttachWhenChosenCreatureLeavesBattlefield() {
        Permanent creature = addCreatureReady(player1, new ExpeditionHealer());
        harness.setHand(player1, List.of(new MaulOfTheSkyclaves()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Maul of the Skyclaves").getAttachedTo()).isNull();
    }

    @Test
    void failedEquipLeavesMaulAttachedToOriginalCreature() {
        Permanent maul = harness.addToBattlefieldAndReturn(player1, new MaulOfTheSkyclaves());
        Permanent original = addCreatureReady(player1, new ExpeditionHealer());
        Permanent target = addCreatureReady(player1, new ExpeditionHealer());
        maul.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.activateAbility(player1, 0, null, target.getId());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));
        resolveAllTriggers();

        assertThat(maul.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, original, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, original, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void equipCannotTargetOpponentCreature() {
        Permanent maul = harness.addToBattlefieldAndReturn(player1, new MaulOfTheSkyclaves());
        Permanent opponentCreature = addCreatureReady(player2, new ExpeditionHealer());
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(maul.getAttachedTo()).isNull();
    }

    @Test
    void equipRequiresTwoWhiteMana() {
        Permanent maul = harness.addToBattlefieldAndReturn(player1, new MaulOfTheSkyclaves());
        Permanent creature = addCreatureReady(player1, new ExpeditionHealer());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(maul.getAttachedTo()).isNull();
    }

    @Test
    void equipCannotBeActivatedDuringCombat() {
        Permanent maul = harness.addToBattlefieldAndReturn(player1, new MaulOfTheSkyclaves());
        Permanent creature = addCreatureReady(player1, new ExpeditionHealer());
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(maul.getAttachedTo()).isNull();
    }

    @Test
    void oldEntryTriggerCannotAttachMaulThatLeftAndReturned() {
        Permanent firstCreature = addCreatureReady(player1, new ExpeditionHealer());
        Permanent secondCreature = addCreatureReady(player1, new ExpeditionHealer());
        Permanent originalMaul = harness.enterBattlefieldAndReturn(player1, new MaulOfTheSkyclaves());
        harness.handlePermanentChosen(player1, firstCreature.getId());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, originalMaul));
        harness.setGraveyard(player1, List.of());
        Permanent returnedMaul = harness.enterBattlefieldAndReturn(player1, originalMaul.getCard());
        harness.handlePermanentChosen(player1, secondCreature.getId());
        resolveAllTriggers();

        assertThat(returnedMaul.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(4);
    }

    @Test
    void oldEquipAbilityCannotMoveMaulThatLeftAndReturned() {
        Permanent originalMaul = harness.addToBattlefieldAndReturn(player1, new MaulOfTheSkyclaves());
        Permanent firstCreature = addCreatureReady(player1, new ExpeditionHealer());
        Permanent secondCreature = addCreatureReady(player1, new ExpeditionHealer());
        originalMaul.setAttachedTo(firstCreature.getId());
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.activateAbility(player1, 0, null, secondCreature.getId());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, originalMaul));
        harness.setGraveyard(player1, List.of());
        Permanent returnedMaul = harness.enterBattlefieldAndReturn(player1, originalMaul.getCard());
        harness.handlePermanentChosen(player1, firstCreature.getId());
        resolveAllTriggers();

        assertThat(returnedMaul.getAttachedTo()).isEqualTo(firstCreature.getId());
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(2);
    }
}
