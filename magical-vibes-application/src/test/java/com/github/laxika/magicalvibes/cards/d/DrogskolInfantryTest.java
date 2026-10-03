package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.t.TravelingMinister;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrogskolInfantry.class, DrogskolArmaments.class, TravelingMinister.class})
class DrogskolInfantryTest extends BaseCardTest {

    @Test
    @DisplayName("Disturb casts the card from the graveyard transformed as an Aura")
    void disturbEntersTransformedAttachedAndBoostsCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new TravelingMinister());
        castDisturb(bears.getId());

        Permanent aura = findTransformedPermanent();
        assertThat(aura.isTransformed()).isTrue();
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The transformed Aura is exiled instead of going to the graveyard")
    void transformedAuraIsExiledInsteadOfGraveyard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new TravelingMinister());
        Permanent aura = castDisturb(bears.getId());
        UUID auraCardId = aura.getOriginalCard().getId();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId())).contains(auraCardId);
    }

    @Test
    @DisplayName("Disturb requires a creature target")
    void disturbRequiresCreatureTarget() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new DrogskolInfantry()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("target");
    }

    @Test
    void disturbCanEnchantOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new TravelingMinister());

        Permanent aura = castDisturb(creature.getId());

        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void frontFaceGoesToGraveyardNormally() {
        Permanent infantry = harness.addToBattlefieldAndReturn(player1, new DrogskolInfantry());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, infantry));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(infantry.getOriginalCard());
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void disturbIsExiledWhenItsTargetLeavesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TravelingMinister());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        DrogskolInfantry infantry = new DrogskolInfantry();
        harness.setGraveyard(player1, List.of(infantry));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFlashback(player1, 0, creature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature.getOriginalCard());
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId())).contains(infantry.getId());
    }

    @Test
    void disturbCannotBeCastForFrontFaceManaCost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TravelingMinister());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        DrogskolInfantry infantry = new DrogskolInfantry();
        harness.setGraveyard(player1, List.of(infantry));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(infantry);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent castDisturb(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new DrogskolInfantry()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveFlashback(player1, 0, targetId);
        return findTransformedPermanent();
    }

    private Permanent findTransformedPermanent() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isTransformed)
                .findFirst()
                .orElseThrow();
    }
}
