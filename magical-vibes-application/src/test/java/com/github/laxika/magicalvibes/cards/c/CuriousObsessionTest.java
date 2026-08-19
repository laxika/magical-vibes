package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CuriousObsessionTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1")
    void enchantedCreatureGetsBoost() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachAura(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Enchanted creature may draw after dealing combat damage to a player")
    void enchantedCreatureDrawsOnCombatDamage() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachAura(bears);
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Curious Obsession is sacrificed at the end step if no creature attacked")
    void sacrificedIfNoCreatureAttacked() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachAura(bears);

        advanceToEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof CuriousObsession);
    }

    @Test
    @DisplayName("Curious Obsession remains at the end step if a creature attacked")
    void remainsIfCreatureAttacked() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachAura(bears);
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(other)));
        resolveCombat();
        advanceToEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof CuriousObsession);
    }

    private void attachAura(Permanent creature) {
        Permanent aura = new Permanent(new CuriousObsession());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
