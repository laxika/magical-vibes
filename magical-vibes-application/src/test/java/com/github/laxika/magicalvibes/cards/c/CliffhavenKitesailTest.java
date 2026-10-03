package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
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

@CardUsed({CliffhavenKitesail.class, CanopyBaloth.class, IntoTheRoil.class})
class CliffhavenKitesailTest extends BaseCardTest {

    @Test
    void equippedCreatureHasFlying() {
        Permanent creature = addCreatureReady(player1, new CanopyBaloth());
        Permanent kitesail = harness.addToBattlefieldAndReturn(player1, new CliffhavenKitesail());
        kitesail.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();

        kitesail.setAttachedTo(null);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    void enteringKitesailAttachesToTargetCreatureYouControl() {
        Permanent creature = addCreatureReady(player1, new CanopyBaloth());
        harness.setHand(player1, List.of(new CliffhavenKitesail()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0, creature.getId());
        resolveAllTriggers();

        Permanent kitesail = findPermanent(player1, "Cliffhaven Kitesail");
        assertThat(kitesail.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    void enteringKitesailCannotTargetOpponentCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new CanopyBaloth());
        harness.setHand(player1, List.of(new CliffhavenKitesail()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equipMovesKitesailToAnotherCreature() {
        Permanent kitesail = harness.addToBattlefieldAndReturn(player1, new CliffhavenKitesail());
        Permanent firstCreature = addCreatureReady(player1, new CanopyBaloth());
        Permanent secondCreature = addCreatureReady(player1, new CanopyBaloth());
        kitesail.setAttachedTo(firstCreature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(kitesail.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.FLYING)).isTrue();
    }

    @Test
    void canBeCastWithoutCreaturesAndEntersUnattached() {
        harness.setHand(player1, List.of(new CliffhavenKitesail()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Cliffhaven Kitesail").getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void entryTargetIsChosenAfterEquipmentSpellResolves() {
        Permanent creature = addCreatureReady(player1, new CanopyBaloth());
        Permanent opponentCreature = addCreatureReady(player2, new CanopyBaloth());
        harness.setHand(player1, List.of(new CliffhavenKitesail()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent kitesail = findPermanent(player1, "Cliffhaven Kitesail");
        assertThat(kitesail.getAttachedTo()).isNull();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, creature.getId());
        assertThat(kitesail.getAttachedTo()).isNull();
        resolveAllTriggers();

        assertThat(kitesail.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    void equipCannotTargetOpponentCreature() {
        Permanent kitesail = harness.addToBattlefieldAndReturn(player1, new CliffhavenKitesail());
        Permanent creature = addCreatureReady(player1, new CanopyBaloth());
        Permanent opponentCreature = addCreatureReady(player2, new CanopyBaloth());
        kitesail.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(kitesail.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void equipRequiresTwoMana() {
        Permanent kitesail = harness.addToBattlefieldAndReturn(player1, new CliffhavenKitesail());
        Permanent creature = addCreatureReady(player1, new CanopyBaloth());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(kitesail.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotBeActivatedDuringCombat() {
        Permanent kitesail = harness.addToBattlefieldAndReturn(player1, new CliffhavenKitesail());
        Permanent creature = addCreatureReady(player1, new CanopyBaloth());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(kitesail.getAttachedTo()).isNull();
    }

    @Test
    void entryAbilityDoesNotAttachWhenTargetLeavesInResponse() {
        Permanent creature = addCreatureReady(player1, new CanopyBaloth());
        harness.setHand(player1, List.of(new CliffhavenKitesail()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0, creature.getId());
        harness.passBothPriorities();
        Permanent kitesail = findPermanent(player1, "Cliffhaven Kitesail");

        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, creature.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Canopy Baloth");
        assertThat(kitesail.getAttachedTo()).isNull();
    }

    @Test
    void failedEquipLeavesEquipmentAttachedToOriginalCreature() {
        Permanent kitesail = harness.addToBattlefieldAndReturn(player1, new CliffhavenKitesail());
        Permanent firstCreature = addCreatureReady(player1, new CanopyBaloth());
        Permanent secondCreature = addCreatureReady(player1, new CanopyBaloth());
        kitesail.setAttachedTo(firstCreature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, secondCreature.getId());

        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, secondCreature.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Canopy Baloth");
        assertThat(kitesail.getAttachedTo()).isEqualTo(firstCreature.getId());
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.FLYING)).isTrue();
    }
}
