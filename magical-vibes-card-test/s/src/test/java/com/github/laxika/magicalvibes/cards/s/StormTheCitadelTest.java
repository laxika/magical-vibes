package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormTheCitadel.class, GrizzlyBears.class, FountainOfYouth.class, AngelicChorus.class,
        SaheeliSublimeArtificer.class})
class StormTheCitadelTest extends BaseCardTest {

    @Test
    @DisplayName("Gives your creatures +2/+2 and the combat-damage destruction ability")
    void boostsOwnCreaturesAndGrantsTrigger() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        castStormTheCitadel();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
        assertThat(PermanentGrants.grantedTriggerEffects(gd, ownCreature, Triggers.dealsCombatDamageToPlayerOrPlaneswalker()))
                .isNotEmpty();
    }

    @Test
    @DisplayName("Combat damage destroys an artifact or enchantment controlled by the defending player")
    void combatDamageDestroysDefendingPlayersPermanent() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.addToBattlefield(player2, new AngelicChorus());

        castStormTheCitadel();
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(artifact.getId());
        assertThat(choice.validIds()).doesNotContain(ownArtifact.getId());

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertOnBattlefield(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("The boost and granted ability wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        castStormTheCitadel();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(PermanentGrants.grantedTriggerEffects(gd, creature, Triggers.dealsCombatDamageToPlayerOrPlaneswalker()))
                .isNotEmpty();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(PermanentGrants.grantedTriggerEffects(gd, creature, Triggers.dealsCombatDamageToPlayerOrPlaneswalker()))
                .isEmpty();
    }

    private void castStormTheCitadel() {
        harness.setHand(player1, List.of(new StormTheCitadel()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("Combat damage to a planeswalker also triggers destruction")
    void combatDamageToPlaneswalkerDestroysArtifact() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new SaheeliSublimeArtificer());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());

        castStormTheCitadel();
        attacker.setAttacking(true);
        attacker.setAttackTarget(planeswalker.getId());
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(artifact.getId()).doesNotContain(ownArtifact.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertOnBattlefield(player1, "Fountain of Youth");
        harness.assertOnBattlefield(player2, "Saheeli, Sublime Artificer");
    }

    @Test
    @DisplayName("The destruction ability can target an enchantment")
    void combatDamageDestroysEnchantment() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        castStormTheCitadel();
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(enchantment.getId()).doesNotContain(opponentCreature.getId());
        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Angelic Chorus");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creatures entering after resolution receive neither the boost nor the ability")
    void laterCreatureIsUnaffected() {
        castStormTheCitadel();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new FountainOfYouth());

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Combat damage with no legal artifact or enchantment target needs no choice")
    void noLegalTargetsDoesNotPrompt() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());

        castStormTheCitadel();
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertOnBattlefield(player1, "Fountain of Youth");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
