package com.github.laxika.magicalvibes.cards.s;

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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeeRed.class, GrizzlyBears.class})
class SeeRedTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+1 and first strike")
    void enchantedCreatureGetsBoostAndFirstStrike() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachAura(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("See Red is sacrificed at the end step if no creature attacked")
    void sacrificedIfNoCreatureAttacked() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachAura(bears);

        advanceToEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof SeeRed);
    }

    @Test
    @DisplayName("See Red remains at the end step if a creature attacked")
    void remainsIfCreatureAttacked() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachAura(bears);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();
        advanceToEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof SeeRed);
    }

    @Test
    void canResolveOnOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SeeRed()));
        harness.addMana(player1, ManaColor.RED, 2);

        gs.playCard(gd, player1, 0, 0, creature.getId(), null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "See Red").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void sacrificeUsesAuraControllerAndLeavesEnchantedCreatureAlive() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachAura(creature);

        advanceToEndStep();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof SeeRed);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(creature);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "See Red")).hasSize(1);
    }

    @Test
    void attackBeforeAuraEnteredPreventsTriggerEvenAfterAttackerLeaves() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(0));
        resolveCombat();
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        gd.playerGraveyards.get(player1.getId()).add(attacker.getCard());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(creature);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "See Red")).hasSize(1);
    }

    @Test
    void sacrificeWaitsForTriggerToResolve() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(creature);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player1, "See Red")).hasSize(1);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "See Red")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof SeeRed);
    }

    private void attachAura(Permanent creature) {
        Permanent aura = new Permanent(new SeeRed());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
