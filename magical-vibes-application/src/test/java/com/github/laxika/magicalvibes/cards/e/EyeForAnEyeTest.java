package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.l.LivingArtifact;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EyeForAnEye.class, GrizzlyBears.class, LightningBolt.class, LivingArtifact.class,
        ProdigalSorcerer.class, SolRing.class})
class EyeForAnEyeTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Eye for an Eye prompts for a source choice")
    void resolvingPromptsForSourceChoice() {
        castEyeForAnEye(player1);
        addCreatureReady(player2, new GrizzlyBears());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Choosing a source records a one-shot reflection shield")
    void choosingSourceRecordsShield() {
        castEyeForAnEye(player1);
        Permanent goblin = addCreatureReady(player2, new GrizzlyBears());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        assertThat(gd.eyeForAnEyeShields)
                .anyMatch(s -> s.protectedPlayerId().equals(player1.getId())
                        && s.sourceId().equals(goblin.getId()));
    }

    @Test
    @DisplayName("Chosen source's damage still hits you and is reflected at its controller")
    void reflectsDamageToSourceController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castEyeForAnEye(player1);
        Permanent goblin = addCreatureReady(player2, new GrizzlyBears());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        goblin.setAttacking(true);
        resolveCombat(player2);

        // You still take the 2 damage; 2 is also reflected at the goblin's controller.
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        assertThat(gd.eyeForAnEyeShields).isEmpty();
    }

    @Test
    @DisplayName("A different source deals damage without reflection; the shield is untouched")
    void differentSourceNoReflection() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castEyeForAnEye(player1);
        Permanent chosen = addCreatureReady(player2, new GrizzlyBears());
        Permanent other = addCreatureReady(player2, new GrizzlyBears());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.eyeForAnEyeShields)
                .anyMatch(s -> s.sourceId().equals(chosen.getId()));
    }

    @Test
    @DisplayName("Chosen source's noncombat damage still hits you and is reflected at its controller")
    void reflectsNoncombatDamageToSourceController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());
        castEyeForAnEye(player1);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, sorcerer.getId());

        int sorcererIndex = gd.playerBattlefields.get(player2.getId()).indexOf(sorcerer);
        harness.activateAbility(player2, sorcererIndex, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        assertThat(gd.eyeForAnEyeShields).isEmpty();
    }

    @Test
    @DisplayName("Allows a spell on the stack as a source choice")
    void allowsSpellOnStackAsSourceChoice() {
        addCreatureReady(player2, new GrizzlyBears());
        Card lightningBolt = new LightningBolt();
        harness.setHand(player2, List.of(lightningBolt));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        castEyeForAnEye(player1);

        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(lightningBolt.getId());
    }

    @Test
    @DisplayName("Reflected combat damage triggers damage-to-controller abilities")
    void reflectedCombatDamageTriggersDamageToControllerAbilities() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent aura = addLivingArtifact(player2);
        Permanent goblin = addCreatureReady(player2, new GrizzlyBears());
        castEyeForAnEye(player1);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        goblin.setAttacking(true);
        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(aura.getCounterCount(CounterType.VITALITY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        castEyeForAnEye(player1);
        Permanent goblin = addCreatureReady(player2, new GrizzlyBears());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        assertThat(gd.eyeForAnEyeShields).isNotEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.eyeForAnEyeShields).isEmpty();
    }

    @Test
    @DisplayName("Answering the source choice resumes the parked resolution entry")
    void answeringSourceChoiceClearsParkedResolution() {
        castEyeForAnEye(player1);
        Permanent goblin = addCreatureReady(player2, new GrizzlyBears());

        harness.passBothPriorities();
        assertThat(gd.pendingEffectResolutionEntry).isNotNull();

        harness.handlePermanentChosen(player1, goblin.getId());

        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.deferPlayerLossCheck).isFalse();
    }

    private void castEyeForAnEye(Player player) {
        harness.setHand(player, List.of(new EyeForAnEye()));
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.castInstant(player, 0);
    }

    private Permanent addLivingArtifact(Player player) {
        Permanent artifact = harness.addToBattlefieldAndReturn(player, new SolRing());
        Permanent aura = harness.addToBattlefieldAndReturn(player, new LivingArtifact());
        aura.setAttachedTo(artifact.getId());
        return aura;
    }
}
