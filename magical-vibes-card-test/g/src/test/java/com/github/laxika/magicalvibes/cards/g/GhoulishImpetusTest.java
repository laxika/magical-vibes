package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.r.Regrowth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;




@CardUsed({GhoulishImpetus.class, DarkBanishing.class, GrizzlyBears.class, Regrowth.class, MindRot.class})
class GhoulishImpetusTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1, deathtouch, and goad")
    void enchantedCreatureGetsStaticEffects() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachGhoulishImpetus(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(als.getMustAttackRequirementCount(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Returns attached at the next end step after the enchanted creature dies")
    void returnsAtNextEndStep() {
        Permanent dyingCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent targetCreature = addCreatureReady(player2, new GrizzlyBears());
        attachGhoulishImpetus(player1, dyingCreature);

        destroyCreature(player2, dyingCreature);
        harness.assertInGraveyard(player1, "Ghoulish Impetus");

        advanceToReturn();

        Permanent aura = findPermanent(player1, "Ghoulish Impetus");
        assertThat(aura.getAttachedTo()).isEqualTo(targetCreature.getId());
        harness.assertNotInGraveyard(player1, "Ghoulish Impetus");
    }

    private Permanent attachGhoulishImpetus(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new GhoulishImpetus());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    private void destroyCreature(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new DarkBanishing()));
        harness.addMana(caster, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }
    @Test
    @DisplayName("Boosts the enchanted creature and grants deathtouch")
    void boostsAndGrantsDeathtouch() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Goads the enchanted creature while the Aura remains attached")
    void goadsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachAura(player2, creature);

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    private Permanent attachAura(Player controller, Permanent creature) {
        return attachGhoulishImpetus(controller, creature);
    }

    @Test
    @DisplayName("Casting the Aura attaches it to the selected creature")
    void castingAttachesToCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GhoulishImpetus()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Ghoulish Impetus").getAttachedTo())
                .isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("The Aura's controller chooses among legal creatures when it returns")
    void choosesCreatureOnReturn() {
        Permanent dying = addCreatureReady(player2, new GrizzlyBears());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        attachAura(player1, dying);
        destroyCreature(player2, dying);
        harness.assertNotOnBattlefield(player1, "Ghoulish Impetus");

        advanceToReturn();
        harness.handlePermanentChosen(player1, ownCreature.getId());

        assertThat(findPermanent(player1, "Ghoulish Impetus").getAttachedTo())
                .isEqualTo(ownCreature.getId());
    }

    @Test
    @DisplayName("With no legal creature the Aura stays in the graveyard and does not retry")
    void noCreatureAtEndStepDoesNotRetry() {
        Permanent dying = addCreatureReady(player1, new GrizzlyBears());
        attachAura(player1, dying);
        destroyCreature(player2, dying);
        advanceToReturn();
        harness.assertInGraveyard(player1, "Ghoulish Impetus");
        harness.assertNotOnBattlefield(player1, "Ghoulish Impetus");

        addCreatureReady(player2, new GrizzlyBears());
        advanceToReturn();
        harness.assertInGraveyard(player1, "Ghoulish Impetus");
        harness.assertNotOnBattlefield(player1, "Ghoulish Impetus");
    }

    @Test
    @DisplayName("Removing the Aura removes its continuous bonuses and goad")
    void removingAuraRemovesStaticEffects() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = attachAura(player1, creature);
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
        assertThat(als.getMustAttackRequirementCount(gd, creature)).isZero();
    }

    @Test
    @DisplayName("The delayed return cannot return an Aura that left and reentered the graveyard")
    void doesNotReturnNewGraveyardObject() {
        Permanent dying = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        Card aura = attachAura(player1, dying).getCard();
        destroyCreature(player2, dying);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Regrowth(), new MindRot()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveSorcery(player1, 0, aura.getId());
        harness.assertInHand(player1, "Ghoulish Impetus");

        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player1, "Ghoulish Impetus");
        advanceToReturn();

        harness.assertInGraveyard(player1, "Ghoulish Impetus");
        harness.assertNotOnBattlefield(player1, "Ghoulish Impetus");
    }

    private void advanceToReturn() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
