package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.ContentiousPlan;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({IchorAberration.class, ContentiousPlan.class})
class IchorAberrationTest extends BaseCardTest {

    @Test
    @DisplayName("Proliferating perpetually boosts Ichor Aberration on the battlefield")
    void proliferatingPerpetuallyBoostsBattlefieldAberration() {
        Permanent aberration = addReadyAberration();
        castContentiousPlan();

        assertThat(gqs.getEffectivePower(gd, aberration)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, aberration)).isEqualTo(4);
    }

    @Test
    @DisplayName("Proliferating in the graveyard boosts Ichor Aberration and permits casting it")
    void proliferatingBoostsAndPermitsGraveyardCast() {
        IchorAberration aberration = new IchorAberration();
        harness.setGraveyard(player1, List.of(aberration));
        castContentiousPlan();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        Permanent permanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(candidate -> candidate.getCard().getId().equals(aberration.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(4);
    }

    @Test
    @DisplayName("Ichor Aberration can attack only once its power is at least seven")
    void attacksOnlyAtSevenPower() {
        Permanent aberration = addReadyAberration();
        beginAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        aberration.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> gs.declareAttackers(gd, player1, List.of(0)));

        assertThat(aberration.isAttacking()).isTrue();
    }

    private Permanent addReadyAberration() {
        return addReadyPermanent(player1, new IchorAberration());
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void castContentiousPlan() {
        harness.setHand(player1, List.of(new ContentiousPlan()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }

    private void beginAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginInteraction(new PendingInteraction.AttackerDeclaration(player1.getId()));
    }
}
