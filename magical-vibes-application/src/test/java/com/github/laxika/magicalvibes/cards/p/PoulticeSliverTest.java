package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CradleToGrave;
import com.github.laxika.magicalvibes.cards.s.SerraSphinx;
import com.github.laxika.magicalvibes.cards.s.SinewSliver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PoulticeSliver.class, SinewSliver.class, SerraSphinx.class, CradleToGrave.class})
class PoulticeSliverTest extends BaseCardTest {

    @Test
    void regeneratesTargetSliver() {
        addCreatureReady(player1, new PoulticeSliver());
        Permanent target = harness.enterBattlefieldAndReturn(player2, new SinewSliver());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getRegenerationShield()).isEqualTo(1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new CradleToGrave()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(target.getCard());
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getRegenerationShield()).isZero();
    }

    @Test
    void cannotTargetNonSliver() {
        addCreatureReady(player1, new PoulticeSliver());
        Permanent sphinx = addCreatureReady(player2, new SerraSphinx());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, sphinx.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void itselfCanActivateTheGrantedAbility() {
        Permanent poulticeSliver = addCreatureReady(player1, new PoulticeSliver());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, poulticeSliver.getId());
        harness.passBothPriorities();

        assertThat(poulticeSliver.getRegenerationShield()).isEqualTo(1);
        assertThat(poulticeSliver.isTapped()).isTrue();
    }

    @Test
    void doesNotGrantAbilityToNonSlivers() {
        addCreatureReady(player1, new PoulticeSliver());
        addCreatureReady(player1, new SerraSphinx());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
