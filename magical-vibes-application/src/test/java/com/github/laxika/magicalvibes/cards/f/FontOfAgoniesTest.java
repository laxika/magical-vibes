package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.Greed;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FontOfAgoniesTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever its controller pays life, Font of Agonies gets that many blood counters")
    void payingLifeAddsBloodCounters() {
        harness.addToBattlefield(player1, new FontOfAgonies());
        harness.addToBattlefield(player1, new Greed());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        Permanent font = findPermanent(player1, "Font of Agonies");

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(font.getCounterCount(CounterType.BLOOD)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing four blood counters destroys a target creature")
    void abilityDestroysTargetCreature() {
        harness.addToBattlefield(player1, new FontOfAgonies());
        Permanent font = findPermanent(player1, "Font of Agonies");
        font.setCounterCount(CounterType.BLOOD, 4);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 2);

        int fontIndex = gd.playerBattlefields.get(player1.getId()).indexOf(font);
        harness.activateAbility(player1, fontIndex, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        assertThat(font.getCounterCount(CounterType.BLOOD)).isZero();
    }

    @Test
    @DisplayName("Font of Agonies cannot be activated without four blood counters")
    void cannotActivateWithoutFourCounters() {
        harness.addToBattlefield(player1, new FontOfAgonies());
        Permanent font = findPermanent(player1, "Font of Agonies");
        font.setCounterCount(CounterType.BLOOD, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 2);

        int fontIndex = gd.playerBattlefields.get(player1.getId()).indexOf(font);
        assertThatThrownBy(() -> harness.activateAbility(player1, fontIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
