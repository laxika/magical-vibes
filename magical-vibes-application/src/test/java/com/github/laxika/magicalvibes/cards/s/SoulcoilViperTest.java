package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulcoilViper.class, GrizzlyBears.class, Forest.class})
class SoulcoilViperTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and returns a target creature with a finality counter")
    void sacrificesSelfAndReturnsCreatureWithFinalityCounter() {
        Permanent viper = addViperReady(player1);
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(target.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(viper);
    }

    @Test
    @DisplayName("Rejects a noncreature graveyard target without paying costs")
    void rejectsNoncreatureTarget() {
        Permanent viper = addViperReady(player1);
        Card target = new Forest();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(viper);
        assertThat(viper.isTapped()).isFalse();
    }

    private void enterMainWithPriority(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Permanent addViperReady(Player player) {
        Permanent permanent = new Permanent(new SoulcoilViper());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
