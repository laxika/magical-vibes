package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrissSamiteGuardian.class, GrizzlyBears.class, Shock.class})
class OrissSamiteGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability prevents all damage to target creature this turn")
    void preventsAllDamageToTargetCreature() {
        Permanent oriss = harness.addToBattlefieldAndReturn(player1, new OrissSamiteGuardian());
        oriss.setSummoningSick(false);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
        assertThat(oriss.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Grandeur discards another Oriss and locks the target player")
    void grandeurLocksTargetPlayer() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new OrissSamiteGuardian());
        source.setSummoningSick(false);
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OrissSamiteGuardian()));

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player2.getId()))
                .doesNotContain(indexOf(player2, bear));
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId()))
                .contains(indexOf(player1, source));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Oriss, Samite Guardian"));
        assertThat(source.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Grandeur cannot be paid with a different card")
    void grandeurRequiresAnotherOriss() {
        harness.addToBattlefield(player1, new OrissSamiteGuardian());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private int indexOf(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
