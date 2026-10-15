package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SpectralBinding;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BindingGeist.class, SpectralBinding.class, GrizzlyBears.class, FountainOfYouth.class})
class BindingGeistTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gives an opponent's creature -2/-0 until end of turn")
    void debuffsTargetOpponentCreatureOnAttack() {
        addCreatureReady(player1, new BindingGeist());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        gd.playerAutoStopSteps.put(player1.getId(), Set.of(TurnStep.END_STEP));
        addCreatureReady(player1, new BindingGeist());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        gs.declareBlockers(gd, player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Disturb casts Spectral Binding transformed and applies its debuff")
    void disturbEntersTransformedAndDebuffsEnchantedCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = castWithDisturb(bears.getId());

        assertThat(aura.isTransformed()).isTrue();
        assertThat(aura.getCard()).isInstanceOf(SpectralBinding.class);
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Spectral Binding's debuff ends when the Aura leaves the battlefield")
    void auraRemovalEndsDebuff() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = castWithDisturb(bears.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Disturb requires a creature target")
    void disturbRequiresCreatureTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        prepareDisturbCast();

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Spectral Binding is exiled instead of going to the graveyard")
    void auraIsExiledInsteadOfGraveyard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = castWithDisturb(bears.getId());
        UUID cardId = aura.getOriginalCard().getId();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId())).contains(cardId);
    }

    @Test
    @DisplayName("The attack trigger cannot target a creature its controller controls")
    void attackTriggerRejectsOwnCreature() {
        Permanent geist = addCreatureReady(player1, new BindingGeist());
        Permanent opponent = addCreatureReady(player2, new BindingGeist());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, geist.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, opponent.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(1);
    }

    @Test
    @DisplayName("The attack trigger resolves even if Binding Geist leaves the battlefield")
    void attackTriggerSurvivesSourceRemoval() {
        Permanent geist = addCreatureReady(player1, new BindingGeist());
        Permanent opponent = addCreatureReady(player2, new BindingGeist());
        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, opponent.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, geist));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Binding Geist");
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(1);
    }

    @Test
    @DisplayName("A disturbed Aura is exiled if its target disappears before resolution")
    void disturbWithMissingTargetIsExiled() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BindingGeist());
        prepareDisturbCast();
        UUID cardId = gd.playerGraveyards.get(player1.getId()).getFirst().getId();
        harness.castFlashback(player1, 0, target.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId())).contains(cardId);
    }

    @Test
    @DisplayName("Spectral Binding can enchant an opponent's creature and is exiled when it dies")
    void auraIsExiledWhenEnchantedCreatureDies() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BindingGeist());
        Permanent aura = castWithDisturb(target.getId());
        UUID cardId = aura.getOriginalCard().getId();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Binding Geist");
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId())).contains(cardId);
    }

    private Permanent castWithDisturb(UUID targetId) {
        prepareDisturbCast();
        harness.castAndResolveFlashback(player1, 0, targetId);
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isTransformed)
                .findFirst()
                .orElseThrow();
    }

    private void prepareDisturbCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new BindingGeist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
