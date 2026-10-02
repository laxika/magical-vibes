package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GoldmeadowStalwart;
import com.github.laxika.magicalvibes.cards.w.WanderersTwig;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShieldsOfVelisVel.class, GoldmeadowStalwart.class, WanderersTwig.class})
class ShieldsOfVelisVelTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures target player controls get +0/+1")
    void boostsTargetPlayersCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GoldmeadowStalwart());
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);

        castShields(player2.getId());

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Creatures target player controls gain all creature types")
    void grantsAllCreatureTypes() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GoldmeadowStalwart());
        assertThat(GameQueryService.permanentHasSubtype(bears, CardSubtype.KITHKIN)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(bears, CardSubtype.SOLDIER)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(bears, CardSubtype.GOBLIN)).isFalse();

        castShields(player2.getId());

        assertThat(GameQueryService.permanentHasSubtype(bears, CardSubtype.GOBLIN)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(bears, CardSubtype.ELF)).isTrue();
    }

    @Test
    @DisplayName("Affects all target player's creatures but not their noncreature permanents")
    void affectsOnlyTargetPlayersCreatures() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new GoldmeadowStalwart());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new GoldmeadowStalwart());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new WanderersTwig());

        castShields(player2.getId());

        assertThat(firstCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(secondCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(GameQueryService.permanentHasSubtype(firstCreature, CardSubtype.GOBLIN)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(secondCreature, CardSubtype.GOBLIN)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(artifact, CardSubtype.GOBLIN)).isFalse();
    }

    @Test
    @DisplayName("Does not affect caster's creatures when targeting opponent")
    void doesNotAffectCasterCreatures() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GoldmeadowStalwart());

        castShields(player2.getId());

        assertThat(ownBears.getEffectiveToughness()).isEqualTo(2);
        assertThat(GameQueryService.permanentHasSubtype(ownBears, CardSubtype.GOBLIN)).isFalse();
    }

    @Test
    @DisplayName("Can target self to buff own creatures")
    void canTargetSelf() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GoldmeadowStalwart());

        castShields(player1.getId());

        assertThat(ownBears.getEffectiveToughness()).isEqualTo(3);
        assertThat(GameQueryService.permanentHasSubtype(ownBears, CardSubtype.GOBLIN)).isTrue();
    }

    @Test
    @DisplayName("Effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GoldmeadowStalwart());

        castShields(player2.getId());
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
        assertThat(GameQueryService.permanentHasSubtype(bears, CardSubtype.GOBLIN)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(GameQueryService.permanentHasSubtype(bears, CardSubtype.GOBLIN)).isFalse();
    }

    private void castShields(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new ShieldsOfVelisVel()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, targetPlayerId);
    }
}
