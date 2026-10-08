package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.j.JovensFerrets;
import com.github.laxika.magicalvibes.cards.r.RevengeOfRavens;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WulfgarOfIcewindDale.class, JovensFerrets.class, RevengeOfRavens.class})
class WulfgarOfIcewindDaleTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles attack triggers of permanents you control")
    void doublesAttackTriggers() {
        addCreatureReady(player1, new WulfgarOfIcewindDale());
        Permanent ferrets = addCreatureReady(player1, new JovensFerrets());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(ferrets.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Wulfgar's own melee triggers an additional time")
    void meleeBoostsAttackingWulfgar() {
        Permanent wulfgar = addCreatureReady(player1, new WulfgarOfIcewindDale());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(wulfgar.getPowerModifier()).isEqualTo(2);
        assertThat(wulfgar.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Each attacking creature's ability triggers an additional time")
    void doublesEachAttackingCreaturesTrigger() {
        Permanent wulfgar = addCreatureReady(player1, new WulfgarOfIcewindDale());
        Permanent firstFerrets = addCreatureReady(player1, new JovensFerrets());
        Permanent secondFerrets = addCreatureReady(player1, new JovensFerrets());

        declareAttackers(player1, List.of(1, 2));
        resolveAllTriggers();

        assertThat(firstFerrets.getToughnessModifier()).isEqualTo(4);
        assertThat(secondFerrets.getToughnessModifier()).isEqualTo(4);
        assertThat(wulfgar.getPowerModifier()).isZero();
        assertThat(wulfgar.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Does not add triggers to permanents an opponent controls")
    void doesNotDoubleOpponentsAttackTriggers() {
        addCreatureReady(player1, new WulfgarOfIcewindDale());
        Permanent ferrets = addCreatureReady(player2, new JovensFerrets());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(ferrets.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's attacker does not cause your permanent to trigger an additional time")
    void doesNotDoubleTriggersCausedByOpponentsAttacker() {
        addCreatureReady(player1, new WulfgarOfIcewindDale());
        harness.addToBattlefield(player1, new RevengeOfRavens());
        addCreatureReady(player2, new JovensFerrets());
        harness.setLife(player1, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0)));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }
}
