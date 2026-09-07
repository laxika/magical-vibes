package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SamutTyrantSmasher.class, GrizzlyBears.class})
class SamutTyrantSmasherTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control have haste")
    void grantsHasteToOwnCreatures() {
        addReadySamut(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent ownBear = findPermanent(player1, "Grizzly Bears");
        Permanent opposingBear = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingBear, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("-1 boosts a target creature, grants haste, and scries 1")
    void minusOneBoostsAndScries() {
        Permanent samut = addReadySamut(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bear = findPermanent(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isTrue();
        assertThat(samut.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("-1 cannot target a planeswalker")
    void minusOneCannotTargetPlaneswalker() {
        Permanent samut = addReadySamut(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, samut.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadySamut(Player player) {
        Permanent perm = new Permanent(new SamutTyrantSmasher());
        perm.setCounterCount(CounterType.LOYALTY, 4);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
