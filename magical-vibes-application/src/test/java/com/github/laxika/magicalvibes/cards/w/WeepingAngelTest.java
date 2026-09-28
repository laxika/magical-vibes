package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeepingAngel.class, GrizzlyBears.class, MindStone.class, Shock.class})
class WeepingAngelTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent casting a creature spell makes Weeping Angel a noncreature artifact until end of turn")
    void opponentCreatureSpellTurnsAngelIntoArtifact() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new WeepingAngel());

        opponentCasts(new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, angel)).isFalse();
        assertThat(gqs.isArtifact(gd, angel)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, angel)).isTrue();
    }

    @Test
    @DisplayName("A noncreature spell does not turn Weeping Angel into a statue")
    void noncreatureSpellDoesNotTurnAngelIntoArtifactOnly() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new WeepingAngel());

        opponentCasts(new MindStone(), "{2}");
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, angel)).isTrue();
    }

    @Test
    @DisplayName("Combat damage from Weeping Angel shuffles the damaged creature instead of dealing damage")
    void combatDamageShufflesDamagedCreature() {
        Permanent angel = addAttacker(player1);
        Permanent blocker = addBlockerWithPowerAndToughness(player2, 0, 3);
        harness.setLibrary(player2, List.of(new MindStone()));

        resolveWeepingAngelCombat();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(angel.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The shuffle replacement does not apply to noncombat damage")
    void noncombatDamageIsNotReplaced() {
        Card angelCard = new WeepingAngel();
        angelCard.setToughness(3);
        Permanent angel = harness.addToBattlefieldAndReturn(player2, angelCard);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, angel.getId());
        harness.passBothPriorities();

        assertThat(angel.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Combat damage that cannot be prevented does not shuffle the damaged creature")
    void unpreventableCombatDamageDoesNotShuffle() {
        addAttacker(player1);
        Permanent blocker = addBlockerWithPowerAndToughness(player2, 0, 3);
        harness.setLibrary(player2, List.of(new MindStone()));
        gd.damageCantBePreventedThisTurn = true;

        resolveWeepingAngelCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    private void opponentCasts(Card card, String manaCost) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, card, manaCost);
    }

    private Permanent addAttacker(Player player) {
        Permanent attacker = harness.addToBattlefieldAndReturn(player, new WeepingAngel());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent addBlockerWithPowerAndToughness(
            Player player, int power, int toughness) {
        Card blockerCard = new GrizzlyBears();
        blockerCard.setPower(power);
        blockerCard.setToughness(toughness);
        Permanent blocker = new Permanent(blockerCard);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player.getId()).add(blocker);
        return blocker;
    }

    private void resolveWeepingAngelCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
