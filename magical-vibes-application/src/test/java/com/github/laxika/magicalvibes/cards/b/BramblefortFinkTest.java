package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.o.OkoThiefOfCrowns;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BramblefortFink.class, OkoThiefOfCrowns.class})
class BramblefortFinkTest extends BaseCardTest {

    @Test
    void becomesTenTenUntilEndOfTurnWithOkoPlaneswalker() {
        Permanent fink = addCreatureReady(player1, new BramblefortFink());
        addReadyOko(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        int originalPower = gqs.getEffectivePower(gd, fink);
        int originalToughness = gqs.getEffectiveToughness(gd, fink);

        harness.activateAbility(player1, battlefieldIndex(player1, fink), 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, fink)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, fink)).isEqualTo(10);

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        assertThat(gqs.getEffectivePower(gd, fink)).isEqualTo(originalPower);
        assertThat(gqs.getEffectiveToughness(gd, fink)).isEqualTo(originalToughness);
    }

    @Test
    void cannotActivateWithoutControllingOkoPlaneswalker() {
        Permanent fink = addCreatureReady(player1, new BramblefortFink());
        addReadyOko(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, fink), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Oko planeswalker");
    }

    private Permanent addReadyOko(Player player) {
        Permanent oko = harness.addToBattlefieldAndReturn(player, new OkoThiefOfCrowns());
        oko.setCounterCount(CounterType.LOYALTY, 3);
        oko.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return oko;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
