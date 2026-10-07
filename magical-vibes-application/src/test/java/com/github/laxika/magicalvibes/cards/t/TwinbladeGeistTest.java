package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({TwinbladeGeist.class, GrizzlyBears.class})
class TwinbladeGeistTest extends BaseCardTest {

    @Test
    @DisplayName("Disturb casts from graveyard transformed as Twinblade Invocation attached to a creature")
    void disturbEntersTransformedAttached() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new TwinbladeGeist()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveFlashback(player1, 0, bears.getId());

        Permanent aura = findPermanent(player1, "Twinblade Invocation");
        assertThat(aura.isTransformed()).isTrue();
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Enchanted creature has double strike")
    void enchantedCreatureHasDoubleStrike() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        putInvocationAttachedTo(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses double strike when the Aura is removed")
    void doubleStrikeStopsWhenAuraRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = putInvocationAttachedTo(bears);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Twinblade Invocation is exiled instead of going to the graveyard")
    void invocationExiledInsteadOfGraveyard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = putInvocationAttachedTo(bears);
        UUID auraCardId = aura.getOriginalCard().getId();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(e -> e.card().getId())).contains(auraCardId);
    }

    @Test
    @DisplayName("Disturb requires a creature target")
    void disturbRequiresCreatureTarget() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new TwinbladeGeist()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("target");
    }

    @Test
    void frontFaceDealsDamageInBothCombatDamageSteps() {
        addCreatureReady(player1, new TwinbladeGeist());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    void frontFaceDiesNormallyRatherThanBeingExiled() {
        Permanent geist = harness.addToBattlefieldAndReturn(player1, new TwinbladeGeist());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, geist));

        harness.assertInGraveyard(player1, "Twinblade Geist");
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void disturbCanEnchantOpponentsCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        Permanent aura = putInvocationAttachedTo(bears);

        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(aura);
    }

    @Test
    void disturbIsExiledWhenItsTargetLeavesBeforeResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        TwinbladeGeist geist = new TwinbladeGeist();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(geist));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFlashback(player1, 0, bears.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Twinblade Invocation");
        harness.assertNotInGraveyard(player1, "Twinblade Geist");
        assertThat(gd.exiledCards.stream().map(e -> e.card().getId())).contains(geist.getId());
    }

    @Test
    @CardUsed({ThaliaGuardianOfThraben.class})
    void disturbPaysNoncreatureSpellTax() {
        Permanent thalia = harness.addToBattlefieldAndReturn(player1, new ThaliaGuardianOfThraben());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new TwinbladeGeist()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, thalia.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }
    @Test
    void invocationIsExiledWhenEnchantedCreatureDies() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = putInvocationAttachedTo(bears);
        UUID auraCardId = aura.getOriginalCard().getId();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Twinblade Geist");
        harness.assertNotOnBattlefield(player1, "Twinblade Invocation");
        assertThat(gd.exiledCards.stream().map(e -> e.card().getId())).contains(auraCardId);
    }
    private Permanent putInvocationAttachedTo(Permanent creature) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new TwinbladeGeist()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveFlashback(player1, 0, creature.getId());
        return findPermanent(player1, "Twinblade Invocation");
    }
}
