package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImmortalObligation.class, GrizzlyBears.class})
class ImmortalObligationTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature from an opponent's graveyard under its owner's control with a duty counter")
    void returnsCreatureUnderItsOwnersControlWithDutyCounter() {
        Card target = new GrizzlyBears();

        castAgainst(target);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId())
                        && permanent.getCounterCount(CounterType.DUTY) == 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A creature with a duty counter is goaded and cannot attack or block the controller's creatures")
    void dutyCreatureHasThePrintedRestrictions() {
        Permanent returned = castAgainst(new GrizzlyBears());
        returned.setSummoningSick(false);

        assertThat(gqs.isGoaded(gd, returned)).isTrue();
        assertThat(als.getMustAttackRequirementCount(gd, returned)).isEqualTo(1);
        assertThat(als.canAttackDefender(gd, returned, player1.getId())).isFalse();

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(returned);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("duty counters");
    }

    @Test
    @DisplayName("Cannot target a creature card in the controller's graveyard")
    void cannotTargetOwnGraveyard() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new ImmortalObligation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castAgainst(Card target) {
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new ImmortalObligation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(target.getId()))
                .findFirst()
                .orElseThrow();
    }
}
