package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.k.KnightOfMeadowgrain;
import com.github.laxika.magicalvibes.cards.w.WizenedCenn;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EgoErasure.class, KnightOfMeadowgrain.class, WizenedCenn.class, WoodlandChangeling.class})
class EgoErasureTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures target player controls get -2/-0")
    void weakensTargetPlayersCreatures() {
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new KnightOfMeadowgrain());
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);

        castEgoErasure(player2.getId());

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures target player controls lose all creature types")
    void stripsAllCreatureTypes() {
        Permanent changeling = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling());
        assertThat(GameQueryService.permanentHasSubtype(changeling, CardSubtype.GOBLIN)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(changeling, CardSubtype.KITHKIN)).isTrue();

        castEgoErasure(player2.getId());

        assertThat(GameQueryService.permanentHasSubtype(changeling, CardSubtype.GOBLIN)).isFalse();
        assertThat(GameQueryService.permanentHasSubtype(changeling, CardSubtype.KITHKIN)).isFalse();
        assertThat(GameQueryService.permanentHasSubtype(changeling, CardSubtype.SHAPESHIFTER)).isFalse();
    }

    @Test
    @DisplayName("Stripping creature types removes tribal buffs")
    void stripCreatureTypesRemovesTribalBuff() {
        harness.addToBattlefield(player2, new WizenedCenn());
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new KnightOfMeadowgrain());
        // 2/2 base + Wizened Cenn (+1/+1) = 3/3
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(3);

        castEgoErasure(player2.getId());

        // -2/-0 from Ego Erasure and no longer a Kithkin, so Wizened Cenn no longer buffs it: 0/2
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not affect caster's creatures when targeting opponent")
    void doesNotAffectCasterCreatures() {
        Permanent ownKnight = harness.addToBattlefieldAndReturn(player1, new KnightOfMeadowgrain());

        castEgoErasure(player2.getId());

        assertThat(gqs.getEffectivePower(gd, ownKnight)).isEqualTo(2);
        assertThat(GameQueryService.permanentHasSubtype(ownKnight, CardSubtype.KITHKIN)).isTrue();
    }

    @Test
    @DisplayName("Can target the caster")
    void canTargetSelf() {
        Permanent ownKnight = harness.addToBattlefieldAndReturn(player1, new KnightOfMeadowgrain());

        castEgoErasure(player1.getId());

        assertThat(gqs.getEffectivePower(gd, ownKnight)).isEqualTo(0);
        assertThat(GameQueryService.permanentHasSubtype(ownKnight, CardSubtype.KITHKIN)).isFalse();
    }

    @Test
    @DisplayName("Only creatures present when the spell resolves are affected")
    void doesNotAffectCreaturesEnteringLater() {
        Permanent existingKnight = harness.addToBattlefieldAndReturn(player2, new KnightOfMeadowgrain());

        castEgoErasure(player2.getId());

        Permanent laterKnight = harness.addToBattlefieldAndReturn(player2, new KnightOfMeadowgrain());
        assertThat(gqs.getEffectivePower(gd, existingKnight)).isEqualTo(0);
        assertThat(GameQueryService.permanentHasSubtype(existingKnight, CardSubtype.KITHKIN)).isFalse();
        assertThat(gqs.getEffectivePower(gd, laterKnight)).isEqualTo(2);
        assertThat(GameQueryService.permanentHasSubtype(laterKnight, CardSubtype.KITHKIN)).isTrue();
    }

    @Test
    @DisplayName("Can target only a player")
    void rejectsPermanentTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new KnightOfMeadowgrain());
        harness.setHand(player1, List.of(new EgoErasure()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new KnightOfMeadowgrain());

        castEgoErasure(player2.getId());
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(0);
        assertThat(GameQueryService.permanentHasSubtype(knight, CardSubtype.KITHKIN)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(GameQueryService.permanentHasSubtype(knight, CardSubtype.KITHKIN)).isTrue();
    }

    private void castEgoErasure(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new EgoErasure()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, targetPlayerId);
    }
}
