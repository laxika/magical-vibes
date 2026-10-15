package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AjanisMantra;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MortalObstinacy.class, GrizzlyBears.class, AjanisMantra.class, FountainOfYouth.class})
class MortalObstinacyTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1")
    void enchantedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachMortalObstinacy(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot enchant a creature an opponent controls")
    void cannotEnchantOpponentCreature() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new MortalObstinacy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Combat damage trigger targets an enchantment and destroys it after sacrificing the Aura")
    void combatDamageSacrificesAuraAndDestroysTargetEnchantment() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attachMortalObstinacy(player1, creature);
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new AjanisMantra());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        creature.setAttacking(true);

        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(enchantment.getId()).doesNotContain(artifact.getId());

        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(aura.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchantment);
    }

    @Test
    @DisplayName("Declining the combat damage trigger keeps the Aura and enchantment")
    void decliningTriggerKeepsPermanents() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attachMortalObstinacy(player1, creature);
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new AjanisMantra());
        creature.setAttacking(true);

        resolveCombat();
        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchantment);
    }

    @Test
    @DisplayName("Mortal Obstinacy itself is a legal target for its combat damage trigger")
    void auraItselfIsLegalTarget() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attachMortalObstinacy(player1, creature);
        creature.setAttacking(true);

        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(aura.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
    }

    @Test
    @DisplayName("A blocked enchanted creature does not trigger Mortal Obstinacy")
    void blockedCreatureDoesNotTrigger() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attachMortalObstinacy(player1, creature);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        creature.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
    }

    @Test
    @DisplayName("Casting on your creature attaches the Aura and grants the boost")
    void castingAttachesAura() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MortalObstinacy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Mortal Obstinacy");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("The Aura can be sacrificed when its own trigger targets it")
    void sacrificingAuraTargetingItselfRemovesBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attachMortalObstinacy(player1, creature);
        creature.setAttacking(true);

        resolveCombat();
        harness.handlePermanentChosen(player1, aura.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(aura.getCard());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("An illegal enchantment target prevents resolution and sacrificing the Aura")
    void removedTargetPreventsSacrifice() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attachMortalObstinacy(player1, creature);
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new AjanisMantra());
        creature.setAttacking(true);

        resolveCombat();
        harness.handlePermanentChosen(player1, enchantment.getId());
        gd.playerBattlefields.get(player2.getId()).remove(enchantment);
        gd.playerGraveyards.get(player2.getId()).add(enchantment.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(aura.getCard());
    }

    @Test
    @DisplayName("The enchantment is not destroyed if the Aura has already left the battlefield")
    void missingAuraPreventsDestruction() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attachMortalObstinacy(player1, creature);
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new AjanisMantra());
        creature.setAttacking(true);

        resolveCombat();
        harness.handlePermanentChosen(player1, enchantment.getId());
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchantment);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(enchantment.getCard());
    }

    private Permanent attachMortalObstinacy(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new MortalObstinacy());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

}
