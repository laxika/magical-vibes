package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SokenzanSpellblade.class, SakuraTribeScout.class})
class SokenzanSpellbladeTest extends BaseCardTest {

    @Test
    @DisplayName("Bushido 1 triggers when Sokenzan Spellblade becomes blocked")
    void becomesBlockedGetsBushidoBonus() {
        Permanent spellblade = addCreatureReady(player1, new SokenzanSpellblade());
        addCreatureReady(player2, new SakuraTribeScout());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(spellblade.getPowerModifier()).isEqualTo(1);
        assertThat(spellblade.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Bushido 1 triggers when Sokenzan Spellblade blocks")
    void blocksGetsBushidoBonus() {
        addCreatureReady(player1, new SakuraTribeScout());
        Permanent spellblade = addCreatureReady(player2, new SokenzanSpellblade());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(spellblade.getPowerModifier()).isEqualTo(1);
        assertThat(spellblade.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Activated ability uses the controller's hand size when it resolves")
    void activatedAbilityUsesHandSizeAtResolution() {
        Permanent spellblade = addCreatureReady(player1, new SokenzanSpellblade());
        harness.setHand(player1, List.of(new SakuraTribeScout(), new SakuraTribeScout()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        gd.playerHands.get(player1.getId()).add(new SakuraTribeScout());
        harness.passBothPriorities();

        assertThat(spellblade.getPowerModifier()).isEqualTo(3);
        assertThat(spellblade.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An unblocked Sokenzan Spellblade gets no Bushido bonus")
    void unblockedGetsNoBushidoBonus() {
        Permanent spellblade = addCreatureReady(player1, new SokenzanSpellblade());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(spellblade.getPowerModifier()).isZero();
        assertThat(spellblade.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Bushido 1 wears off at the end of the turn")
    void bushidoBonusWearsOffAtEndOfTurn() {
        Permanent spellblade = addCreatureReady(player1, new SokenzanSpellblade());
        addCreatureReady(player2, new SakuraTribeScout());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(spellblade.getPowerModifier()).isEqualTo(1);
        assertThat(spellblade.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(spellblade.getPowerModifier()).isZero();
        assertThat(spellblade.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Bushido 1 triggers only once when Sokenzan Spellblade becomes blocked by multiple creatures")
    void becomesBlockedByMultipleCreaturesGetsOneBushidoBonus() {
        Permanent spellblade = addCreatureReady(player1, new SokenzanSpellblade());
        addCreatureReady(player2, new SakuraTribeScout());
        addCreatureReady(player2, new SakuraTribeScout());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackersAndPrepareBlockers(List.of(0));
            gs.declareBlockers(gd, player2, List.of(
                    new BlockerAssignment(0, 0),
                    new BlockerAssignment(1, 0)));
            resolveAllTriggers();
        });

        assertThat(spellblade.getPowerModifier()).isEqualTo(1);
        assertThat(spellblade.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The activated ability gives no bonus when its controller has no cards in hand")
    void activatedAbilityWithEmptyHandGivesNoBonus() {
        Permanent spellblade = addCreatureReady(player1, new SokenzanSpellblade());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(spellblade.getPowerModifier()).isZero();
        assertThat(spellblade.getToughnessModifier()).isZero();
    }
}
