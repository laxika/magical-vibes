package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({FearOfFalling.class, AirElemental.class, GrizzlyBears.class})
class FearOfFallingTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking makes a creature defending player controls weaker and removes flying")
    void attackTriggerDebuffsDefendingCreature() {
        addCreatureReady(player1, new FearOfFalling());
        Permanent elemental = addCreatureReady(player2, new AirElemental());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, elemental.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The attack trigger cannot target a creature controlled by the attacking player")
    void attackTriggerCannotTargetAttackerControlledCreature() {
        addCreatureReady(player1, new FearOfFalling());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The debuff lasts through cleanup and ends on the attacker's next turn")
    void debuffLastsUntilAttackersNextTurn() {
        addCreatureReady(player1, new FearOfFalling());
        Permanent elemental = addCreatureReady(player2, new AirElemental());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, elemental.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();

        resolveCombat();
        endTurn(player1);
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();

        endTurn(player2);
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A creature without flying is still a legal target and gets -2/-0")
    void attackTriggerDebuffsNonflyingCreature() {
        addCreatureReady(player1, new FearOfFalling());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
    }

    @Test
    @DisplayName("The attack trigger resolves even after its source leaves the battlefield")
    void attackTriggerResolvesWithoutSource() {
        Permanent attacker = addCreatureReady(player1, new FearOfFalling());
        Permanent elemental = addCreatureReady(player2, new AirElemental());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, elemental.getId());
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        gd.playerGraveyards.get(player1.getId()).add(attacker.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();
    }

    private void endTurn(Player activePlayer) {
        harness.setHand(activePlayer, List.of());
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        for (int step = 0; step < 10 && activePlayer.getId().equals(gd.activePlayerId); step++) {
            harness.clearPriorityPassed();
            harness.passBothPriorities();
        }
    }

}
