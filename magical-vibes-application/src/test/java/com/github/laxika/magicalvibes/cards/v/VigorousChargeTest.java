package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VigorousChargeTest extends BaseCardTest {

    @Test
    @DisplayName("Grants trample to the target creature")
    void grantsTrample() {
        Permanent bear = readyCreature(player1);
        castCharge(bear, false);

        assertThat(bear.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("A kicked charge gains life equal to combat damage dealt by the target")
    void kickedChargeGainsLifeEqualToCombatDamage() {
        Permanent bear = readyCreature(player1);
        castCharge(bear, true);
        harness.setLife(player1, 10);

        bear.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("The kicked trigger still works if the target dies after dealing combat damage")
    void kickedTriggerWorksWhenTargetDiesInCombat() {
        Permanent bear = readyCreature(player1);
        Permanent blocker = readyCreature(player2);
        castCharge(bear, true);
        harness.setLife(player1, 10);

        bear.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 2));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An unkicked charge does not grant the life-gain trigger")
    void unkickedChargeDoesNotGainLife() {
        Permanent bear = readyCreature(player1);
        castCharge(bear, false);
        harness.setLife(player1, 10);

        bear.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("The temporary effects wear off at end of turn")
    void temporaryEffectsWearOff() {
        Permanent bear = readyCreature(player1);
        castCharge(bear, true);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        readyCreature(player1);
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new VigorousCharge()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID fountainId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, fountainId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castCharge(Permanent target, boolean kicked) {
        harness.setHand(player1, List.of(new VigorousCharge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        if (kicked) {
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.castKickedInstant(player1, 0, target.getId());
        } else {
            harness.castInstant(player1, 0, target.getId());
        }
        harness.passBothPriorities();
    }

    private Permanent readyCreature(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
