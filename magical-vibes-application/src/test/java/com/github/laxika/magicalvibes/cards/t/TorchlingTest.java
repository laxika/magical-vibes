package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Torchling.class, GrizzlyBears.class, ProdigalSorcerer.class, Shock.class})
class TorchlingTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps itself")
    void untapsItself() {
        Permanent torchling = addReadyCreature(player1, new Torchling());
        torchling.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(torchling.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Forces a target creature to block it when able")
    void forcesTargetCreatureToBlock() {
        Permanent torchling = addReadyCreature(player1, new Torchling());
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, blocker.getId());
        harness.passBothPriorities();

        torchling.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

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
        GrizzlyBears bearsCard = new GrizzlyBears();
        harness.addToBattlefield(player1, torchlingCard);
        harness.addToBattlefield(player1, bearsCard);
        Permanent torchling = findPermanent(player1, "Torchling");
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, torchling.getId());
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 2, null, shock.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(torchling);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Cannot redirect a spell targeting another creature")
    void cannotTargetSpellThatTargetsAnotherCreature() {
        Permanent torchling = addReadyCreature(player1, new Torchling());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot redirect an activated ability")
    void cannotTargetActivatedAbility() {
        Permanent torchling = addReadyCreature(player1, new Torchling());
        Permanent sorcerer = addReadyCreature(player2, new ProdigalSorcerer());

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, 0, null, torchling.getId());
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 2, null, gd.stack.getFirst().getCard().getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The power and toughness abilities apply until end of turn")
    void changesPowerAndToughnessUntilEndOfTurn() {
        Permanent torchling = addReadyCreature(player1, new Torchling());
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

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
