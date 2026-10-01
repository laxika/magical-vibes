package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AvenRiftwatcher;
import com.github.laxika.magicalvibes.cards.d.DeadGone;
import com.github.laxika.magicalvibes.cards.e.Electrolyze;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Torchling.class, AvenRiftwatcher.class, DeadGone.class, ProdigalPyromancer.class, Electrolyze.class})
class TorchlingTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps itself")
    void untapsItself() {
        Permanent torchling = addCreatureReady(player1, new Torchling());
        torchling.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(torchling.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Forces a target creature to block it when able")
    void forcesTargetCreatureToBlock() {
        Permanent torchling = addCreatureReady(player1, new Torchling());
        Permanent blocker = addCreatureReady(player2, new AvenRiftwatcher());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, blocker.getId());
        harness.passBothPriorities();

        torchling.setAttacking(true);
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Redirects a single-target spell that targets only Torchling")
    void redirectsSpellTargetingOnlyTorchling() {
        Torchling torchlingCard = new Torchling();
        AvenRiftwatcher replacementCard = new AvenRiftwatcher();
        harness.addToBattlefield(player1, torchlingCard);
        harness.addToBattlefield(player1, replacementCard);
        Permanent torchling = findPermanent(player1, "Torchling");
        Permanent replacement = findPermanent(player1, "Aven Riftwatcher");

        harness.forceActivePlayer(player2);
        DeadGone deadGone = new DeadGone();
        harness.setHand(player2, List.of(deadGone));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castModalInstant(player2, 0, 0, List.of(torchling.getId()));
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 2, null, deadGone.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, replacement.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(torchling, replacement);
        assertThat(torchling.getMarkedDamage()).isZero();
        assertThat(replacement.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot redirect a spell targeting another creature")
    void cannotTargetSpellThatTargetsAnotherCreature() {
        Permanent torchling = addCreatureReady(player1, new Torchling());
        Permanent replacement = addCreatureReady(player1, new AvenRiftwatcher());

        harness.forceActivePlayer(player2);
        DeadGone deadGone = new DeadGone();
        harness.setHand(player2, List.of(deadGone));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castModalInstant(player2, 0, 0, List.of(replacement.getId()));
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, deadGone.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot redirect an activated ability")
    void cannotTargetActivatedAbility() {
        Permanent torchling = addCreatureReady(player1, new Torchling());
        addCreatureReady(player2, new ProdigalPyromancer());

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, 0, null, torchling.getId());
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 2, null, gd.stack.getFirst().getCard().getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot redirect a spell with multiple targets")
    void cannotRedirectSpellWithMultipleTargets() {
        Permanent torchling = addCreatureReady(player1, new Torchling());
        Electrolyze electrolyze = new Electrolyze();

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(electrolyze));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, Map.of(torchling.getId(), 1, player2.getId(), 1));
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, electrolyze.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Cannot redirect a spell with no targets")
    void cannotRedirectSpellWithNoTargets() {
        Permanent torchling = addCreatureReady(player1, new Torchling());
        AvenRiftwatcher creatureSpell = new AvenRiftwatcher();

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(creatureSpell));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, creatureSpell.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The power and toughness abilities apply until end of turn")
    void changesPowerAndToughnessUntilEndOfTurn() {
        Permanent torchling = addCreatureReady(player1, new Torchling());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();
        assertThat(torchling.getEffectivePower()).isEqualTo(4);
        assertThat(torchling.getEffectiveToughness()).isEqualTo(2);

        harness.activateAbility(player1, 0, 4, null, null);
        harness.passBothPriorities();
        assertThat(torchling.getEffectivePower()).isEqualTo(3);
        assertThat(torchling.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(torchling.getPowerModifier()).isEqualTo(0);
        assertThat(torchling.getToughnessModifier()).isEqualTo(0);
    }
}
