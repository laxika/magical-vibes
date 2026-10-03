package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CuriousObsession.class, GrizzlyBears.class})
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

    @Test
    @DisplayName("The enchanted creature's controller may decline the draw")
    void mayDeclineDraw() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachAura(bears);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An opponent's enchanted creature grants the draw to that opponent")
    void opponentControlsGrantedDrawAbility() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CuriousObsession()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Curious Obsession");
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Curious Obsession");

        advanceToEndStep();
        harness.assertInGraveyard(player1, "Curious Obsession");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking before the Aura enters still prevents its sacrifice")
    void attackBeforeAuraEnteredCounts() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(0));
        resolveCombat();
        attachAura(bears);

        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Curious Obsession");
        assertThat(gd.stack).isEmpty();
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
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
