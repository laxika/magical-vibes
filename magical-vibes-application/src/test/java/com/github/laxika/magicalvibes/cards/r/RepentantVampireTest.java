package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.Gravedigger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RepentantVampireTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when a creature it damaged in combat dies")
    void getsCounterWhenDamagedCreatureDies() {
        harness.addToBattlefield(player1, new RepentantVampire());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent vampire = gd.playerBattlefields.get(player1.getId()).getFirst();
        vampire.setSummoningSick(false);
        vampire.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Threshold makes it white and grants the black-creature destroy ability")
    void thresholdGrantsWhiteColorAndDestroyAbility() {
        fillGraveyard(player1, 7);
        Permanent vampire = addReadyVampire(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Gravedigger());

        assertThat(gqs.getEffectiveColors(gd, vampire)).containsExactly(CardColor.WHITE);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Threshold abilities are absent below seven cards in the controller's graveyard")
    void thresholdAbilitiesAbsentBelowSevenCards() {
        fillGraveyard(player1, 6);
        Permanent vampire = addReadyVampire(player1);

        assertThat(gqs.hasColor(gd, vampire, CardColor.WHITE)).isFalse();
        assertThat(gs.getEffectiveActivatedAbilities(gd, vampire)).isEmpty();
    }

    @Test
    @DisplayName("The threshold ability cannot target a nonblack creature")
    void cannotTargetNonblackCreature() {
        fillGraveyard(player1, 7);
        addReadyVampire(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black creature");
    }

    private Permanent addReadyVampire(Player player) {
        Permanent vampire = harness.addToBattlefieldAndReturn(player, new RepentantVampire());
        vampire.setSummoningSick(false);
        return vampire;
    }

    private void fillGraveyard(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Spellbook());
        }
        harness.setGraveyard(player, cards);
    }
}
