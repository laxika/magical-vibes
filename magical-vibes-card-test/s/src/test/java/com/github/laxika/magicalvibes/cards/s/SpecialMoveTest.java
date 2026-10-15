package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpecialMove.class, HillGiant.class, PithingNeedle.class, InvasionOfZendikar.class})
class SpecialMoveTest extends BaseCardTest {

    @Test
    @DisplayName("Jump Kick destroys an artifact and Dash Attack adds two counters")
    void jumpKickAndDashAttack() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PithingNeedle());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.setHand(player1, List.of(new SpecialMove()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{0, 1},
                List.of(artifact.getId(), attacker.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Pithing Needle");
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Foot Toss deals power damage before sacrificing its source")
    void footTossDamagesAndSacrificesSource() {
        Permanent dashTarget = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        dashTarget.setSummoningSick(false);
        dashTarget.setAttacking(true);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        harness.setHand(player1, List.of(new SpecialMove()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{1, 2},
                List.of(dashTarget.getId(), player2.getId(), source.getId()));
        harness.passBothPriorities();

        assertThat(dashTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Dash Attack rejects a creature that is not attacking or blocking")
    void dashAttackRejectsInactiveCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PithingNeedle());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        harness.setHand(player1, List.of(new SpecialMove()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{0, 1},
                List.of(artifact.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Foot Toss rejects the source creature as the other target")
    void footTossRequiresAnotherTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PithingNeedle());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        harness.setHand(player1, List.of(new SpecialMove()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{0, 2},
                List.of(artifact.getId(), source.getId(), source.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Dash Attack can increase the power of the creature used for Foot Toss")
    void dashAttackAndFootTossCanShareSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        source.setSummoningSick(false);
        source.setAttacking(true);
        harness.setHand(player1, List.of(new SpecialMove()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{1, 2},
                List.of(source.getId(), player2.getId(), source.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Dash Attack can target the creature receiving Foot Toss damage")
    void dashAttackAndFootTossCanShareVictim() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        blocker.setBlocking(true);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new SpecialMove()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{1, 2},
                List.of(blocker.getId(), blocker.getId(), source.getId()));
        harness.passBothPriorities();

        assertThat(blocker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blocker).doesNotContain(source);
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Jump Kick and Foot Toss destroy the artifact and damage a creature")
    void jumpKickAndFootToss() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PithingNeedle());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new SpecialMove()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{0, 2},
                List.of(artifact.getId(), victim.getId(), source.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Pithing Needle");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Foot Toss still sacrifices its source when its damage target disappears")
    void footTossSacrificesSourceWithMissingVictim() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PithingNeedle());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new SpecialMove()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{0, 2},
                List.of(artifact.getId(), victim.getId(), source.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(victim);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Pithing Needle");
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Foot Toss deals no damage when its source disappears")
    void footTossDoesNotDealDamageWithMissingSource() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PithingNeedle());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new SpecialMove()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{0, 2},
                List.of(artifact.getId(), player2.getId(), source.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Pithing Needle");
    }

    @Test
    @DisplayName("Foot Toss can target a battle")
    void footTossCanTargetBattle() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PithingNeedle());
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new SpecialMove()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{0, 2},
                List.of(artifact.getId(), battle.getId(), source.getId()));

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Dash Attack adds counters to a blocking creature")
    void dashAttackCanTargetBlocker() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PithingNeedle());
        Permanent blocker = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        blocker.setBlocking(true);
        harness.setHand(player1, List.of(new SpecialMove()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{0, 1},
                List.of(artifact.getId(), blocker.getId()));
        harness.passBothPriorities();

        assertThat(blocker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player2, "Pithing Needle");
    }

    @Test
    @DisplayName("Foot Toss rejects an opponent's creature as its damage source")
    void footTossRejectsOpponentsSource() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PithingNeedle());
        Permanent source = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new SpecialMove()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{0, 2},
                List.of(artifact.getId(), player2.getId(), source.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
