package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GoblinEliteInfantry;
import com.github.laxika.magicalvibes.cards.g.GoblinKing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EgoErasureTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures target player controls get -2/-0")
    void weakensTargetPlayersCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);

        castEgoErasure(player2.getId());

        assertThat(bears.getEffectivePower()).isEqualTo(0);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures target player controls lose all creature types")
    void stripsAllCreatureTypes() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new GoblinEliteInfantry());
        assertThat(GameQueryService.permanentHasSubtype(goblin, CardSubtype.GOBLIN)).isTrue();

        castEgoErasure(player2.getId());

        assertThat(GameQueryService.permanentHasSubtype(goblin, CardSubtype.GOBLIN)).isFalse();
    }

    @Test
    @DisplayName("Stripping creature types removes tribal buffs")
    void stripCreatureTypesRemovesTribalBuff() {
        harness.addToBattlefield(player2, new GoblinKing());
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new GoblinEliteInfantry());
        // 2/2 base + Goblin King (+1/+1) = 3/3
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(3);

        castEgoErasure(player2.getId());

        // -2/-0 from Ego Erasure and no longer a Goblin, so Goblin King no longer buffs it: 0/2
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not affect caster's creatures when targeting opponent")
    void doesNotAffectCasterCreatures() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castEgoErasure(player2.getId());

        assertThat(ownBears.getEffectivePower()).isEqualTo(2);
        assertThat(GameQueryService.permanentHasSubtype(ownBears, CardSubtype.BEAR)).isTrue();
    }

    @Test
    @DisplayName("Effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new GoblinEliteInfantry());

        castEgoErasure(player2.getId());
        assertThat(goblin.getEffectivePower()).isEqualTo(0);
        assertThat(GameQueryService.permanentHasSubtype(goblin, CardSubtype.GOBLIN)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(goblin.getEffectivePower()).isEqualTo(2);
        assertThat(GameQueryService.permanentHasSubtype(goblin, CardSubtype.GOBLIN)).isTrue();
    }

    private void castEgoErasure(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new EgoErasure()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player1, 0, targetPlayerId);
    }
}
