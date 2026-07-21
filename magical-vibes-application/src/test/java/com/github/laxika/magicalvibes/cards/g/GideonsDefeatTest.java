package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GideonsDefeatTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a white attacking creature; no life gained when it isn't a Gideon")
    void exilesWhiteAttackerWithoutLifeGain() {
        harness.setLife(player2, 20);
        Permanent attacker = new Permanent(new EliteVanguard());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GideonsDefeat()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();

        // Exiled — gone from the battlefield and not in the graveyard (exile, not destroy).
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Elite Vanguard"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Elite Vanguard"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Elite Vanguard"));
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Exiling an attacking Gideon planeswalker gains the caster 5 life")
    void exilingGideonGainsFiveLife() {
        harness.setLife(player2, 20);

        Permanent gideon = new Permanent(new GideonOfTheTrials());
        gideon.setCounterCount(CounterType.LOYALTY, 3);
        gideon.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(gideon);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // 0: becomes a 4/4 creature that's still a Gideon planeswalker.
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, gideon)).isTrue();

        gideon.setAttacking(true);

        harness.setHand(player2, List.of(new GideonsDefeat()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, gideon.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Gideon of the Trials"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Gideon of the Trials"));
        harness.assertLife(player2, 25);
    }

    @Test
    @DisplayName("Cannot target a non-white attacking creature")
    void cannotTargetNonWhiteAttacker() {
        // A legal white attacker makes the spell castable; aiming at the green attacker is rejected.
        Permanent legalWhiteAttacker = new Permanent(new EliteVanguard());
        legalWhiteAttacker.setSummoningSick(false);
        legalWhiteAttacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(legalWhiteAttacker);

        Permanent greenAttacker = new Permanent(new GrizzlyBears());
        greenAttacker.setSummoningSick(false);
        greenAttacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(greenAttacker);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GideonsDefeat()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, greenAttacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("white creature");
    }

    @Test
    @DisplayName("Cannot target a white creature that is neither attacking nor blocking")
    void cannotTargetIdleWhiteCreature() {
        // A legal white attacker makes the spell castable; aiming at the idle white creature is rejected.
        Permanent legalWhiteAttacker = new Permanent(new EliteVanguard());
        legalWhiteAttacker.setSummoningSick(false);
        legalWhiteAttacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(legalWhiteAttacker);

        Permanent idleWhite = new Permanent(new EliteVanguard());
        idleWhite.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(idleWhite);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GideonsDefeat()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, idleWhite.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("white creature");
    }
}
