package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ExaltedSunborn;
import com.github.laxika.magicalvibes.cards.f.FungalColossus;
import com.github.laxika.magicalvibes.cards.n.NebulaDragon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CloseEncounter.class, NebulaDragon.class, FungalColossus.class, ExaltedSunborn.class})
class CloseEncounterTest extends BaseCardTest {

    @Test
    void dealsChosenCreaturePowerToTargetCreature() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new NebulaDragon());
        Permanent target = addTargetCreature();

        castCloseEncounter(chosen.getId(), target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void usesLastKnownPowerWhenChosenCreatureLeavesBeforeResolution() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new NebulaDragon());
        Permanent target = addTargetCreature();

        castCloseEncounterWithoutResolving(chosen.getId(), target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, chosen));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void usesPowerAsItLastExistedWhenChosenCreatureChangesBeforeLeaving() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new NebulaDragon());
        Permanent target = addTargetCreature();

        castCloseEncounterWithoutResolving(chosen.getId(), target.getId());
        chosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, chosen));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(6);
    }

    @Test
    void canChooseWarpedCreatureCardOwnedInExile() {
        ExaltedSunborn warpedCreature = new ExaltedSunborn();
        harness.setHand(player1, List.of(warpedCreature));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.END_STEP, () -> harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP));
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);
        assertThat(gd.findExiledCard(warpedCreature.getId())).isNotNull();
        Permanent target = addTargetCreature();

        castCloseEncounter(warpedCreature.getId(), target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void cannotChooseCreatureWithWarpExiledByAnotherEffect() {
        ExaltedSunborn exiledCreature = new ExaltedSunborn();
        harness.setExile(player1, List.of(exiledCreature));
        Permanent target = addTargetCreature();

        assertThatThrownBy(() -> castCloseEncounterWithoutResolving(
                exiledCreature.getId(), target.getId())).isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Close Encounter");
    }

    @Test
    void usesChosenCreaturesCurrentPowerAtResolution() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new NebulaDragon());
        Permanent target = addTargetCreature();

        castCloseEncounterWithoutResolving(chosen.getId(), target.getId());
        chosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(6);
    }

    @Test
    void cannotChooseOpponentsCreature() {
        Permanent target = addTargetCreature();

        assertThatThrownBy(() -> castCloseEncounterWithoutResolving(target.getId(), target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void chosenCreaturesLifelinkDoesNotApplyToSpellDamage() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new ExaltedSunborn());
        Permanent target = addTargetCreature();
        harness.setLife(player1, 10);

        castCloseEncounter(chosen.getId(), target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertLife(player1, 10);
    }

    @Test
    void canTargetTheChosenCreature() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new ExaltedSunborn());

        castCloseEncounter(chosen.getId(), chosen.getId());

        assertThat(chosen.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player1, "Exalted Sunborn");
    }

    @Test
    void dealsNoDamageWhenChosenPowerIsZero() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new ExaltedSunborn());
        chosen.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 4);
        Permanent target = addTargetCreature();

        castCloseEncounter(chosen.getId(), target.getId());

        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void cannotCastWithoutChoosingACreatureOrWarpedCard() {
        harness.addToBattlefield(player1, new NebulaDragon());
        Permanent target = addTargetCreature();

        assertThatThrownBy(() -> castCloseEncounterWithoutResolving(null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Close Encounter");
    }

    @Test
    void cannotChooseExiledCreatureWithoutWarp() {
        NebulaDragon exiledCreature = new NebulaDragon();
        harness.setExile(player1, List.of(exiledCreature));
        Permanent target = addTargetCreature();

        assertThatThrownBy(() -> castCloseEncounterWithoutResolving(
                exiledCreature.getId(), target.getId())).isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotResolveWhenTheTargetLeavesTheBattlefield() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new NebulaDragon());
        Permanent target = addTargetCreature();

        castCloseEncounterWithoutResolving(chosen.getId(), target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(chosen.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Close Encounter");
        harness.assertInGraveyard(player2, "Fungal Colossus");
    }

    private void castCloseEncounter(java.util.UUID chosenObjectId, java.util.UUID targetId) {
        castCloseEncounterWithoutResolving(chosenObjectId, targetId);
        harness.withAutoStop(gd.currentStep, harness::passBothPriorities);
    }

    private void castCloseEncounterWithoutResolving(java.util.UUID chosenObjectId, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new CloseEncounter()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstantWithChosenAdditionalCostObject(player1, 0, targetId, chosenObjectId);
    }

    private Permanent addTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FungalColossus());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        return target;
    }
}
