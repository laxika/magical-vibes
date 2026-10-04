package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.ElaborateFirecannon;
import com.github.laxika.magicalvibes.cards.k.KalonianHydra;
import com.github.laxika.magicalvibes.cards.s.SkilledAnimator;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GargosViciousWatcher.class, KalonianHydra.class, GrizzlyBears.class,
        GiantGrowth.class, ElaborateFirecannon.class, TurnToFrog.class, SkilledAnimator.class})
class GargosViciousWatcherTest extends BaseCardTest {

    @Test
    @DisplayName("Hydra spells you cast cost four less")
    void hydraSpellsCostFourLess() {
        harness.addToBattlefield(player1, new GargosViciousWatcher());
        harness.setHand(player1, List.of(new KalonianHydra()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Kalonian Hydra");
    }

    @Test
    @DisplayName("Fights up to one target creature an opponent controls when your creature is targeted")
    void fightsTargetCreatureWhenOwnCreatureIsTargeted() {
        harness.addToBattlefield(player1, new GargosViciousWatcher());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, ownCreature.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        assertThat(gd.stack).anyMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard().getName().equals("Gargos, Vicious Watcher")
                        && entry.getSourcePermanentId() != null);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(opponentCreature.getId()));
    }

    @Test
    @DisplayName("An activated ability targeting your creature does not trigger the fight")
    void doesNotTriggerForActivatedAbility() {
        harness.addToBattlefield(player1, new GargosViciousWatcher());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.addToBattlefield(player2, new ElaborateFirecannon());

        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.activateAbility(player2, 0, null, ownCreature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    void doesNotReduceNonHydraCosts() {
        harness.addToBattlefield(player1, new GargosViciousWatcher());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotReduceOpponentsHydraCosts() {
        harness.addToBattlefield(player2, new GargosViciousWatcher());
        harness.setHand(player1, List.of(new KalonianHydra()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotFightIfGargosLeavesBeforeResolution() {
        Permanent gargos = harness.addToBattlefieldAndReturn(player1, new GargosViciousWatcher());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, ownCreature.getId());
        harness.handlePermanentChosen(player1, opponent.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, gargos));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponent);
        assertThat(opponent.getMarkedDamage()).isZero();
    }

    @Test
    void canChooseNoFightTarget() {
        Permanent gargos = harness.addToBattlefieldAndReturn(player1, new GargosViciousWatcher());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, gargos.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponent);
        assertThat(opponent.getMarkedDamage()).isZero();
    }

    @Test
    void doesNotTriggerAfterLosingAbilities() {
        Permanent gargos = harness.addToBattlefieldAndReturn(player1, new GargosViciousWatcher());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TurnToFrog(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, gargos.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, ownCreature.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    void targetingAnAnimatedArtifactTriggersFight() {
        harness.addToBattlefield(player1, new GargosViciousWatcher());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ElaborateFirecannon());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SkilledAnimator(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0, artifact.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, artifact.getId());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, opponent.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponent);
    }
}
