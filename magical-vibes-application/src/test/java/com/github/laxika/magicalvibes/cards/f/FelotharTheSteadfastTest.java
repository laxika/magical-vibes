package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.w.WallOfVines;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FelotharTheSteadfast.class, Forest.class, GoblinPiker.class, GrizzlyBears.class,
        HillGiant.class, WallOfVines.class})
class FelotharTheSteadfastTest extends BaseCardTest {

    @Test
    @DisplayName("Own creatures assign combat damage equal to toughness")
    void ownCreaturesUseToughnessForCombatDamage() {
        addReadyCreature(player1, new FelotharTheSteadfast());
        Permanent ownWall = addReadyCreature(player1, new WallOfVines());
        Permanent opponentWall = addReadyCreature(player2, new WallOfVines());

        assertThat(gqs.getEffectiveCombatDamage(gd, ownWall)).isEqualTo(3);
        assertThat(gqs.getEffectiveCombatDamage(gd, opponentWall)).isZero();
    }

    @Test
    @DisplayName("Own creatures can attack as though they did not have defender")
    void ownDefenderCanAttack() {
        addReadyCreature(player1, new FelotharTheSteadfast());
        Permanent wall = addReadyCreature(player1, new WallOfVines());
        addReadyCreature(player2, new GrizzlyBears());

        beginAttackers(player1);
        gs.declareAttackers(gd, player1, List.of(battlefieldIndex(player1, wall)));

        assertThat(wall.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("The defender bypass does not affect an opponent's creatures")
    void opponentDefenderCannotAttack() {
        addReadyCreature(player1, new FelotharTheSteadfast());
        Permanent opponentWall = addReadyCreature(player2, new WallOfVines());
        addReadyCreature(player1, new GrizzlyBears());

        beginAttackers(player2);

        assertThatThrownBy(() -> gs.declareAttackers(
                gd, player2, List.of(battlefieldIndex(player2, opponentWall))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The activated ability draws by toughness, then discards by power")
    void activatedAbilityUsesSacrificedPowerAndToughness() {
        Permanent felothar = addReadyCreature(player1, new FelotharTheSteadfast());
        Permanent sacrificed = addReadyCreature(player1, new GoblinPiker());
        addReadyCreature(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new HillGiant()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 3);
        prepareMainPhase();

        harness.activateAbility(player1, battlefieldIndex(player1, felothar), 0, null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(felothar.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Goblin Piker");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Hill Giant");
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void beginAttackers(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginInteraction(new PendingInteraction.AttackerDeclaration(activePlayer.getId()));
    }
}
